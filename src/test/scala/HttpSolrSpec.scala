package edu.duke.oit.vw.sparql.test

import org.specs2.mutable._
import edu.duke.oit.test.helpers._
import edu.duke.oit.vw.solr._
import edu.duke.oit.vw.models._
import org.apache.solr.client.solrj.{SolrServer,SolrQuery}

class HttpSolrSpec extends Specification {
  skipAllIf(true)

  "Experimenting with Vivo solr index" should {
    val vivoSolr = TestServers.vivoSolr
    object TestModel extends SolrModel

    "accomplish something useful" in {
      //val query = new SolrQuery().setQuery("professor")
      //val response = vivoSolr.query(query)
      //println(response)
      println(TestModel.search("professor",vivoSolr))
      true must beEqualTo(true)
    }

  }

}
