package edu.duke.oit.vw.models

import edu.duke.oit.vw.utils._
import edu.duke.oit.vw.solr.Vivo

case class Publication(uri:String,
                       vivoType:String,
                       label:String,
                       attributes:Option[Map[String, String]]) 
     extends VivoAttributes(uri, vivoType, label, attributes) with AddToJson
{

  override def uris() = {
    uri :: super.uris
  }

  override def officialDateKey = {
    "year"
  }
  

  def subType = {
    val regex = "(.*)/([a-zA-Z0-9.\\-]+)(#)?([a-zA-Z0-9.\\-]+)?".r
    val tmpType = vivoType match {
      case regex(_,subType1,_,subType2) => if(subType2 == null) subType1 else subType2
      case _ => this.getClass.getName
    }
    validType(tmpType)
  }

  protected def validType(subType:String) = {
    // There needs to be matching jade templates for each of these.
    Set("AcademicArticle", "Article", "Book", "Chapter", "ConferencePaper").contains(subType) match {
      case true => subType
      case _ => "Article"
    }
  }

}

object Publication extends AttributeParams {

  def fromUri(vivo: Vivo, uriContext:Map[String, Any]) = {
    val data  = vivo.selectFromTemplate("sparql/publications.ssp", uriContext)
    val existingData = data.filter(datum => !datum.isEmpty)
    existingData.map(build(_)).asInstanceOf[List[Publication]]
  }

  def build(pub:Map[Symbol,String]) = {
    new Publication(uri        = pub('publication).stripBrackets(),
                    vivoType   = pub('type).stripBrackets(),
                    label      = pub('label),
                    attributes = parseAttributes(pub,List('publication,'type,'label)))
  }

}
