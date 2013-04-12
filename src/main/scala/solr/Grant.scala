package edu.duke.oit.vw.solr

import edu.duke.oit.vw.utils._

case class Grant(uri:String,
                 vivoType: String,
                 label: String,
                 attributes:Option[Map[String, String]])
     extends VivoAttributes(uri, vivoType, label, attributes) with AddToJson 
{

  override def uris():List[String] = {
    uri :: super.uris
  }

}

object Grant extends AttributeParams {

  def fromUri(vivo: Vivo, uriContext:Map[String, Any], 
              useCache: Boolean = false, templatePath: String="sparql/grants.ssp") = {
    val grantData = vivo.selectFromTemplate(templatePath, uriContext, useCache)
    grantData.map(build(_)).asInstanceOf[List[Grant]]

  }

  def build(grant:Map[Symbol,String]) = {
    new Grant(uri         = grant('agreement).stripBrackets(),
              vivoType    = grant('type).stripBrackets(),
              label       = grant('grantName),
              attributes  = parseAttributes(grant, List('agreement,'type,'grantName)))
  }

}
