package edu.duke.oit.vw.models

import edu.duke.oit.vw.utils._
import edu.duke.oit.vw.solr.Vivo

case class ArtisticEvent(uri:String,
                        vivoType: String,
                        label:  String,
                        attributes:Option[Map[String, String]])
     extends VivoAttributes(uri, vivoType, label, attributes) with AddToJson 
{

  override def uris():List[String] = {
    uri :: super.uris
  }

}

object ArtisticEvent extends AttributeParams {

  def fromUri(vivo: Vivo, uriContext:Map[String, Any], templatePath: String="sparql/artistic_events.ssp") = {
    val data  = vivo.selectFromTemplate(templatePath, uriContext)
    val existingData = data.filter(datum => !datum.isEmpty)
    existingData.map(build(_)).asInstanceOf[List[ArtisticEvent]]
  }


  def build(artisticEvent:Map[Symbol,String]) = {
    new ArtisticEvent(uri  = artisticEvent('event).stripBrackets(),
              vivoType     = artisticEvent('type).stripBrackets(),
              label        = artisticEvent('label),
              attributes   = parseAttributes(artisticEvent, List('event,'type,'label)))
  }

}
