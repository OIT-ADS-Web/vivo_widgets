package edu.duke.oit.vw.models

import edu.duke.oit.vw.utils._
import edu.duke.oit.vw.solr.Vivo
import java.util.Date
import java.text.SimpleDateFormat

case class Presentation(uri:String,
                 vivoType: String,
                 label:  String,
                 attributes:Option[Map[String, String]])
     extends VivoAttributes(uri, vivoType, label, attributes) with AddToJson 
{

  override def uris():List[String] = {
    uri :: super.uris
  }

  override def withinTimePeriod(start: Date, end: Date): Boolean = {
    startBeforeEndOfItem(start) && endAfterStartOfItem(end)
  }

  def startBeforeEndOfItem(start: Date) = {
    val endDateString = get("endDate")
    if (endDateString == null) {
      true
    } else {
      val endDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(endDateString)
      (start.before(endDate) || start.equals(endDate))
    }
  }

  def endAfterStartOfItem(end: Date) = {
    val startDateString = get("startDate")
    if (startDateString == null) {
      true
    } else {
      val startDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(startDateString)
      (end.after(startDate) || end.equals(startDate))
    }
  }

}

object Presentation extends AttributeParams {

  def fromUri(vivo: Vivo, uriContext:Map[String, Any], 
              templatePath: String="sparql/presentations.ssp") = {
    val presentationData = vivo.selectFromTemplate(templatePath, uriContext)
    presentationData.map(build(_)).asInstanceOf[List[Presentation]]

  }

  def build(presentation:Map[Symbol,String]) = {
    new Presentation(uri  = presentation('presentation).stripBrackets(),
              vivoType    = presentation('type).stripBrackets(),
              label       = presentation('label),
              attributes  = parseAttributes(presentation, List('presentation,'type,'label)))
  }

}
