package edu.duke.oit.vw.solr.test

import org.specs2.mutable._
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.common.SolrDocumentList

import edu.duke.oit.vw.jena._
import edu.duke.oit.vw.solr._
import edu.duke.oit.vw.models._
import edu.duke.oit.vw.scalatra.ScalateTemplateStringify
import edu.duke.oit.test.helpers.TestServers

class VivoSolrIndexerSpec extends Specification with ScalateTemplateStringify {
  
  val vivo = TestServers.vivo
  vivo.setupConnectionPool()
  TestServers.loadSampleData("/src/test/resources/minimal_person.rdf")

  val solrSrv = TestServers.widgetSolr

  "A Vivo Solr Indexer" should {
    solrSrv.deleteByQuery("*:*")

    val vsi = new VivoSolrIndexer(vivo, solrSrv)

    "be connected to a vivo server with more than 0 people" in {
      // guard against running tests with bad db
      vivo.numPeople() must be_> (0)
    }
    
    "create a retrievable person" in {
      vsi.indexPeople()
      val people = vivo.select(renderFromClassPath("sparql/person.ssp"))
      for (p <- people) {
        val uri = p('person).toString.replaceAll("<|>","")
        val person_object = Person.find(uri, solrSrv)
        val person = person_object.get
        person.uri must_== uri
      }
      success
    }

    "update the indexed document for an individual" in {
      vsi.indexPeople()
      val people = vivo.select(renderFromClassPath("sparql/person.ssp"))

      val uri = people.head('person).toString.replaceAll("<|>","")
      vsi.reindexPerson(uri)
      val person = vsi.getPerson(uri)
      failure
    }.pendingUntilFixed("RDF fixture needs to be updated")

    "be able to execute a sparql 1.1 recursive query" in {
      val model = Map("root_organization_uri" -> "http://localhost/individual/n3590");
      val organizations = vivo.select(renderFromClassPath("sparql/organization.ssp",model));
      success
    }

  }

}
