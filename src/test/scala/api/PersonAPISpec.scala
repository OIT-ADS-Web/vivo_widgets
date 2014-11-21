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
  The Person API should return        ${ Step(getJson)}
    top-level person data             $topPersonData
    attributes data                   $attributesData
    two addresses                     $addressesSize
    correct first address             $firstAddress
    correct first address attributes  $firstAddressAttributes
    correct second address            $secondAddress
    correct second address attributes $secondAddressAttributes
    two art works                     $artWorksSize
    correct first art work            $firstArtWork
    correct first art work attrs      $firstArtWorkAttributes
    correct second art work           $secondArtWork
    correct second art work attrs     $secondArtWorkAttributes
    two publications                  $publicationsSize
    correct authored publication      $authoredPublication
    correct authored pub attrs        $authoredPubAttrs
    correct translated pub            $translatedPub
    correct translate pub attrs       $translatedPubAttrs
  """

  val personUri = "http://localhost/individual/n503"

  var json:Map[String, Any] = _
  var attributes:Map[String, Any] = _
  var addresses:List[Map[String, Any]] = _
  var artisticWorks:List[Map[String, Any]] = _
  var publications:List[Map[String, Any]] = _

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
      artisticWorks = json("artisticWorks").asInstanceOf[List[Map[String, Any]]]
      publications = json("publications").asInstanceOf[List[Map[String, Any]]]
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

  def artWorksSize = { artisticWorks must have size(2) }

  def firstArtWork = {
    val firstArtWork = artisticWorks.head
    firstArtWork must havePairs(
      "uri" -> "http://localhost/individual/art24521",
      "label" -> "Musical Work",
      "vivoType" -> "http://vivo.duke.edu/vivo/ontology/duke-art-extension#MusicalComposition" 
      )
  }

  def firstArtWorkAttributes = {
    val firstArtWorkAttributes = artisticWorks.head("attributes").asInstanceOf[Map[String, Any]]
    firstArtWorkAttributes must havePairs(
      "role" -> "Composer",
      "role_description" -> "Description of composer role.",
      "abstract" -> "Abstract of a musical work.",
      "link_url" -> "http://www.example.com/music",
      "link_label" -> "Music Link Label",
      "date" -> "2018-01-01T00:00:00",
      "date_precision" -> "http://vivoweb.org/ontology/core#yearPrecision",
      "type_description" -> "Musical Composition",
      "collaborators" -> "Jocelyn Harrison Olcott"
      )
  }

  def secondArtWork = {
    val secondArtWork = artisticWorks.last
    secondArtWork must havePairs(
      "uri" -> "http://localhost/individual/art24520",
      "label" -> "Title of the acting work",
      "vivoType" -> "http://vivo.duke.edu/vivo/ontology/duke-art-extension#Film" 
      )
  }

  def secondArtWorkAttributes = {
    val secondArtWorkAttributes = artisticWorks.last("attributes").asInstanceOf[Map[String, Any]]
    secondArtWorkAttributes must havePairs(
      "role" -> "Actor",
      "role_description" -> "A description of the acting role.",
      "abstract" -> "This is the abstract of the acting work.",
      "link_url" -> "http://www.example.com/acting",
      "link_label" -> "Acting Link Label",
      "date" -> "2014-06-01T00:00:00",
      "date_precision" -> "http://vivoweb.org/ontology/core#yearMonthPrecision",
      "type_description" -> "Film",
      "commissioning_body" -> "Motion Picture Commissioners",
      "collaborators" -> "Jocelyn Harrison Olcott; Laurent Dubois; Non-Duke Collaborator"
      )
  }

  def publicationsSize = { publications must have size(2) }

  def authoredPublication = {
    val pub = publications.head
    pub must havePairs(
      "uri" -> "http://localhost/individual/pub932901",
      "label" -> "Teaching population health: a competency map approach to education.",
      "vivoType" -> "http://purl.org/ontology/bibo/AcademicArticle"
      )
  }

  def authoredPubAttrs = {
    val pubAttrs = publications.head("attributes").asInstanceOf[Map[String, Any]]
    pubAttrs must havePairs(
      "authorship" -> "http://localhost/individual/author932901-503",
      "authorshipType" -> "http://vivoweb.org/ontology/core#Authorship",
      "numPages" -> "9",
      "edition" -> "10",
      "volume" -> "88",
      "issue" -> "5",
      "isbn10" -> "isbn10",
      "isbn13" -> "isbn13",
      // SHOULD TEST THESE
      //NEED TO ADD JOURNAL "publishedIn" -> "",
      //"publicationVenue" -> "http://localhost/individual/jou1938-808X",
      //"publishedBy" -> "",
      //"isFavorite" -> "false",
      //"parentBookTitle" -> "",
      //"pmcid" -> "",
      //"subtypes" -> "",
      "authorList" -> "Kaprielian, VS; Silberberg, M",
      "editorList" -> "Editor, SM",
      "translatorList" -> "Translator, SM",
      "startPage" -> "626",
      "endPage" -> "637",
      "datetime" -> "http://localhost/individual/dateValue201305",
      "year" -> "2013-05-01T00:00:00",
      "doi" -> "10.1097/ACM.0b013e31828acf27",
      "abstract" -> "This is a publication abstract.",
      "pmid" -> "23524919",
      "publicationSource" -> "pubmed",
      "chicagoCitation" -> "Kaprielian, VS, Silberberg, M. <a href=\"http://localhost:8080/individual/pub932901\">\"Teaching population health: a competency map approach to education.\"</a> <em>Acad Med</em> 88, no. 5 (May 2013): 626-637.",
      "mlaCitation" -> "Kaprielian, VS, Silberberg, M. <a href=\"http://localhost:8080/individual/pub932901\">\"Teaching population health: a competency map approach to education.\"</a> <em>Acad Med</em> 88.5 (May 2013): 626-637.",
      "apaCitation" -> "Kaprielian, VS, Silberberg, M. (2013, May). <a href=\"http://localhost:8080/individual/pub932901\">Teaching population health: a competency map approach to education.</a> <em>Acad Med</em>, <em>88</em>(5), 626-637.",
      "icmjeCitation" -> "Kaprielian VS, Silberberg M. <a href=\"http://localhost:8080/individual/pub932901\">Teaching population health: a competency map approach to education.</a> Acad Med. 2013 May;88(5):626-637. PubMed PMID: 23524919."
      )
  }

  def translatedPub = {todo}

  def translatedPubAttrs = {todo}
}
