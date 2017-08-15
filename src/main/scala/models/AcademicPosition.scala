package edu.duke.oit.vw.models

import edu.duke.oit.vw.utils._
import edu.duke.oit.vw.solr.Vivo

case class AcademicPosition(uri:String,
                    vivoType: String,
                    label: String,
                    attributes:Option[Map[String, String]])
     extends VivoAttributes(uri, vivoType, label, attributes) with AddToJson 
{

  override def uris():List[String] = {
    uri :: super.uris
  }

}

object AcademicPosition extends AttributeParams {

  def fromUri(vivo: Vivo, uriContext:Map[String, Any], templatePath: String="sparql/academic_positions.ssp") = {
    val data  = vivo.selectFromTemplate(templatePath, uriContext)
    val existingData = data.filter(datum => !datum.isEmpty)
    existingData.map(build(_)).asInstanceOf[List[AcademicPosition]]
  }

  def build(academicPosition:Map[Symbol,String]) = {
    new AcademicPosition(uri         = academicPosition('uri).stripBrackets(),
                 vivoType    = academicPosition('type).stripBrackets(),
                 label       = academicPosition('label),
                 attributes  = parseAttributes(academicPosition, List('uri,'type,'label)))
  }

}
