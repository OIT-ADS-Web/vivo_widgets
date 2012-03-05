package edu.duke.oit.vw.solr

import edu.duke.oit.vw.utils._


case class PersonReference(uri:String,
                  vivoType:String,
                  name:String,
                  title:String,
                  extraItems:Option[Map[String, String]])
     extends ExtraItems(extraItems) with AddToJson
{

  override def uris():List[String] = {
    uri :: super.uris
  }

}
