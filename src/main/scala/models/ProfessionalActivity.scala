package edu.duke.oit.vw.models

import edu.duke.oit.vw.utils._
import edu.duke.oit.vw.solr.Vivo

case class ProfessionalActivity(uri:String,
                 vivoType: String,
                 label:  String,
                 attributes:Option[Map[String, String]])
     extends VivoAttributes(uri, vivoType, label, attributes) with AddToJson 
{

  override def uris():List[String] = {
    uri :: super.uris
  }

}

object ProfessionalActivity extends AttributeParams {

  def fromUri(vivo: Vivo, uriContext:Map[String, Any], 
              templatePath: String="sparql/professional_activities.ssp") = {
    val professionalActivityData = vivo.selectFromTemplate(templatePath, uriContext)
    professionalActivityData.map(build(_)).asInstanceOf[List[ProfessionalActivity]]

  }

  def build(professionalActivity:Map[Symbol,String]) = {
    new ProfessionalActivity(uri  = professionalActivity('activity).stripBrackets(),
              vivoType    = professionalActivity('type).stripBrackets(),
              label       = professionalActivity('label),
              attributes  = parseAttributes(professionalActivity, List('activity,'type,'label)))
  }

}
