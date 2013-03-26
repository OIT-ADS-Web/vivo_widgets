package edu.duke.oit.vw.solr

import org.apache.solr.client.solrj.SolrServer
import org.apache.solr.common.SolrInputDocument

import edu.duke.oit.vw.utils._
import edu.duke.oit.vw.scalatra.ScalateTemplateStringify

import com.hp.hpl.jena.rdf.model.ModelFactory
import edu.duke.oit.vw.jena.Sparqler

object OrganizationIndexer extends SimpleConversion 
  with Timer
  with ScalateTemplateStringify 
  with WidgetLogging {

  def index(uri: String,vivo: Vivo,solr: SolrServer,useCache: Boolean = false) = {
    val query = renderFromClassPath("sparql/organizationData.ssp", Map("uri" -> uri))
    val organizationData = vivo.select(query,useCache)
    if (organizationData.size > 0) {

      val grantSparql = renderFromClassPath("sparql/organization/grants.ssp", Map("uri" -> uri))
      log.debug("grant sparql: " + grantSparql)
      val grantData = timer("select grants") { vivo.select(grantSparql,useCache) }.asInstanceOf[List[Map[Symbol, String]]]

      val grants: List[Grant] = grantData.map(grant => new Grant(uri      = grant('agreement).replaceAll("<|>",""),
                                                                 vivoType = grant('type).replaceAll("<|>",""),
                                                                 name     = grant('grantName),
                                                                 extraItems = parseExtraItems(grant,List('agreement,'type,'grantName)))).asInstanceOf[List[Grant]]


      val personSparql = renderFromClassPath("sparql/organization/people.ssp", Map("uri" -> uri))
      log.debug("person sparql: " + personSparql)
      val personData = vivo.select(personSparql,useCache)

      val people: List[PersonReference] = personData.map(person => new PersonReference(uri      = person('person).replaceAll("<|>",""),
                                                                                        vivoType = person('type).replaceAll("<|>",""),
                                                                                        name     = person('name),
                                                                                        title     = person('title),
                                                                                        extraItems = parseExtraItems(person,List('person,'type,'name,'title)))).asInstanceOf[List[PersonReference]]

      val o = new Organization(uri,
                               vivoType = organizationData(0)('type).replaceAll("<|>",""),
                               name     = organizationData(0)('name),
                               people = people,
                               grants = grants,
                               extraItems = parseExtraItems(organizationData(0),List('type,'name)))
      timer("add solr doc") {
        val solrDoc = new SolrInputDocument()
        solrDoc.addField("id",o.uri)
        solrDoc.addField("group","organizations")
        solrDoc.addField("json",o.toJson)
        o.uris.map {uri => solrDoc.addField("uris",uri)}
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

//object RichPersonIndexer extends SimpleConversion 
  //with Timer
  //with ScalateTemplateStringify 
  //with WidgetLogging {

  //def index(uri: String,solr: SolrServer) = {
    //val personModel = ModelFactory.createDefaultModel().read("http://localhost:8080/individual?uri="+uri+"&format=rdfxml&include=all")
    //val ontologyModel = ModelFactory.createDefaultModel().read("http://vivoweb.org/ontology/core")
    //val queryModel = ModelFactory.createUnion(personModel,ontologyModel)
    //val personSparql = renderFromClassPath("rich_export_sparql/personData.ssp", Map("uri" -> uri))
    //val personData = Sparqler.selectingFromModel(personModel,personSparql){ results => Sparqler.simpleResults(results)}
    //println(personData)
    //val publicationSparql = renderFromClassPath("rich_export_sparql/publications.ssp")
    //val publicationData = Sparqler.selectingFromModel(personModel,publicationSparql){ results => Sparqler.simpleResults(results)}
    //println(publicationData)
  //}
//}
