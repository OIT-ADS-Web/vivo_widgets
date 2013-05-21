package edu.duke.oit.vw.solr
import edu.duke.oit.vw.scalatra.WidgetsConfig

import org.apache.solr.client.solrj.{SolrServer,SolrQuery}
import org.apache.solr.common.SolrDocumentList

import edu.duke.oit.vw.jena._
import edu.duke.oit.vw.utils._
import edu.duke.oit.vw.scalatra.ScalateTemplateStringify
import edu.duke.oit.vw.models.{Person,Organization}

import org.slf4j.Logger
import org.slf4j.LoggerFactory

// use scala collections with java iterators
import scala.collection.JavaConversions._

class VivoSolrIndexer(vivo: Vivo, solr: SolrServer) 
  extends WidgetLogging 
  with Timer
  with ScalateTemplateStringify {

  def indexPeople(clearCacheOnFinish: Boolean = true) = {

    val sparql = renderFromClassPath("sparql/facultyMember.ssp")
    log.debug("sparql>>>> " + sparql)
    val peopleUris = vivo.select(sparql).map(_('person))
    for (p <- peopleUris) {
      PersonIndexer.index(p.toString.replaceAll("<|>",""),vivo,solr)
    }
    solr.commit()
    if (clearCacheOnFinish) {
      JenaCache.clear
    }
  }

  def indexOrganizations(clearCacheOnFinish: Boolean = true) = {

    val sparql = renderFromClassPath("sparql/organization.ssp", Map("root_organization_uri" -> WidgetsConfig.topLevelOrg))
    log.debug("sparql>>>> " + sparql)
    val organizationUris = vivo.select(sparql).map(_('organization))
    for (o <- organizationUris) {
      log.debug("org>>>>> " + o)
      OrganizationIndexer.index(o.toString.replaceAll("<|>",""),vivo,solr)
    }
    solr.commit()
    if (clearCacheOnFinish) {
      JenaCache.clear
    }
  }

  def indexAll(clearCacheOnFinish: Boolean = true) = {
    indexPeople(false);
    indexOrganizations(false);
    if (clearCacheOnFinish) {
      JenaCache.clear
    }
  }

  def indexPerson(uri:String) = {
    PersonIndexer.index(uri.replaceAll("<|>",""), vivo, solr)
    solr.commit()
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

  def reindexPerson(uri: String) = {
    // logger.debug("reindex person: " + uri)
    timer("index person") {
      PersonIndexer.index(uri, vivo, solr)
    }
    solr.commit
  }

  def reindexOrganization(uri: String) = {
    // logger.debug("reindex organization: " + uri)
    timer("index organization") {
      OrganizationIndexer.index(uri, vivo, solr)
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
