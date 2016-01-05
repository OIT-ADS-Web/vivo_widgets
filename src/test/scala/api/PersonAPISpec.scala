package edu.duke.oit.api.test

import org.scalatra.test.specs2._

import net.liftweb.json._
import edu.duke.oit.test.helpers.TestServers
import org.specs2.specification.Step

import edu.duke.oit.vw.scalatra._
import edu.duke.oit.vw.solr._

class PersonApiSpec extends ScalatraSpec { def is = s2"""
  """
  //The Person API should return        ${ Step(getJson)}
    //top-level person data             $topPersonData
    //attributes data                   $attributesData

    //two addresses                     $addressesSize
    //correct first address             $firstAddress
    //correct first address attributes  $firstAddressAttributes
    //correct second address            $secondAddress
    //correct second address attributes $secondAddressAttributes

    //two art works                     $artWorksSize
    //correct first art work            $firstArtWork
    //correct first art work attrs      $firstArtWorkAttributes
    //correct second art work           $secondArtWork
    //correct second art work attrs     $secondArtWorkAttributes

    //two publications                  $publicationsSize
    //correct authored publication      $authoredPublication
    //correct authored pub attrs        $authoredPubAttrs

    //correct number courses            $courseSize
    //correct course fields             $courseFields

    //an education and prof experience  $educationsSize
    //correct education                 $education
    //correct education attrs           $educationAttrs
    //correct prof experience           $profExperience
    //correct prof experience attrs     $profExperienceAttrs

    //a grant                           $grantSize
    //correct grant                     $grant
    //correct grant attrs               $grantAttrs

    //a position                        $positionSize
    //correct position                  $position
    //correct position attrs            $positionAttrs

    //a webpage                         $webpageSize
    //correct webpage                   $webpage
    //correct webpage attrs             $webpageAttrs

    //an award                          $awardSize
    //correct award                     $award
    //correct award attrs               $awardAttrs

    //four prof activities              $profActivitiesSize
    //outreach                          $outreach
    //outreachAttrs                     $outreachAttrs
    //presentation                      $presentation
    //presentationAttrs                 $presentationAttrs
    //serviceToProf                     $serviceToProf
    //serviceToProfAttrs                $serviceToProfAttrs
    //serviceToUniv                     $serviceToUniv
    //serviceToUnivAttrs                $serviceToUnivAttrs
  //"""

  val personUri = "http://localhost/individual/n503"

  var json:Map[String, Any] = _
  var attributes:Map[String, Any] = _
  var addresses:List[Map[String, Any]] = _
  var artisticWorks:List[Map[String, Any]] = _
  var publications:List[Map[String, Any]] = _
  var courses:List[Map[String, Any]] = _
  var educations:List[Map[String, Any]] = _
  var grants:List[Map[String, Any]] = _
  var positions:List[Map[String, Any]] = _
  var webpages:List[Map[String, Any]] = _
  var awards:List[Map[String, Any]] = _
  var professionalActivities:List[Map[String, Any]] = _
  var newsfeeds:List[Map[String, Any]] = _

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
      courses = json("courses").asInstanceOf[List[Map[String, Any]]]
      educations = json("educations").asInstanceOf[List[Map[String, Any]]]
      grants = json("grants").asInstanceOf[List[Map[String, Any]]]
      positions = json("positions").asInstanceOf[List[Map[String, Any]]]
      webpages = json("webpages").asInstanceOf[List[Map[String, Any]]]
      awards = json("awards").asInstanceOf[List[Map[String, Any]]]
      professionalActivities = json("professionalActivities").asInstanceOf[List[Map[String, Any]]]
      newsfeeds = json("newsfeeds").asInstanceOf[List[Map[String, Any]]]
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
      "vivoType" -> "http://www.w3.org/2006/vcard/ns#Location",
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

  def publicationsSize = { publications must have size(3) }

  def authoredPublication = {
    val pubSome = publications.find({pub => pub("uri") == "http://localhost/individual/pub932901"})
    val pub = pubSome match {
      case Some(pub) => pub.asInstanceOf[Map[String, Any]]
    }
    pub must havePairs(
      "uri" -> "http://localhost/individual/pub932901",
      "label" -> "Teaching population health: a competency map approach to education.",
      "vivoType" -> "http://purl.org/ontology/bibo/AcademicArticle"
      )
  }

