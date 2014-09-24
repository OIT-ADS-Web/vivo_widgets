package edu.duke.oit.vw.models

import edu.duke.oit.vw.utils._
import edu.duke.oit.vw.solr.Vivo

case class Presentation(uri:String,
                 vivoType: String,
                 label:  String,
                 attributes:Option[Map[String, String]])
     extends VivoAttributes(uri, vivoType, label, attributes) with AddToJson 
{

  override def uris():List[String] = {
    uri :: super.uris
  }

}

object Presentation extends AttributeParams {

  def fromUri(vivo: Vivo, uriContext:Map[String, Any], 
              templatePath: String="sparql/presentations.ssp") = {
    val presentationData = vivo.selectFromTemplate(templatePath, uriContext)
    presentationData.map(build(_)).asInstanceOf[List[Presentation]]

  }

  def build(presentation:Map[Symbol,String]) = {
    new Presentation(uri  = presentation('presentation).stripBrackets(),
              vivoType    = presentation('type).stripBrackets(),
              label       = presentation('label),
              attributes  = parseAttributes(presentation, List('presentation,'type,'label)))
  }

}
