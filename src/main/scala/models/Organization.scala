package edu.duke.oit.vw.models

import org.apache.solr.client.solrj.SolrServer
import edu.duke.oit.vw.utils._

import java.util.Date

object Organization extends SolrModel with AttributeParams {

  def find(uri: String, solr: SolrServer): Option[Organization] = {
    getDocumentById(uri,solr) match {
      case Some(sd) => Option(OrganizationExtraction(sd.get("json").toString))
      case _ => None
    }
  }

  def build(uri:String, updatedAt:Date, orgData:Map[Symbol,String], people:List[PersonReference], grants:List[Grant]): Organization = {
    new Organization(uri,
                     updatedAt,
                     vivoType    = orgData('type).stripBrackets(),
                     label       = orgData('label),
                     people      = people,
                     grants      = grants,
                     attributes  = parseAttributes(orgData,List('type,'name)))
  }

}

case class Organization(uri:String,
                        updatedAt:Date,
                        vivoType:String,
                        label:String,
                        people:List[PersonReference],
                        grants:List[Grant],
                        attributes:Option[Map[String, String]])
     extends VivoAttributes(uri, vivoType, label, attributes) with AddToJson
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
