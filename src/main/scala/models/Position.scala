package edu.duke.oit.vw.models

import edu.duke.oit.vw.utils._
import edu.duke.oit.vw.solr.Vivo

case class Position(uri:String,
                    vivoType: String,
                    label: String,
                    attributes:Option[Map[String, String]])
     extends VivoAttributes(uri, vivoType, label, attributes) with AddToJson 
{

  override def uris():List[String] = {
    uri :: super.uris
  }

}

object Position extends AttributeParams {

  def fromUri(vivo: Vivo, uriContext:Map[String, Any], templatePath: String="sparql/positions.ssp") = {
    val data  = vivo.selectFromTemplate(templatePath, uriContext)
    val existingData = data.filter(datum => !datum.isEmpty)
    existingData.map(build(_)).asInstanceOf[List[Position]]
  }

  def build(position:Map[Symbol,String]) = {
    new Position(uri         = position('uri).stripBrackets(),
                 vivoType    = position('type).stripBrackets(),
                 label       = position('label),
                 attributes  = parseAttributes(position, List('uri,'type,'label)))
  }

}
