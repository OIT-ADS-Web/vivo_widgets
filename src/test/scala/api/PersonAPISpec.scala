package edu.duke.oit.api.test

import org.scalatra.test.specs2._

import edu.duke.oit.vw._
import net.liftweb.json._
import edu.duke.oit.vw.scalatra._
import edu.duke.oit.vw.solr._
import edu.duke.oit.vw.models._
import edu.duke.oit.test.helpers.TestServers

class PersonApiSpec extends ScalatraSpec { def is = s2"""
  The Person API should
    return 'uri' $e1
  """

  addFilter(new WidgetsFilter, "/*")
  val vivo = TestServers.vivo
  vivo.setupConnectionPool()
  TestServers.loadSampleData("/src/test/resources/minimal_person.rdf")
  val solrServer = TestServers.widgetSolr
  val indexer = new VivoSolrIndexer(vivo, solrServer)

  indexer.indexPeople()

  def e1 = get("/api/v0.9/people/complete/all.json?uri=http://localhost/individual/n503") {
    status must_== 200
    val json = JsonParser.parse(body).values.asInstanceOf[Map[String, Any]]
    json("uri") must_== "http://localhost/individual/n503"
  }
}
