package edu.duke.oit.vw.models

import edu.duke.oit.vw.utils._
import edu.duke.oit.vw.solr.Vivo

case class Education(uri:String,
                     vivoType: String,
                     label: String,
                     attributes:Option[Map[String, String]])
     extends VivoAttributes(uri, vivoType, label, attributes) with AddToJson
{

  override def uris():List[String] = {
    uri :: super.uris
  }

}

object Education extends AttributeParams {

  def fromUri(vivo: Vivo, uriContext:Map[String, Any], templatePath: String="sparql/educations.ssp") = {
    val data  = vivo.selectFromTemplate(templatePath, uriContext)
    val items = data.map(build(_))
    items.groupBy{_.uri}.map{_._2.head}.asInstanceOf[List[Education]]
  }


  def build(education:Map[Symbol,String]) = {
    new Education(uri         = education('uri).stripBrackets(),
                  vivoType    = education('type).stripBrackets(),
                  label       = education('label),
                  attributes  = parseAttributes(education, List('uri,'type,'label)))
  }

}
