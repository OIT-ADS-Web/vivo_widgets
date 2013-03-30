package edu.duke.oit.vw.solr

import edu.duke.oit.vw.utils._

case class Grant(uri:String,
                 vivoType: String,
                 name: String,
                 extraItems:Option[Map[String, String]])
     extends ExtraItems(extraItems) with AddToJson 
{

  override def uris():List[String] = {
    uri :: super.uris
  }

}

object Grant extends ExtraParams {

  def fromUri(vivo: Vivo, uriContext:Map[String, Any], useCache: Boolean = false) = {
    val grantData = vivo.selectFromTemplate("sparql/grants.ssp", uriContext, useCache)
    grantData.map(build(_)).asInstanceOf[List[Grant]]

  }

  def build(grant:Map[Symbol,String]) = {
    new Grant(uri         = grant('agreement).replaceAll("<|>",""),
              vivoType    = grant('type).replaceAll("<|>",""),
              name        = grant('grantName),
              extraItems  = parseExtraItems(grant, List('agreement,'type,'grantName)))
  }

}