  def authoredPubAttrs = {
    val pubSome = publications.find({pub => pub("uri") == "http://localhost/individual/pub932901"})
    val pubAttrs = pubSome match {
      case Some(pub) => pub("attributes").asInstanceOf[Map[String, Any]]
    }
    pubAttrs must havePairs(
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
      "publishedBy" -> "Springer New York LLC",
      //"parentBookTitle" -> "",
      //"pmcid" -> "",
      //"subtypes" -> "",
      "isFavorite" -> "true",
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
      "startDate" -> "12/31/1987",
      "finishDate" -> "01/01/1988",
      "conferenceName" -> "Name of Conference",
      "conferenceLocation" -> "Location of Conference, LC",
      "chicagoCitation" -> "Kaprielian, VS, Silberberg, M. <a href=\"http://localhost:8080/individual/pub932901\">\"Teaching population health: a competency map approach to education.\"</a> <em>Acad Med</em> 88, no. 5 (May 2013): 626-637.",
      "mlaCitation" -> "Kaprielian, VS, Silberberg, M. <a href=\"http://localhost:8080/individual/pub932901\">\"Teaching population health: a competency map approach to education.\"</a> <em>Acad Med</em> 88.5 (May 2013): 626-637.",
      "apaCitation" -> "Kaprielian, VS, Silberberg, M. (2013, May). <a href=\"http://localhost:8080/individual/pub932901\">Teaching population health: a competency map approach to education.</a> <em>Acad Med</em>, <em>88</em>(5), 626-637.",
      "icmjeCitation" -> "Kaprielian VS, Silberberg M. <a href=\"http://localhost:8080/individual/pub932901\">Teaching population health: a competency map approach to education.</a> Acad Med. 2013 May;88(5):626-637. PubMed PMID: 23524919."
      )
  }

  def courseSize = { courses must have size(10) }

  def courseFields = {
    val course = courses.head
    course must haveKeys("uri", "vivoType", "label")
  }

  def courseAttrFields = {
    val courseAttrs = courses.head("attributes").asInstanceOf[Map[String, Any]]
    courseAttrs must haveKeys("roleName", "role")
  }

  def educationsSize = {educations must have size(2)}

  def education = {
    val education = educations.find(
      {education => education("label") == "M.D. 1990"}).
      getOrElse(throw new RuntimeException("Education not found."))
    education must havePairs(
      "uri" -> "http://localhost/individual/edu5031990academicDegree77",
      "vivoType" -> "http://vivoweb.org/ontology/core#EducationalProcess")
  }

  def educationAttrs = {
    val education = educations.find(
      {education => education("label") == "M.D. 1990"}).
      getOrElse(throw new RuntimeException("Education not found."))
    val educationAttrs = education("attributes").asInstanceOf[Map[String, Any]]
    educationAttrs must havePairs(
      "degreeUri" -> "http://vivoweb.org/ontology/degree/academicDegree77",
      "endDate" -> "1990-01-01T00:00:00",
      "degree" -> "M.D.",
      "organizationUri" -> "http://localhost/individual/insbrownuniversity",
      "institution" -> "Brown University",
      "dateTimeUri" -> "http://localhost/individual/dateInterval-e19900101",
      "personUri" -> "http://localhost/individual/n503",
      "endUri" -> "http://localhost/individual/dateValue1990"
      )
  }

  def profExperience = {
    val experience = educations.find(
      {experience => experience("label") == "Fellowship In Cornea And External Disease, Refractive Surgery, Ophthalmology"}).
      getOrElse(throw new RuntimeException("Prof experience not found."))
    experience must havePairs(
      "uri" -> "http://localhost/individual/professional_experience200749",
      "vivoType" -> "http://vivoweb.org/ontology/core#EducationalProcess")
  }

  def profExperienceAttrs = {
    val experience = educations.find(
      {experience => experience("label") == "Fellowship In Cornea And External Disease, Refractive Surgery, Ophthalmology"}).
      getOrElse(throw new RuntimeException("Prof experience not found."))
    val experienceAttrs = experience("attributes").asInstanceOf[Map[String, Any]]
    experienceAttrs must havePairs(
      "endDate" -> "1995-07-01T00:00:00",
      "organizationUri" -> "http://localhost/individual/insdukeuniversity",
      "institution" -> "Duke University",
      "dateTimeUri" -> "http://localhost/individual/dateInterval-s19940701-e19950701",
      "personUri" -> "http://localhost/individual/n503",
      "endUri" -> "http://localhost/individual/dateValue19950701",
      "startUri" -> "http://localhost/individual/dateValue19940701",
      "startDate" -> "1994-07-01T00:00:00"
      )
  }

  def grantSize = {grants must haveSize(1)}

  def grant = {
    val grant = grants.head
    grant must havePairs(
      "uri" -> "http://localhost/individual/gra199341",
      "vivoType" -> "http://vivo.duke.edu/vivo/ontology/duke-extension#InstitutionalSupportGrant",
      "label" -> "Development of a Neuroblastoma Clinical Research Program"
      )
  }

