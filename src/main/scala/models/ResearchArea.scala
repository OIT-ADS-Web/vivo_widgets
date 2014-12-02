package edu.duke.oit.vw.models

import edu.duke.oit.vw.utils._
import edu.duke.oit.vw.solr.Vivo

case class ResearchArea(uri:String,
                        vivoType: String,
                        label: String,
                        attributes:Option[Map[String, String]])
     extends VivoAttributes(uri, vivoType, label, attributes) with AddToJson
{

  override def uris():List[String] = {
    uri :: super.uris
  }

}

object ResearchArea extends AttributeParams {

  def fromUri(vivo: Vivo, uriContext:Map[String, Any], templatePath: String="sparql/researchAreas.ssp") = {
    val data  = vivo.selectFromTemplate(templatePath, uriContext)
    val items = data.map(build(_))
    items.groupBy{_.uri}.map{_._2.head}.asInstanceOf[List[ResearchArea]]
  }


  def build(researchArea:Map[Symbol,String]) = {
    new ResearchArea(uri         = researchArea('uri).stripBrackets(),
                     vivoType    = researchArea('type).stripBrackets(),
                     label       = researchArea('label),
                     attributes  = parseAttributes(researchArea, List('uri,'type,'label)))
  }

}
