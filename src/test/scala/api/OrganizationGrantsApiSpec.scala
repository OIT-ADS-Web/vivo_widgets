package edu.duke.oit.api.test

import org.scalatra.test.specs2._
import net.liftweb.json._
import org.specs2.specification.Step
import edu.duke.oit.test.helpers.TestServers

import edu.duke.oit.vw.scalatra._
import edu.duke.oit.vw.solr._

class OrganizationGrantsApiSpec extends ScalatraSpec { def is = s2"""
  The Organization Grants API should return ${ Step(getJson) }
    a grant                                 $grantsSize
    correct grant                           $grant
    correct grant attrs                     $grantAttrs
  """

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

    get("/api/v0.9/organizations/grants/all.json?uri=" + orgUri) {
      json = JsonParser.parse(body).values.asInstanceOf[List[Map[String, Any]]]
    }
  }

  def grantsSize = {json must haveSize(1)}

  def grant = {
    val grant = json.head
    grant must havePairs(
      "uri" -> "https://scholars.duke.edu/individual/gra195634",
      "vivoType" -> "http://vivo.duke.edu/vivo/ontology/duke-extension#ResearchGrant",
      "label" -> "Center for Public Genomics 2.0"
      )
  }

  def grantAttrs = {
    val grantAttrs = json.head("attributes").asInstanceOf[Map[String, Any]]
    grantAttrs must havePairs(
      "organization" -> "https://scholars.duke.edu/individual/our_org",
      "organizationName" -> "Pratt School of Engineering"
      )
  }
}