  def grantAttrs = {
    val grantAttrs = grants.head("attributes").asInstanceOf[Map[String, Any]]
    grantAttrs must havePairs(
      "endDate" -> "2014-12-31T00:00:00",
      "roleName" -> "Principal Investigator",
      "awardedBy" -> "St. Baldrick's Foundation",
      "awardedByUri" -> "http://localhost/individual/insstbaldricksfoundation",
      "administeredBy" -> "Pediatrics, Hematology-Oncology",
      "administeredByUri" -> "http://localhost/individual/org50000886",
      "startDate" -> "2014-01-01T00:00:00"
      )
  }

  def positionSize = {positions must haveSize(1)}

  def position = {
    val position = positions.head
    position must havePairs(
      "uri" -> "http://localhost/individual/apt3786572",
      "vivoType" -> "http://vivoweb.org/ontology/core#PrimaryPosition",
      "label" -> "Professor of Earth and Ocean Sciences"
      )
  }

  def positionAttrs = {
    val positionAttrs = positions.head("attributes").asInstanceOf[Map[String, Any]]
    positionAttrs must havePairs(
      "startDatetimeUri" -> "http://localhost/individual/dateValue20090101",
      "organizationLabel" -> "Nicholas School of the Environment",
      "organizationUri" -> "http://localhost/individual/org50000478",
      "rank" -> "1",
      "startYear" -> "2009-01-01T00:00:00",
      "dateUri" -> "http://localhost/individual/dateInterval-s20090101",
      "personUri" -> "http://localhost/individual/n503"
      )
  }

  def webpageSize = {webpages must haveSize(1)}

  def webpage = {
    val webpage = webpages.head
    webpage must havePairs(
      "uri" -> "http://localhost/individual/urllink503http://example.com",
      "vivoType" -> "http://www.w3.org/2006/vcard/ns#URL",
      "label" -> "An Example URL"
      )
  }

  def webpageAttrs = {
    val webpageAttrs = webpages.head("attributes").asInstanceOf[Map[String, Any]]
    webpageAttrs must havePairs(
      "personUri" -> "http://localhost/individual/n503",
      "linkURI" -> "http://example.com"
      )
  }

  def awardSize = {awards must haveSize(1)}

  def award = {
    val award = awards.head
    award must havePairs(
      "uri" -> "http://localhost/individual/awd41181",
      "vivoType" -> "http://vivoweb.org/ontology/core#Award",
      "label" -> "Test Date Precision. Association of Public Policy and Management.")
  }

  def awardAttrs = {
    val awardAttrs = awards.head("attributes").asInstanceOf[Map[String, Any]]
    awardAttrs must havePairs(
      "name" -> "Test Date Precision",
      "serviceType" -> "School",
      "date" -> "2018-10-30T00:00:00",
      "datePrecision" -> "http://vivoweb.org/ontology/core#yearMonthDayPrecision",
      "awardedBy" -> "Association of Public Policy and Management",
      "awardedByUri" -> "http://localhost/individual/insassociationofpublicpolicyandmanagement"
      )
  }

  def profActivitiesSize = {professionalActivities must haveSize(4)}

  def outreach = {
    val outreach = professionalActivities.find(
      {outreach => outreach("vivoType") ==
          "http://vivo.duke.edu/vivo/ontology/duke-activity-extension#Outreach"}).
      getOrElse(throw new RuntimeException("Outreach not found."))
    outreach must havePairs(
      "uri" -> "http://localhost/individual/outreach11230",
      "label" -> "National Endowment for the Humanities. Reviewer, documentary film proposals. Outreach Host Organization. Azerbaijan. March 1, 2011 - 2011"
      )
  }

  def outreachAttrs = {
    val outreach = professionalActivities.find(
      {outreach => outreach("vivoType") ==
          "http://vivo.duke.edu/vivo/ontology/duke-activity-extension#Outreach"}).
      getOrElse(throw new RuntimeException("Outreach not found."))
    val outreachAttrs = outreach("attributes").asInstanceOf[Map[String, Any]]
    outreachAttrs must havePairs(
      "startDatePrecision" -> "http://vivoweb.org/ontology/core#yearMonthDayPrecision",
      "serviceOrEventName" -> "National Endowment for the Humanities",
      "serviceType" -> "Other",
      "endDate" -> "2011-01-01T00:00:00",
      "role" -> "Reviewer, documentary film proposals",
      "description" -> "Service to the Community",
      "hostOrganization" -> "Outreach Host Organization",
      "endDatePrecision" -> "http://vivoweb.org/ontology/core#yearPrecision",
      "locationOrVenue" -> "Washington, DC",
      "startDate" -> "2011-03-01T00:00:00",
      "geoFocus" -> "Azerbaijan"
      )
  }

  def presentation = {
    val presentation = professionalActivities.find(
      {presentation => presentation("vivoType") ==
          "http://vivo.duke.edu/vivo/ontology/duke-activity-extension#Presentation"}).
      getOrElse(throw new RuntimeException("Presentation not found."))
    presentation must havePairs(
      "uri" -> "http://localhost/individual/presentation15485",
      "label" -> "“Riding the New Wave: Mexican Feminism and the Politics of International Women’s Year”. Presentation Event Name. Presentation Host Organization. December 2, 2011 - October 2018"
      )
  }

