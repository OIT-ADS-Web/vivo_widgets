package edu.duke.oit.vw.solr

import org.apache.solr.client.solrj.SolrServer
import edu.duke.oit.vw.utils._

object Person extends SolrModel with ExtraParams {

  def find(uri: String, solr: SolrServer): Option[Person] = {
    getDocumentById(uri,solr) match {
      case Some(sd) => Option(PersonExtraction(sd.get("json").toString))
      case _ => None
    }
  }

  def build(uri:String, personData:Map[Symbol,String], pubs:List[Publication], grants:List[Grant], courses:List[Course]): Person = {
    new Person(uri,
               vivoType = personData('type).stripBrackets(),
               name     = personData('name),
               title    = personData('title),
               publications = pubs,
               grants = grants,
               courses = courses,
               extraItems = parseExtraItems(personData, List('type,'name,'title)))
  }
}

case class Person(uri:String,
                  vivoType:String,
                  name:String,
                  title:String,
                  publications:List[Publication],
                  grants:List[Grant],
                  courses:List[Course],
                  extraItems:Option[Map[String, String]])
     extends ExtraItems(extraItems) with AddToJson
{

  override def uris() = {
    (uri :: super.uris) ++ 
    publications.foldLeft(List[String]()) {(u,publication) => u ++ publication.uris} ++
    grants.foldLeft(List[String]()) {(u,grant) => u ++ grant.uris} ++
    courses.foldLeft(List[String]()) {(u,course) => u ++ course.uris}
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
