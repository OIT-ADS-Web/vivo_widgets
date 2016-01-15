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

import scala.concurrent._
import scala.concurrent.util._
import scala.concurrent.duration._
import java.util.concurrent.Executors

import collection.mutable.ListBuffer

class VivoSolrIndexer(vivo: Vivo, solr: SolrServer) 
  extends WidgetLogging 
  with Timer
  with ScalateTemplateStringify {

  def indexPeople() = {

    val sparql = renderFromClassPath("sparql/person.ssp")
    log.debug("sparql>>>> " + sparql)
    val peopleUris = vivo.select(sparql).map(_('person))
    val parsedUris = peopleUris.map( p => p.toString.replaceAll("<|>","") )
    if (parsedUris.size() > 0) {
      PersonIndexer.indexAll(parsedUris.toList,vivo,solr)
    }
    log.info("finished indexing people")
    solr.commit(false,false)
  }

  def indexOrganizations() = {

    val sparql = renderFromClassPath("sparql/organization.ssp")
    log.debug("sparql>>>> " + sparql)
    val organizationUris = vivo.select(sparql).map(_('organization))
    val parsedUris = organizationUris.map( o => o.toString.replaceAll("<|>","") )
    if (parsedUris.size() > 0) {
      OrganizationIndexer.indexAll(parsedUris.toList,vivo,solr)
    }
    log.info("finished indexing organizations")
    solr.commit(false,false)
  }

  def indexAll() = {
    implicit val executorService = Executors.newFixedThreadPool(2)
    implicit val executorContext = ExecutionContext.fromExecutorService(executorService)
    val futureList = List(
      Future(indexPeople()),
      Future(indexOrganizations())
    )
    val future = Future.sequence(futureList)
    future onSuccess {
      case results => {
        log.info("done indexing people and organizations.")
      }
    }
    Await.ready(future, Duration(22, HOURS))
  }

  def indexAllSerially() = {
    indexPeople()
    indexOrganizations()
    log.info("done serially indexing people and organizations.")
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

  def reindexPeople(uris: List[String]) = {
    if (uris.size() > 0) {
      PersonIndexer.indexAll(uris.toSet.toList,vivo,solr)
    }
    solr.commit()
  }

  def reindexOrganizations(uris: List[String]) = {
    if (uris.size() > 0) {
      OrganizationIndexer.indexAll(uris.toSet.toList, vivo, solr)
    }
    solr.commit()
  }


  def reindexPerson(uri: String) = {
    timer("index person") {
      PersonIndexer.index(uri, vivo, solr)
    }
    solr.commit
  }

  def reindexOrganization(uri: String) = {
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
