package edu.duke.oit.vw.models

import edu.duke.oit.vw.utils._
import edu.duke.oit.vw.solr.Vivo

case class Webpage(uri:String,
                   vivoType: String,
                   label: String,
                   attributes:Option[Map[String, String]])
     extends VivoAttributes(uri, vivoType, label, attributes) with AddToJson
{

  override def uris():List[String] = {
    uri :: super.uris
  }

}

object Webpage extends AttributeParams {

  def fromUri(vivo: Vivo, uriContext:Map[String, Any], templatePath: String="sparql/webpages.ssp") = {
    val data  = vivo.selectFromTemplate(templatePath, uriContext)
    val existingData = data.filter(datum => !datum.isEmpty)
    existingData.map(build(_)).asInstanceOf[List[Webpage]]
  }


  def build(webpage:Map[Symbol,String]) = {
    new Webpage(uri         = webpage('uri).stripBrackets(),
                vivoType    = webpage('type).stripBrackets(),
                label       = webpage('label),
                attributes  = parseAttributes(webpage, List('uri,'type,'label)))
  }

}
