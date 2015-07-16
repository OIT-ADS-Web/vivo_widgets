package edu.duke.oit.vw.solr.test

import org.specs2.mutable._

import edu.duke.oit.vw.jena._
import edu.duke.oit.vw.solr._
import edu.duke.oit.vw.models._
import edu.duke.oit.vw.scalatra.ScalateTemplateStringify
import edu.duke.oit.test.helpers.TestServers

class VivoSolrIndexerSpec extends Specification with ScalateTemplateStringify {
  
  val vivo = TestServers.vivo
  vivo.setupConnectionPool()
  TestServers.loadSampleData("/src/test/resources/minimal_person.n3")

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
/*
    "update the indexed document for an individual" in {
      vsi.indexPeople()
      val people = vivo.select(renderFromClassPath("sparql/person.ssp"))
      val uri = people.head('person).toString.replaceAll("<|>","")
      val original_person = vsi.getPerson(uri)
      val original_title = original_person.get.title
      original_title must_== "Programming CIO"

      TestServers.loadSampleData("/src/test/resources/updated_minimal_person.n3")
      vsi.reindexPerson(uri)
      val person = vsi.getPerson(uri)
      val title = person.get.title
      title must_== "Programming CIO of the Universe"
      success
    }

    "update an individual if one of their related uris has changed" in {
      vsi.indexPeople()
      val people = vivo.select(renderFromClassPath("sparql/person.ssp"))
      val uri = people.head('person).toString.replaceAll("<|>","")
      val original_person = vsi.getPerson(uri)
      val original_title = original_person.get.title
      original_title must_== "Programming CIO"

      TestServers.loadSampleData("/src/test/resources/updated_minimal_person.n3")
      vsi.reindexUri("http://localhost/individual/pub1029903")
      val person = vsi.getPerson(uri)
      val title = person.get.title
      title must_== "Programming CIO of the Universe"
      success
    }
*/
   "update the distinct list of matching documents when a related uri changes" in {
     vsi.indexPeople()
     val people = vivo.select(renderFromClassPath("sparql/person.ssp"))
     val uri = people.head('person).toString.replaceAll("<|>","")
     val original_person = vsi.getPerson(uri)
     val original_title = original_person.get.title
     original_title must_== "Programming CIO"

     TestServers.loadSampleData("/src/test/resources/updated_minimal_person.n3")

     val uris = List("http://localhost/individual/art24520","http://localhost/individual/courseHISTORY790S-07")
     vsi.reindexUris(uris)
     val person = vsi.getPerson(uri)
     val title = person.get.title
     title must_== "Programming CIO of the Universe"
     success
   }

    "be able to execute a sparql 1.1 recursive query" in {
      val model = Map("root_organization_uri" -> "http://localhost/individual/n3590");
      val organizations = vivo.select(renderFromClassPath("sparql/organization.ssp",model));
      success
    }

  }

}
