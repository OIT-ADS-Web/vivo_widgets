package edu.duke.oit.vw.models

import edu.duke.oit.vw.utils._
import edu.duke.oit.vw.solr.Vivo

case class Address(uri:String,
                   vivoType: String,
                   label: String,
                   attributes:Option[Map[String, String]])
     extends VivoAttributes(uri, vivoType, label, attributes) with AddToJson
{

  override def uris():List[String] = {
    uri :: super.uris
  }

}

object Address extends AttributeParams {

  def fromUri(vivo: Vivo, uriContext:Map[String, Any], templatePath: String="sparql/addresses.ssp") = {
    val addressData = vivo.selectFromTemplate(templatePath, uriContext)
    addressData.map(build(_)).asInstanceOf[List[Address]]
  }

  def build(address:Map[Symbol,String]) = {
    new Address(uri         = address('uri).stripBrackets(),
                vivoType    = address('type).stripBrackets(),
                label       = address('label),
                attributes  = parseAttributes(address, List('uri,'type,'label)))
  }

}
