package edu.duke.oit.vw.solr

import org.apache.solr.client.solrj.{SolrServer,SolrQuery}
import org.apache.solr.client.solrj.response.QueryResponse
import org.apache.solr.common.{SolrInputDocument,SolrDocumentList}

import org.scardf._
import org.scardf.Node
import org.scardf.NodeConverter._
import org.scardf.jena.JenaGraph

import edu.duke.oit.vw.connection._
import edu.duke.oit.vw.actor.JenaCache
import edu.duke.oit.vw.utils._

import com.hp.hpl.jena.sdb.SDBFactory
import com.hp.hpl.jena.sdb.sql.SDBConnection
import com.hp.hpl.jena.query._
import com.hp.hpl.jena.rdf.model.{Model => JModel, ModelFactory}

import org.slf4j.Logger
import org.slf4j.LoggerFactory

// use scala collections with java iterators
import scala.collection.JavaConversions._

import akka.util._

class Vivo(url: String, user: String, password: String, dbType: String, driver: String) {
//class Vivo(sdbConnection: SDBConnection) {

  def loadDriver() = {
    Class.forName(driver)
  }
  def initializeJenaCache() = {
    loadDriver()
    JenaCache.setFromDatabase(new JenaConnectionInfo(url,user,password,dbType),
                              "http://vitro.mannlib.cornell.edu/default/vitro-kb-2")
  }

  def queryJenaCache(sparql: String) = {
    JenaCache.getModel match {
      case Some(m: JModel) => true
      case _ => initializeJenaCache()
    }
    JenaCache.queryModel(sparql)
  }

  def queryLive(sparql: String) = {
    val sdbConnection = new SDBConnection(url,user,password)
    try {
      val ds = DatasetFactory.create(SDBFactory.connectDataset(sdbConnection,Jena.storeDesc(Some(dbType))))
      val kb2 = ds.getNamedModel("http://vitro.mannlib.cornell.edu/default/vitro-kb-2")
      val owl = ds.getNamedModel("http://vitro.mannlib.cornell.edu/filegraph/tbox/vivo-core-1.2.owl")
      val queryModel = ModelFactory.createUnion(kb2,owl)
      try {
        new JenaGraph(queryModel).select(sparql)
      } finally { ds.close() }
    } finally { sdbConnection.close() }
  }

  def select(sparql: String, useCache: Boolean = false) = {
    if (useCache) {
      queryJenaCache(sparql)
    } else {
      queryLive(sparql)
    }
  }

