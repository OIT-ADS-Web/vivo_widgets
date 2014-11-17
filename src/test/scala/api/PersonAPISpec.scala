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
    TestServers.loadSampleData("/src/test/resources/minimal_person.rdf")
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
    "primaryEmail" -> "rsmith@example.com",
    "overview" -> "This is an overview.",
    "mentorshipOverview" -> "A mentor overview.",
    "mentorshipAvailabilities" -> "Faculty, Provosts, Deans",
    "phoneNumber" -> "919-555-5555",
    "preferredCitationFormat" -> "http://vivo.duke.edu/vivo/ontology/duke-extension#apaCitation",
    "suffixName" -> "Jr.",
    "prefixName" -> "Miss",
    "imageUri" -> "https://scholars.duke.edu/individual/file_i2977242",
    "imageDownload" -> "https://scholars.duke.edu/individual/i2977242",
    "imageThumbnailUri" -> "https://scholars.duke.edu/individual/file_t2977242"
  ) }
  def p20 = { attributes("imageThumbnailDownload") must_== "https://scholars.duke.edu/individual/t2977242" }
  def p21 = { attributes("mentorshipAvailabilities") must_== "" }
  }
