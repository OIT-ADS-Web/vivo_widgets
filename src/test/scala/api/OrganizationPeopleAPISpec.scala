package edu.duke.oit.api.test

import org.scalatra.test.specs2._
import net.liftweb.json._
import org.specs2.specification.Step
import edu.duke.oit.test.helpers.TestServers

import edu.duke.oit.vw.scalatra._
import edu.duke.oit.vw.solr._

class OrganizationPeopleApiSpec extends ScalatraSpec { def is = s2"""
  """
  //The Organization People API should return ${ Step(getJson) }
    //a person                                $peopleSize
  //"""

  val orgUri = "https://scholars.duke.edu/individual/our_org"

  var json:List[Map[String, Any]] = _

  addFilter(new WidgetsFilter("vivowidgetcoretest", "/Users/pmm21/work/vivo_widgets/solr/test"), "/*")

  def getJson = {
    val vivo = TestServers.vivo
    vivo.setupConnectionPool()
    TestServers.loadSampleData("/src/test/resources/organizations_with_people.n3")
    val solrServer = TestServers.widgetSolr
    solrServer.deleteByQuery("*:*")
    val indexer = new VivoSolrIndexer(vivo, solrServer)

    indexer.indexOrganizations()

    get("/api/v0.9/organizations/people/all.json?uri=" + orgUri) {
      json = JsonParser.parse(body).values.asInstanceOf[List[Map[String, Any]]]
    }
  }

  def peopleSize = {json must haveSize(0)}
}
