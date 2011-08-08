package edu.duke.oit.vw.solr

import org.apache.solr.client.solrj.SolrServer
import org.apache.solr.common.SolrInputDocument

import edu.duke.oit.vw.utils._
import edu.duke.oit.vw.scalatra.ScalateTemplateStringify

import com.hp.hpl.jena.rdf.model.ModelFactory
import edu.duke.oit.vw.jena.Sparqler

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
object RichPersonIndexer extends SimpleConversion 
  with Timer
  with ScalateTemplateStringify 
  with WidgetLogging {

  def index(uri: String,solr: SolrServer) = {
    val personModel = ModelFactory.createDefaultModel().read("http://localhost:8080/individual?uri="+uri+"&format=rdfxml&include=all")
    val ontologyModel = ModelFactory.createDefaultModel().read("http://vivoweb.org/ontology/core")
    //val queryModel = ModelFactory.createUnion(personModel,ontologyModel)
    //val personSparql = renderFromClassPath("rich_export_sparql/personData.ssp", Map("uri" -> uri))
    //val personData = Sparqler.selectingFromModel(personModel,personSparql){ results => Sparqler.simpleResults(results)}
    //println(personData)
    val publicationSparql = renderFromClassPath("rich_export_sparql/publications.ssp")
    val publicationData = Sparqler.selectingFromModel(personModel,publicationSparql){ results => Sparqler.simpleResults(results)}
    println(publicationData)
  }
}
