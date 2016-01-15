package edu.duke.oit.vw.models

import edu.duke.oit.vw.utils._
import edu.duke.oit.vw.solr.Vivo

case class Newsfeed(uri:String,
                  vivoType: String,
                  label: String,
                  attributes:Option[Map[String, String]])
     extends VivoAttributes(uri, vivoType, label, attributes) with AddToJson
{

  override def uris():List[String] = {
    uri :: super.uris
  }

}

object Newsfeed extends AttributeParams {

  def fromUri(vivo: Vivo, uriContext:Map[String, Any]) = {
    val data  = vivo.selectFromTemplate("sparql/newsfeeds.ssp", uriContext)
    val existingData = data.filter(datum => !datum.isEmpty)
    existingData.map(build(_)).asInstanceOf[List[Newsfeed]]
  }

  def build(newsfeed:Map[Symbol,String]) = {
    new Newsfeed(uri        = newsfeed('newsfeed).stripBrackets(),
                 vivoType   = newsfeed('type).stripBrackets(),
                 label      = newsfeed('label),
                 attributes = parseAttributes(newsfeed, List('newsfeed,'type,'label)))
  }

}
