package edu.duke.oit.vw.models

import edu.duke.oit.vw.utils._
import edu.duke.oit.vw.solr.Vivo

case class ArtisticWork(uri:String,
                        vivoType: String,
                        label:  String,
                        attributes:Option[Map[String, String]])
     extends VivoAttributes(uri, vivoType, label, attributes) with AddToJson 
{

  override def uris():List[String] = {
    uri :: super.uris
  }

}

object ArtisticWork extends AttributeParams {

  def fromUri(vivo: Vivo, uriContext:Map[String, Any], 
              templatePath: String="sparql/artistic_works.ssp") = {
    val artisticWorkData = vivo.selectFromTemplate(templatePath, uriContext)
    artisticWorkData.map(build(_)).asInstanceOf[List[ArtisticWork]]

  }

  def build(artisticWork:Map[Symbol,String]) = {
    new ArtisticWork(uri  = artisticWork('work).stripBrackets(),
              vivoType    = artisticWork('type).stripBrackets(),
              label       = artisticWork('label),
              attributes  = parseAttributes(artisticWork, List('work,'type,'label)))
  }

}
