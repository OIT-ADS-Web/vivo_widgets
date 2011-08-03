package edu.duke.oit.vw.solr

import edu.duke.oit.vw.utils._

case class Publication(uri:String,
                       vivoType:String,
                       title:String,
                       authors:List[String],
                       extraItems:Option[Map[String, String]]) 
     extends ExtraItems(extraItems) with AddToJson
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