  def presentationAttrs = {
    val presentation = professionalActivities.find(
      {presentation => presentation("vivoType") ==
          "http://vivo.duke.edu/vivo/ontology/duke-activity-extension#Presentation"}).
      getOrElse(throw new RuntimeException("Presentation not found."))
    val presentationAttrs = presentation("attributes").asInstanceOf[Map[String, Any]]
    presentationAttrs must havePairs(
      "startDatePrecision" -> "http://vivoweb.org/ontology/core#yearMonthDayPrecision",
      "serviceOrEventName" -> "Presentation Event Name",
      "serviceType" -> "Lecture",
      "endDate" -> "2018-10-01T00:00:00",
      "nameOfTalk" -> "“Riding the New Wave: Mexican Feminism and the Politics of International Women’s Year”",
      "description" -> "Invited Lectures ; Jocelyn Olcott",
      "hostOrganization" -> "Presentation Host Organization",
      "endDatePrecision" -> "http://vivoweb.org/ontology/core#yearMonthPrecision",
      "locationOrVenue" -> "Yale University",
      "startDate" -> "2011-12-02T00:00:00"
      )
  }

  def serviceToProf = {
    val serviceToProf = professionalActivities.find(
      {serviceToProf => serviceToProf("vivoType") ==
          "http://vivo.duke.edu/vivo/ontology/duke-activity-extension#ServiceToTheProfession"}).
      getOrElse(throw new RuntimeException("ServiceToProf not found."))
    serviceToProf must havePairs(
      "uri" -> "http://localhost/individual/servicetoprof24520",
      "label" -> "Event/Organization Administration. Server to the Profession. Name of Service to Prof. Organization for Serving Profession. 2019 - 2020"
      )
  }

  def serviceToProfAttrs = {
    val serviceToProf = professionalActivities.find(
      {serviceToProf => serviceToProf("vivoType") ==
          "http://vivo.duke.edu/vivo/ontology/duke-activity-extension#ServiceToTheProfession"}).
      getOrElse(throw new RuntimeException("ServiceToProf not found."))
    val serviceToProfAttrs = serviceToProf("attributes").asInstanceOf[Map[String, Any]]
    serviceToProfAttrs must havePairs(
      "startDatePrecision" -> "http://vivoweb.org/ontology/core#yearPrecision",
      "serviceOrEventName" -> "Name of Service to Prof",
      "serviceType" -> "Event/Organization Administration",
      "endDate" -> "2020-01-01T00:00:00",
      "role" -> "Server to the Profession",
      "description" -> "Describing service to profession.",
      "hostOrganization" -> "Organization for Serving Profession",
      "endDatePrecision" -> "http://vivoweb.org/ontology/core#yearPrecision",
      "locationOrVenue" -> "Venue to Serve the Profession",
      "startDate" -> "2019-01-01T00:00:00"
      )
  }

  def serviceToUniv = {
    val serviceToUniv = professionalActivities.find(
      {serviceToUniv => serviceToUniv("vivoType") ==
          "http://vivo.duke.edu/vivo/ontology/duke-activity-extension#ServiceToTheUniversity"}).
      getOrElse(throw new RuntimeException("ServiceToUniv not found."))
    serviceToUniv must havePairs(
      "uri" -> "http://localhost/individual/serviceto_univ24820",
      "label" -> "Committee Service. Duke University. The Big Committee. May 13, 2013 - October 30, 2018"
      )
  }

  def serviceToUnivAttrs = {
    val serviceToUniv = professionalActivities.find(
      {serviceToUniv => serviceToUniv("vivoType") ==
          "http://vivo.duke.edu/vivo/ontology/duke-activity-extension#ServiceToTheUniversity"}).
      getOrElse(throw new RuntimeException("ServiceToUniv not found."))
    val serviceToUnivAttrs = serviceToUniv("attributes").asInstanceOf[Map[String, Any]]
    serviceToUnivAttrs must havePairs(
      "serviceType" -> "Committee Service",
      "startDatePrecision" -> "http://vivoweb.org/ontology/core#yearMonthDayPrecision",
      "committeeType" -> "School",
      "endDate" -> "2018-10-30T00:00:00",
      "description" -> "Description of committee service at Duke.",
      "hostOrganization" -> "Duke University",
      "endDatePrecision" -> "http://vivoweb.org/ontology/core#yearMonthDayPrecision",
      "locationOrVenue" -> "Duke Campus",
      "committeeName" -> "The Big Committee",
      "startDate" -> "2013-05-13T00:00:00"
      )
  }
}
