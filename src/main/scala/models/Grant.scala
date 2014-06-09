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
    inTimePeriod("startDate", start, end) ||
    inTimePeriod("endDate", start, end)
  }

  def inTimePeriod(dateAttribute: String, start: Date, end: Date): Boolean = {
    val dateString = get(dateAttribute)

    if (dateString == null) {
      false
    } else {
      val date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(dateString)
      (date.after(start) || date.equals(start)) &&
      (date.before(end) || date.equals(end))
    }
  }

}

object Grant extends AttributeParams {

  def fromUri(vivo: Vivo, uriContext:Map[String, Any], 
              templatePath: String="sparql/grants.ssp") = {
    val grantData = vivo.selectFromTemplate(templatePath, uriContext)
    grantData.map(build(_)).asInstanceOf[List[Grant]]

  }

  def build(grant:Map[Symbol,String]) = {
    new Grant(uri         = grant('agreement).stripBrackets(),
              vivoType    = grant('type).stripBrackets(),
              label       = grant('label),
              attributes  = parseAttributes(grant, List('agreement,'type,'label)))
  }

}
