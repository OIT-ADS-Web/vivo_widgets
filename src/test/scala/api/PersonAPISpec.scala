package edu.duke.oit.api.test

import org.scalatra.test.specs2._

import edu.duke.oit.vw._
import net.liftweb.json._
import edu.duke.oit.vw.scalatra._
import edu.duke.oit.vw.solr._
import edu.duke.oit.vw.models._
import edu.duke.oit.test.helpers.TestServers
import org.specs2.specification.Step

import org.apache.solr.client.solrj.SolrRequest
import org.apache.solr.client.solrj.request.CoreAdminRequest

class PersonApiSpec extends ScalatraSpec { def is = s2"""
  The Person API should return           ${ Step(getJson)}
    top-level person data                $topPersonData
    attributes data                      $attributesData
  """

  var json:Map[String, Any] = _
  var attributes:Map[String, Any] = _

  addFilter(new WidgetsFilter("vivowidgetcoretest", "/Users/pmm21/work/vivo_widgets/solr/test"), "/*")

  def getJson = {
    val vivo = TestServers.vivo
    vivo.setupConnectionPool()
    TestServers.loadSampleData("/src/test/resources/minimal_person.n3")
    val solrServer = TestServers.widgetSolr
    solrServer.deleteByQuery("*:*")
    val indexer = new VivoSolrIndexer(vivo, solrServer)

    indexer.indexPeople()

    get("/api/v0.9/people/complete/all.json?uri=http://localhost/individual/n503") {
      json = JsonParser.parse(body).values.asInstanceOf[Map[String, Any]]
      attributes = json("attributes").asInstanceOf[Map[String, Any]]
    }
  }

  def topPersonData = { json must havePairs (
    "uri" -> "http://localhost/individual/n503",
    "vivoType" -> "http://vivoweb.org/ontology/core#FacultyMember",
    "label" -> "Smith, Richard",
    "title" -> "Programming CIO"
  ) }

  def attributesData = { attributes must havePairs(
    "lastName" -> "Smith",
    "firstName" -> "Richard",
    "preferredTitle" -> "Programming CIO",
    "alternateId" -> "0123456",
    "middleName" -> "Big",
    "primaryEmail" -> "rsmith@example.org",
    "overview" -> "This is an overview.",
    "mentorshipOverview" -> "A mentor overview.",
    "mentorshipAvailabilities" -> "Faculty, Provosts, Deans",
    "phoneNumber" -> "919-555-5555",
    "preferredCitationFormat" -> "http://vivo.duke.edu/vivo/ontology/duke-extension#apaCitation",
    "suffixName" -> "Jr.",
    "prefixName" -> "Miss",
    "imageUri" -> "http://localhost/individual/file_i503",
    "imageDownload" -> "http://localhost/individual/i503",
    "imageThumbnailUri" -> "http://localhost/individual/file_t503"
  ) }
}
