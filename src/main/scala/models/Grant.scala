package edu.duke.oit.vw.models

import edu.duke.oit.vw.utils._
import edu.duke.oit.vw.solr.Vivo
import java.util.Date
import java.text.SimpleDateFormat

case class Grant(uri:String,
                 vivoType: String,
                 label: String,
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
    } else if (endDateString.length() == 10) {
      val endDate = new SimpleDateFormat("yyyy-MM-dd").parse(endDateString)
      (start.before(endDate) || start.equals(endDate))
    } else {
      val endDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(endDateString)
      (start.before(endDate) || start.equals(endDate))
    }
  }

  def endAfterStartOfItem(end: Date) = {
    val startDateString = get("startDate")
    if (startDateString == null) {
      true
    } else if (startDateString.length() == 10) {
      val startDate = new SimpleDateFormat("yyyy-MM-dd").parse(startDateString)
      (end.after(startDate) || end.equals(startDate))
    } else {
      val startDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(startDateString)
      (end.after(startDate) || end.equals(startDate))
    }
  }

}

object Grant extends AttributeParams {

  def fromUri(vivo: Vivo, uriContext:Map[String, Any], templatePath: String="sparql/grants.ssp") = {
    val data  = vivo.selectFromTemplate(templatePath, uriContext)
    val existingData = data.filter(datum => !datum.isEmpty)
    existingData.map(build(_)).asInstanceOf[List[Grant]]
  }


  def build(grant:Map[Symbol,String]) = {
    new Grant(uri         = grant('agreement).stripBrackets(),
              vivoType    = grant('type).stripBrackets(),
              label       = grant('label),
              attributes  = parseAttributes(grant, List('agreement,'type,'label)))
  }

}
