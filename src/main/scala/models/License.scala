package edu.duke.oit.vw.models

import edu.duke.oit.vw.utils._
import edu.duke.oit.vw.solr.Vivo

case class License(uri:String,
                  vivoType: String,
                  label: String,
                  attributes:Option[Map[String, String]])
     extends VivoAttributes(uri, vivoType, label, attributes) with AddToJson
{

  override def uris():List[String] = {
    uri :: super.uris
  }

}

object License extends AttributeParams {

  def fromUri(vivo: Vivo, uriContext:Map[String, Any]) = {
    val data  = vivo.selectFromTemplate("sparql/licenses.ssp", uriContext)
    val existingData = data.filter(datum => !datum.isEmpty)
    existingData.map(build(_)).asInstanceOf[List[License]]
  }

  def build(license:Map[Symbol,String]) = {
    new License(uri         = license('license).stripBrackets(),
                 vivoType   = license('type).stripBrackets(),
                 label      = license('label),
                 attributes = parseAttributes(license, List('license,'type,'label)))
  }

}