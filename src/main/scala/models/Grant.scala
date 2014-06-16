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
    startBeforeEndOfGrant(start) && endAfterStartOfGrant(end)
  }

  def startBeforeEndOfGrant(start: Date) = {
    val endDateString = get("endDate")
    if (endDateString == null) {
      true
    } else {
      val endDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(endDateString)
      (start.before(endDate) || start.equals(endDate))
    }
  }

  def endAfterStartOfGrant(end: Date) = {
    val startDateString = get("startDate")
    if (startDateString == null) {
      true
    } else {
      val startDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(startDateString)
      (end.after(startDate) || end.equals(startDate))
    }
  }

}

object Grant extends AttributeParams {

  def fromUri(vivo: Vivo, uriContext:Map[String, Any], 
              templatePath: String="sparql/grants.ssp") = {
    val grantData = vivo.selectFromTemplate(templatePath, uriContext)
    val existingGrantData = grantData.filter(grant => !grant.isEmpty)
    existingGrantData.map(build(_)).asInstanceOf[List[Grant]]

  }

  def build(grant:Map[Symbol,String]) = {
    new Grant(uri         = grant('agreement).stripBrackets(),
              vivoType    = grant('type).stripBrackets(),
              label       = grant('label),
              attributes  = parseAttributes(grant, List('agreement,'type,'label)))
  }

}
