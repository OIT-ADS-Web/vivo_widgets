package edu.duke.oit.vw.solr

import org.apache.solr.client.solrj.SolrServer
import edu.duke.oit.vw.utils._

object Organization extends SolrModel with ExtraParams {

  def find(uri: String, solr: SolrServer): Option[Organization] = {
    getDocumentById(uri,solr) match {
      case Some(sd) => Option(OrganizationExtraction(sd.get("json").toString))
      case _ => None
    }
  }

  def build(uri:String, orgData:Map[Symbol,String], people:List[PersonReference], grants:List[Grant]): Organization = {
    new Organization(uri,
                     vivoType    = orgData('type).stripBrackets(),
                     name        = orgData('name),
                     people      = people,
                     grants      = grants,
                     extraItems  = parseExtraItems(orgData,List('type,'name)))
  }

}

case class Organization(uri:String,
                  vivoType:String,
                  name:String,
                  people:List[PersonReference],
                  grants:List[Grant],
                  extraItems:Option[Map[String, String]])
     extends ExtraItems(extraItems) with AddToJson
{

  override def uris() = {
    (uri :: super.uris) ++ 
    people.foldLeft(List[String]()) {(u,publication) => u ++ publication.uris} ++
    grants.foldLeft(List[String]()) {(u,grant) => u ++ grant.uris}
  }

}

/**
 * Wraps the lift-json parsing and extraction of an organization.
 */
object OrganizationExtraction {
  def apply(json:String) = {
    import net.liftweb.json._
    // Brings in default date formats etc.
    implicit val formats = DefaultFormats 

    val j = JsonParser.parse(json)
    j.extract[Organization]
  }
}
