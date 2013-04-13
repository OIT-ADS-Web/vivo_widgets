package edu.duke.oit.vw.solr

import edu.duke.oit.vw.utils._

case class Publication(uri:String,
                       vivoType:String,
                       label:String,
                       attributes:Option[Map[String, String]]) 
     extends VivoAttributes(uri, vivoType, label, attributes) with AddToJson
{

  override def uris() = {
    uri :: super.uris
  }

  def subType = {
    val regex = "(.*)/([a-zA-Z0-9.\\-]+)(#)?([a-zA-Z0-9.\\-]+)?".r
    vivoType match {
      case regex(_,subType1,_,subType2) => if(subType2 == null) subType1 else subType2
      case _ => this.getClass.getName
    }
  }
}

object Publication extends AttributeParams {

  def fromUri(vivo: Vivo, uriContext:Map[String, Any], useCache: Boolean = false) = {
    val publicationData  = vivo.selectFromTemplate("sparql/publications.ssp", uriContext, useCache)
    publicationData.map(build(vivo, _)).asInstanceOf[List[Publication]]
  }

  def build(vivo: Vivo, pub:Map[Symbol,String], useCache: Boolean=false) = {
    new Publication(uri        = pub('publication).stripBrackets(),
                    vivoType   = pub('type).stripBrackets(),
                    label      = pub('label),
                    attributes = parseAttributes(pub,List('publication,'type,'label)))
  }

}