  def numPeople(useCache: Boolean = false) = asInt(select(sparqlPrefixes + """
    select (count(?p) as ?numPeople) where { ?p rdf:type core:FacultyMember }
  """,useCache)(0)('numPeople).asInstanceOf[NodeFromGraph])

  def sparqlPrefixes: String = """
    PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
    PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
    PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
    PREFIX owl: <http://www.w3.org/2002/07/owl#>
    PREFIX swrl: <http://www.w3.org/2003/11/swrl#>
    PREFIX swrlb: <http://www.w3.org/2003/11/swrlb#>
    PREFIX vitro: <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#>
    PREFIX bibo: <http://purl.org/ontology/bibo/>
    PREFIX dcelem: <http://purl.org/dc/elements/1.1/>
    PREFIX dcterms: <http://purl.org/dc/terms/>
    PREFIX event: <http://purl.org/NET/c4dm/event.owl#>
    PREFIX foaf: <http://xmlns.com/foaf/0.1/>
    PREFIX geo: <http://aims.fao.org/aos/geopolitical.owl#>
    PREFIX pvs: <http://vivoweb.org/ontology/provenance-support#>
    PREFIX ero: <http://purl.obolibrary.org/obo/>
    PREFIX scires: <http://vivoweb.org/ontology/scientific-research#>
    PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
    PREFIX core: <http://vivoweb.org/ontology/core#>
  """
}


import edu.duke.oit.vw.utils._

class VivoSolrIndexer(vivo: Vivo, solr: SolrServer) extends Timer with WidgetLogging {

  def indexPeople(useCache: Boolean = true) = {
    val peopleUris = vivo.select(vivo.sparqlPrefixes + """
      select ?person where { ?person rdf:type core:FacultyMember }
      """,useCache).map(_('person))
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

object PersonIndexer extends SimpleConversion with Timer {

  def index(uri: String,vivo: Vivo,solr: SolrServer,useCache: Boolean = false) = {
    val query = vivo.sparqlPrefixes + """
    SELECT *
    WHERE {
      <"""+uri+"""> vitro:moniker ?title .
      <"""+uri+"""> rdf:type ?type .
      <"""+uri+"""> rdfs:label ?name .
    }
    """
    val personData = vivo.select(query,useCache)
    if (personData.size > 0) {
      val pubSparql = vivo.sparqlPrefixes + """
      
      select *
      where {
        <"""+uri+"""> core:authorInAuthorship ?authorship .
        ?publication core:informationResourceInAuthorship ?authorship .
        ?publication rdfs:label ?title .
        ?publication rdf:type ?type .
        OPTIONAL {
           ?publication rdf:type ?otherType .
           ?otherType rdfs:subClassOf ?type .
           FILTER(?otherType != ?type)
        }
        FILTER(!BOUND(?otherType) && ?type != owl:Thing)
        OPTIONAL { ?publication bibo:numPages ?numPages . }
        OPTIONAL { ?publication bibo:edition ?edition . }
        OPTIONAL { ?publication bibo:volume ?volume . }
        OPTIONAL { ?publication bibo:issue ?issue . }
        OPTIONAL { ?publication core:hasPublicationVenue ?publicationVenue . ?publicationVenue rdfs:label ?publishedIn . }
        OPTIONAL { ?publication core:publisher ?publisher. ?publisher rdfs:label ?publishedBy . }
        OPTIONAL { ?publication bibo:pageStart ?startPage .}
        OPTIONAL { ?publication bibo:pageEnd ?endPage .}
        OPTIONAL { ?publication core:dateTimeValue ?datetime . ?datetime core:dateTime ?year .}
      }
      """

      val publicationData:List[Map[QVar, Node]] = timer("select pubs") { vivo.select(pubSparql,useCache) }.asInstanceOf[List[Map[QVar, Node]]]

      val pubs: List[Publication] = publicationData.map( pub => new Publication(uri      = getString(pub('publication)).replaceAll("<|>",""),
                                                                                vivoType = getString(pub('type)).replaceAll("<|>",""),
                                                                                title    = getString(pub('title)),
                                                                                authors  = getAuthors(getString(pub('publication)).replaceAll("<|>",""),vivo,useCache),
                                                                                extraItems = parseExtraItems(pub,List('publication,'type,'title)))).asInstanceOf[List[Publication]]

      val grantSparql = vivo.sparqlPrefixes + """
      select  *
      where {
        <"""+uri+"""> core:hasResearcherRole ?role .
        ?role rdfs:label ?roleName .
        ?role core:roleIn ?agreement .
        ?agreement rdf:type ?type .
        ?agreement rdfs:label ?grantName .
        FILTER(?type = core:Grant)
      }
      """

      val grantData = timer("select grants") { vivo.select(grantSparql,useCache) }.asInstanceOf[List[Map[QVar, Node]]]

      val grants: List[Grant] = grantData.map(grant => new Grant(uri      = getString(grant('agreement)).replaceAll("<|>",""),
                                                                 vivoType = getString(grant('type)).replaceAll("<|>",""),
                                                                 name     = getString(grant('grantName)),
                                                                 extraItems = parseExtraItems(grant,List('agreement,'type,'grantName)))).asInstanceOf[List[Grant]]

      val courseSparql = vivo.sparqlPrefixes + """
      select  *
      where {
        <"""+uri+"""> core:hasTeacherRole ?role .
        ?role rdfs:label ?roleName .
        ?role core:roleIn ?course .
        ?course rdf:type ?type .
        ?course rdfs:label ?courseName .
        FILTER(?type = core:Course)
      }
      """

      val courseData = vivo.select(courseSparql,useCache)

      val courses: List[Course] = courseData.map(course => new Course(uri      = getString(course('course)).replaceAll("<|>",""),
                                                                      vivoType = getString(course('type)).replaceAll("<|>",""),
                                                                      name     = getString(course('courseName)),
                                                                      extraItems = parseExtraItems(course,List('course,'type,'courseName)))).asInstanceOf[List[Course]]

      val p = new Person(uri,
                         vivoType = getString(personData(0)('type)).replaceAll("<|>",""),
                         name     = getString(personData(0)('name)),
                         title    = getString(personData(0)('title)),
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

  def parseExtraItems(resultMap: Map[QVar,Node], requiredKeys: List[QVar]): Option[Map[String,String]] = {
    val extraItems = resultMap -- requiredKeys
    Option(extraItems.map(kvp => (kvp._1.name -> getString(kvp._2))))
  }

  def getAuthors(pubURI: String, vivo: Vivo,useCache:Boolean = false): List[String] = {
    val authorData = vivo.select(vivo.sparqlPrefixes + """
     select ?authorName ?rank
     where {
       <"""+pubURI+"""> core:informationResourceInAuthorship ?authorship .
       ?authorship core:linkedAuthor ?author .
       ?author rdfs:label ?authorName .
       OPTIONAL { ?authorship core:authorRank ?rank }
     }
    """,useCache)
    val authorsWithRank = authorData.map(a => (getString(a('authorName)),getString(a.getOrElse('rank, Node.from("0"))))).distinct
    authorsWithRank.sortWith((a1,a2) => (a1._2.toInt < a2._2.toInt)).map(_._1)
  }
}
