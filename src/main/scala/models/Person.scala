package edu.duke.oit.vw.models

import org.apache.solr.client.solrj.SolrServer
import edu.duke.oit.vw.utils._

import java.util.Date

import org.slf4j.{Logger, LoggerFactory}

object Person extends SolrModel 
  with AttributeParams {

  def find(uri: String, solr: SolrServer): Option[Person] = {
    getDocumentByIdOrAlternateId(uri,solr) match {
      case Some(sd) => Option(PersonExtraction(sd.get("json").toString))
      case _ => None
    }
  }

  // NOTE: person and organization have Option[Date] instead of Date for updatedAt because some people exist
  // in the index preceding the existence of that field.  If it is not Option[], it returns an error
  def build(uri:String,
            active:Option[Boolean],
            updatedAt:Option[Date],
            personData:Map[Symbol,String],
            pubs:List[Publication],
            awards:List[Award],
            artisticWorks:List[ArtisticWork],
            artisticEvents:List[ArtisticEvent],
            grants:List[Grant],
            courses:List[Course],
            professionalActivities:List[ProfessionalActivity],
            positions:List[Position],
            addresses:List[Address],
            educations:List[Education],
            researchAreas:List[ResearchArea],
            webpages:List[Webpage],
            geographicalFocus:List[GeographicFocus],
            newsfeeds:List[Newsfeed],
            cvInfo:Option[PersonCVInfo]): Person = {
    new Person(uri,
               active,
               updatedAt,
               vivoType           = personData('type).stripBrackets(),
               label              = personData('label),
               title              = personData('title),
               publications       = pubs,
               awards             = awards,
               artisticWorks      = artisticWorks,
               artisticEvents     = artisticEvents,
               grants             = grants,
               courses            = courses,
               professionalActivities = professionalActivities,
               positions              = positions,
               addresses              = addresses,
               educations             = educations,
               researchAreas          = researchAreas,
               webpages               = webpages,
               geographicalFocus      = geographicalFocus,
               newsfeeds              = newsfeeds,
               cvInfo                 = cvInfo,
               attributes             = parseAttributes(personData, List('type,'label,'title)))
  }

}


case class PersonCVInfo(gifts:List[Gift],
                        academicPositions:List[AcademicPosition],
                        licenses:List[License],
                        pastAppointments:List[PastAppointment]) {

}


case class Person(uri:String,
                  active:Option[Boolean],
                  updatedAt:Option[Date],
                  vivoType:String,
                  label:String,
                  title:String,
                  publications:List[Publication],
                  awards:List[Award],
                  artisticWorks:List[ArtisticWork],
                  artisticEvents:List[ArtisticEvent],
                  grants:List[Grant],
                  courses:List[Course],
                  professionalActivities:List[ProfessionalActivity],
                  positions:List[Position],
                  addresses:List[Address],
                  educations:List[Education],
                  researchAreas:List[ResearchArea],
                  webpages:List[Webpage],
                  geographicalFocus:List[GeographicFocus],
                  newsfeeds:List[Newsfeed],
                  cvInfo: Option[PersonCVInfo],
                  attributes:Option[Map[String, String]])
     extends VivoAttributes(uri, vivoType, label, attributes) 
     with AddToJson
     with Timestamped
{

  val log =  LoggerFactory.getLogger(getClass)

  override def uris() = {
    var results = (uri :: super.uris) ++
    publications.foldLeft(List[String]()) {(u,publication) => u ++ publication.uris} ++
    awards.foldLeft(List[String]()) {(u,award) => u ++ award.uris} ++
    artisticWorks.foldLeft(List[String]()) {(u,artisticWork) => u ++ artisticWork.uris} ++
    artisticEvents.foldLeft(List[String]()) {(u,artisticEvent) => u ++ artisticEvent.uris} ++
    grants.foldLeft(List[String]()) {(u,grant) => u ++ grant.uris} ++
    courses.foldLeft(List[String]()) {(u,course) => u ++ course.uris} ++
    professionalActivities.foldLeft(List[String]()) {(u,professionalActivity) => u ++ professionalActivity.uris} ++
    positions.foldLeft(List[String]()) {(u,position) => u ++ position.uris} ++
    addresses.foldLeft(List[String]()) {(u,address) => u ++ address.uris} ++
    educations.foldLeft(List[String]()) {(u,education) => u ++ education.uris} ++
    researchAreas.foldLeft(List[String]()) {(u,area) => u ++ area.uris} ++
    webpages.foldLeft(List[String]()) {(u,page) => u ++ page.uris} ++
    geographicalFocus.foldLeft(List[String]()) {(u,focus) => u ++ focus.uris} ++
    newsfeeds.foldLeft(List[String]()) {(u,newsfeed) => u ++ newsfeed.uris }
    
    if (cvInfo.isDefined) {
      log.debug("cvInfo.isDefined")
      results = results ++ cvInfo.get.gifts.foldLeft(List[String]()) { (u, gift) => u ++ gift.uris }
      results = results ++ cvInfo.get.academicPositions.foldLeft(List[String]()) {(u,academicPosition) => u ++ academicPosition.uris}
      results = results ++ cvInfo.get.licenses.foldLeft(List[String]()) {(u,license) => u ++ license.uris}
      results = results ++ cvInfo.get.pastAppointments.foldLeft(List[String]()) {(u,PastAppointment) => u ++ PastAppointment.uris}
    }  
  
    results
  }

  def personAttributes() = {
    this.attributes match {
      case Some(attributes) => attributes ++ Map("uri" -> this.uri)
      case _ => Map("uri" -> this.uri)
    }
  }

}

/**
 * Wraps the lift-json parsing and extraction of a person.
 */
object PersonExtraction {
  def apply(json:String) = {
    import net.liftweb.json._
    // Brings in default date formats etc.
    implicit val formats = DefaultFormats

    val j = JsonParser.parse(json)
    j.extract[Person]
  }
}
