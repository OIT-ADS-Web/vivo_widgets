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
    two addresses                        $addressesSize
    correct first address                $firstAddress
    correct first address attributes     $firstAddressAttributes
    correct second address               $secondAddress
    correct second address attributes    $secondAddressAttributes
  """

  val personUri = "http://localhost/individual/n503"

  var json:Map[String, Any] = _
  var attributes:Map[String, Any] = _
  var addresses:List[Map[String, Any]] = _

  addFilter(new WidgetsFilter("vivowidgetcoretest", "/Users/pmm21/work/vivo_widgets/solr/test"), "/*")

  def getJson = {
    val vivo = TestServers.vivo
    vivo.setupConnectionPool()
    TestServers.loadSampleData("/src/test/resources/minimal_person.n3")
    val solrServer = TestServers.widgetSolr
    solrServer.deleteByQuery("*:*")
    val indexer = new VivoSolrIndexer(vivo, solrServer)

    indexer.indexPeople()

    get("/api/v0.9/people/complete/all.json?uri=" + personUri) {
      json = JsonParser.parse(body).values.asInstanceOf[Map[String, Any]]
      attributes = json("attributes").asInstanceOf[Map[String, Any]]
      addresses = json("addresses").asInstanceOf[List[Map[String, Any]]]
    }
  }

  def topPersonData = { json must havePairs (
    "uri" -> personUri,
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
    "phoneNumber" -> "(919) 555-5555",
    "preferredCitationFormat" -> "http://vivo.duke.edu/vivo/ontology/duke-extension#apaCitation",
    "suffixName" -> "Jr.",
    "prefixName" -> "Miss",
    "imageUri" -> "http://localhost/individual/file_i503",
    "imageDownload" -> "http://localhost/individual/i503",
    "imageThumbnailUri" -> "http://localhost/individual/file_t503"
  ) }

  def addressesSize = { addresses must have size(2) }

  def firstAddress = {
    val firstAddress = addresses.head
    firstAddress must havePairs(
      "uri" -> "http://localhost/individual/per_addr503_work_location",
      "vivoType" -> "http://www.w3.org/2006/vcard/ns#Address",
      "label" -> "7605-A Hosp North, Durham, NC 27710"
      )
  }

  def firstAddressAttributes = {
    val firstAddressAttributes = addresses.head("attributes").asInstanceOf[Map[String, Any]]
    firstAddressAttributes must havePairs(
      "address1" -> "7605-A Hosp North",
      "city" -> "Durham",
      "state" -> "NC",
      "postalCode" -> "27710",
      "personUri" -> personUri
      )
  }

  def secondAddress = {
    val secondAddress = addresses.last
    secondAddress must havePairs(
      "uri" -> "http://localhost/individual/per_addr503_work_mailing",
      "vivoType" -> "http://www.w3.org/2006/vcard/ns#Address",
      "label" -> "Box 3090 Med Ctr, Durham, NC 27710"
      )
  }

  def secondAddressAttributes = {
    val secondAddressAttributes = addresses.last("attributes").asInstanceOf[Map[String, Any]]
    secondAddressAttributes must havePairs(
      "address1" -> "Box 3090 Med Ctr",
      "city" -> "Durham",
      "state" -> "NC",
      "postalCode" -> "27710",
      "personUri" -> personUri
      )
  }
}
