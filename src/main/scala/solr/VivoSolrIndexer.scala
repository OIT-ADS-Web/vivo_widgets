package edu.duke.oit.vw.solr

import org.apache.solr.client.solrj.{SolrServer,SolrQuery}
import org.apache.solr.client.solrj.response.QueryResponse
import org.apache.solr.common.{SolrInputDocument,SolrDocumentList}

import edu.duke.oit.vw.connection._
import edu.duke.oit.vw.jena._
import edu.duke.oit.vw.utils._
import edu.duke.oit.vw.scalatra.ScalateTemplateStringify

import com.hp.hpl.jena.sdb.SDBFactory
import com.hp.hpl.jena.sdb.sql.SDBConnection
import com.hp.hpl.jena.query._
import com.hp.hpl.jena.rdf.model.{Model => JModel, ModelFactory}

import org.slf4j.Logger
import org.slf4j.LoggerFactory

// use scala collections with java iterators
import scala.collection.JavaConversions._

import akka.util._

class Vivo(url: String, user: String, password: String, dbType: String, driver: String) extends ScalateTemplateStringify{

  def loadDriver() = {
    Class.forName(driver)
  }
  def initializeJenaCache() = {
    loadDriver()
    JenaCache.setFromDatabase(new JenaConnectionInfo(url,user,password,dbType),
                              "http://vitro.mannlib.cornell.edu/default/vitro-kb-2")
  }

  def queryJenaCache(sparql: String): List[Map[Symbol,String]] = {
    JenaCache.getModel match {
      case Some(m: JModel) => true
      case _ => initializeJenaCache()
    }
    JenaCache.queryModel(sparql)
  }

  def queryLive(sparql: String): List[Map[Symbol,String]] = {
    val sdbConnection = new SDBConnection(url,user,password)
    try {
      val ds = DatasetFactory.create(SDBFactory.connectDataset(sdbConnection,Jena.storeDesc(Some(dbType))))
      val kb2 = ds.getNamedModel("http://vitro.mannlib.cornell.edu/default/vitro-kb-2")
      val owl = ds.getNamedModel("http://vitro.mannlib.cornell.edu/filegraph/tbox/vivo-core-1.2.owl")
      val queryModel = ModelFactory.createUnion(kb2,owl)
      try {
        Sparqler.selectingFromModel(queryModel,sparql) { resultSet => Sparqler.simpleResults(resultSet) }
      } finally { ds.close() }
    } finally { sdbConnection.close() }
  }

  def select(sparql: String, useCache: Boolean = false): List[Map[Symbol,String]] = {
    if (useCache) {
      queryJenaCache(sparql)
    } else {
      queryLive(sparql)
    }
  }

