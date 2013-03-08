package edu.duke.oit.vw.solr
import edu.duke.oit.vw.scalatra.WidgetsConfig

import org.apache.solr.client.solrj.{SolrServer,SolrQuery}
import org.apache.solr.common.SolrDocumentList

import edu.duke.oit.vw.jena._
import edu.duke.oit.vw.utils._
import edu.duke.oit.vw.scalatra.ScalateTemplateStringify

import org.slf4j.Logger
import org.slf4j.LoggerFactory

// use scala collections with java iterators
import scala.collection.JavaConversions._

class VivoSolrIndexer(vivo: Vivo, solr: SolrServer) 
  extends WidgetLogging 
  with Timer
  with ScalateTemplateStringify {

  def indexPeople(useCache: Boolean = true,clearCacheOnFinish: Boolean = true) = {

    val sparql = renderFromClassPath("sparql/facultyMember.ssp")
    println("sparql>>>> " + sparql)
    println("select>>> " + (vivo.select(sparql,useCache)))
    val peopleUris = vivo.select(sparql,useCache).map(_('person))
    println("peopleUris>>>" + peopleUris)
    for (p <- peopleUris) {
      println("indexing: " + p)
      PersonIndexer.index(p.toString.replaceAll("<|>",""),vivo,solr,useCache)
    }
    solr.commit()
    if (clearCacheOnFinish) {
      JenaCache.clear
    }
  }

  def indexOrganizations(useCache: Boolean = true,clearCacheOnFinish: Boolean = true) = {

    val sparql = renderFromClassPath("sparql/organization.ssp", Map("root_organization_uri" -> WidgetsConfig.topLevelOrg))
    println("sparql>>>> " + sparql)
    val organizationUris = vivo.select(sparql,useCache).map(_('organization))
    for (o <- organizationUris) {
      log.debug("org>>>>> " + o)
      OrganizationIndexer.index(o.toString.replaceAll("<|>",""),vivo,solr,useCache)
    }
    solr.commit()
    if (clearCacheOnFinish) {
      JenaCache.clear
    }
  }

  def indexAll(useCache: Boolean = true,clearCacheOnFinish: Boolean = true) = {
println("indexAll!!!")
    indexPeople(useCache,false);
    indexOrganizations(useCache,false);
    if (clearCacheOnFinish) {
      JenaCache.clear
    }
  }

  def reindexUri(uri: String) = {
    vivo.loadDriver()
    var query = new SolrQuery();
    query.setQuery( "uris:\"" + uri + "\"" )
    var rsp = solr.query( query )
    val docs:SolrDocumentList = rsp.getResults()
    timer("reindex docs") {
      docs.map { doc =>
        doc.getFieldValue("group").asInstanceOf[String] match {
          case "people" => reindexPerson(doc.getFieldValue("id").asInstanceOf[String])
          case "organizations" => reindexOrganization(doc.getFieldValue("id").asInstanceOf[String])
        }
      }
    }
  }

  def reindexPerson(uri: String,useCache:Boolean=false) = {
    // logger.debug("reindex person: " + uri)
    timer("index person") {
      PersonIndexer.index(uri, vivo, solr, useCache)
    }
    solr.commit
  }

  def reindexOrganization(uri: String,useCache:Boolean=false) = {
    // logger.debug("reindex organization: " + uri)
    timer("index organization") {
      OrganizationIndexer.index(uri, vivo, solr, useCache)
    }
    solr.commit
  }

  def getPerson(uri: String): Option[Person] = {
    Person.find(uri, solr)
  }

  def getOrganization(uri: String): Option[Organization] = {
    Organization.find(uri, solr)
  }


}
