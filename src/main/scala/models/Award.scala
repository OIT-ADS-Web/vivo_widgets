package edu.duke.oit.vw.models

import edu.duke.oit.vw.utils._
import edu.duke.oit.vw.solr.Vivo

case class Award(uri:String,
                 vivoType: String,
                 label:  String,
                 attributes:Option[Map[String, String]])
     extends VivoAttributes(uri, vivoType, label, attributes) with AddToJson 
{

  override def uris():List[String] = {
    uri :: super.uris
  }

}

object Award extends AttributeParams {

  def fromUri(vivo: Vivo, uriContext:Map[String, Any], templatePath: String="sparql/awards.ssp") = {
    val data  = vivo.selectFromTemplate(templatePath, uriContext)
    val items = data.map(build(_))
    items.groupBy{_.uri}.map{_._2.head}.asInstanceOf[List[Award]]
  }


  def build(award:Map[Symbol,String]) = {
    new Award(uri  = award('award).stripBrackets(),
              vivoType    = award('type).stripBrackets(),
              label       = award('label),
              attributes  = parseAttributes(award, List('award,'type,'label)))
  }

}