  def numPeople(useCache: Boolean = false) = select(renderFromClassPath("sparql/numberOfPeople.ssp"),useCache)(0)('numPeople).toInt

}


import edu.duke.oit.vw.utils._

class VivoSolrIndexer(vivo: Vivo, solr: SolrServer) 
  extends WidgetLogging 
  with Timer
  with ScalateTemplateStringify {

  def indexPeople(useCache: Boolean = true) = {

    val sparql = renderFromClassPath("sparql/facultyMember.ssp")
    log.debug("sparql>>>> " + sparql)
    val peopleUris = vivo.select(sparql,useCache).map(_('person))
    for (p <- peopleUris) {
      PersonIndexer.index(p.toString.replaceAll("<|>",""),vivo,solr,useCache)
    }
    solr.commit()
  }

  def reindexUri(uri: String) = {
    vivo.loadDriver()
    var query = new SolrQuery();
    query.setQuery( "uris:\"" + uri + "\"" )
    var rsp = solr.query( query )
    val docs:SolrDocumentList = rsp.getResults()
    timer("reindex docs") {
      docs.map {doc => reindexPerson(doc.getFieldValue("id").asInstanceOf[String])}
    }
  }

  def reindexPerson(uri: String,useCache:Boolean=false) = {
    // logger.debug("reindex person: " + uri)
    timer("index person") {
      PersonIndexer.index(uri, vivo, solr, useCache)
    }
    solr.commit
  }

  def getPerson(uri: String): Option[Person] = {
    Person.find(uri, solr)
  }

}

object PersonIndexer extends SimpleConversion 
  with Timer
  with ScalateTemplateStringify 
  with WidgetLogging {

  def index(uri: String,vivo: Vivo,solr: SolrServer,useCache: Boolean = false) = {
    val query = renderFromClassPath("sparql/personData.ssp", Map("uri" -> uri))
    val personData = vivo.select(query,useCache)
    if (personData.size > 0) {
      
      val pubSparql = renderFromClassPath("sparql/publications.ssp", Map("uri" -> uri))
      val publicationData:List[Map[Symbol, String]] = timer("select pubs") { vivo.select(pubSparql,useCache) }.asInstanceOf[List[Map[Symbol, String]]]

      val pubs: List[Publication] = publicationData.map( pub => new Publication(uri      = pub('publication).replaceAll("<|>",""),
                                                                                vivoType = pub('type).replaceAll("<|>",""),
                                                                                title    = pub('title),
                                                                                authors  = getAuthors(pub('publication).replaceAll("<|>",""),vivo,useCache),
                                                                                extraItems = parseExtraItems(pub,List('publication,'type,'title)))).asInstanceOf[List[Publication]]

      val grantSparql = renderFromClassPath("sparql/grants.ssp", Map("uri" -> uri))
      log.debug("grant sparql: " + grantSparql)
      val grantData = timer("select grants") { vivo.select(grantSparql,useCache) }.asInstanceOf[List[Map[Symbol, String]]]

      val grants: List[Grant] = grantData.map(grant => new Grant(uri      = grant('agreement).replaceAll("<|>",""),
                                                                 vivoType = grant('type).replaceAll("<|>",""),
                                                                 name     = grant('grantName),
                                                                 extraItems = parseExtraItems(grant,List('agreement,'type,'grantName)))).asInstanceOf[List[Grant]]


      val courseSparql = renderFromClassPath("sparql/courses.ssp", Map("uri" -> uri))
      log.debug("course sparql: " + courseSparql)
      val courseData = vivo.select(courseSparql,useCache)

      val courses: List[Course] = courseData.map(course => new Course(uri      = course('course).replaceAll("<|>",""),
                                                                      vivoType = course('type).replaceAll("<|>",""),
                                                                      name     = course('courseName),
                                                                      extraItems = parseExtraItems(course,List('course,'type,'courseName)))).asInstanceOf[List[Course]]

      val p = new Person(uri,
                         vivoType = personData(0)('type).replaceAll("<|>",""),
                         name     = personData(0)('name),
                         title    = personData(0)('title),
                         publications = pubs,
                         grants = grants,
                         courses = courses,
                         extraItems = parseExtraItems(personData(0),List('type,'name,'title)))
      timer("add solr doc") {
      val solrDoc = new SolrInputDocument()
      solrDoc.addField("id",p.uri)
      solrDoc.addField("json",p.toJson)
      p.uris.map {uri => solrDoc.addField("uris",uri)}
      solr.add(solrDoc)
      }
    }
  }

  def parseExtraItems(resultMap: Map[Symbol,String], requiredKeys: List[Symbol]): Option[Map[String,String]] = {
    val extraItems = resultMap -- requiredKeys
    Option(extraItems.map(kvp => (kvp._1.name -> kvp._2)))
  }

  def getAuthors(pubURI: String, vivo: Vivo,useCache:Boolean = false): List[String] = {
    val authorSparql = renderFromClassPath("sparql/authors.ssp", Map("uri" -> pubURI))
    log.debug("author sparql: " + authorSparql)
    val authorData = vivo.select(authorSparql,useCache)

    val authorsWithRank = authorData.map(a => (a('authorName),a.getOrElse('rank, 0))).distinct
    authorsWithRank.sortWith((a1,a2) => (Int(a1._2.toString).get < Int(a2._2.toString).get)).map(_._1)
  }
}
