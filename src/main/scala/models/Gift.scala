package edu.duke.oit.vw.models

import edu.duke.oit.vw.utils._
import edu.duke.oit.vw.solr.Vivo
import java.util.Date
import java.text.SimpleDateFormat

case class Gift(uri:String,
                  vivoType: String,
                  label: String,
                  attributes:Option[Map[String, String]])
     extends VivoAttributes(uri, vivoType, label, attributes) with AddToJson
{

  override def uris():List[String] = {
    uri :: super.uris
  }

}

object Gift extends AttributeParams {

  def fromUri(vivo: Vivo, uriContext:Map[String, Any]) = {
    val data  = vivo.selectFromTemplate("sparql/gifts.ssp", uriContext)
    val existingData = data.filter(datum => !datum.isEmpty)
    existingData.map(build(_)).asInstanceOf[List[Gift]]
  }

  def build(gift:Map[Symbol,String]) = {
    new Gift(uri            = gift('gift).stripBrackets(),
                 vivoType   = gift('type).stripBrackets(),
                 label      = gift('label),
                 attributes = parseAttributes(gift, List('gift,'type,'label)))
  }

}

