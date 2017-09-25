package edu.duke.oit.vw.models

import edu.duke.oit.vw.utils._
import edu.duke.oit.vw.solr.Vivo

case class PastAppointment(uri:String,
                    vivoType: String,
                    label: String,
                    attributes:Option[Map[String, String]])
     extends VivoAttributes(uri, vivoType, label, attributes) with AddToJson 
{

  override def uris():List[String] = {
    uri :: super.uris
  }

}

object PastAppointment extends AttributeParams {

  def fromUri(vivo: Vivo, uriContext:Map[String, Any], templatePath: String="sparql/past_appointments.ssp") = {
    val data  = vivo.selectFromTemplate(templatePath, uriContext)
    val existingData = data.filter(datum => !datum.isEmpty)
    existingData.map(build(_)).asInstanceOf[List[PastAppointment]]
  }

  def build(pastAppointment:Map[Symbol,String]) = {
    new PastAppointment(uri         = pastAppointment('uri).stripBrackets(),
                 vivoType    = pastAppointment('type).stripBrackets(),
                 label       = pastAppointment('label),
                 attributes  = parseAttributes(pastAppointment, List('uri,'type,'label)))
  }

}
