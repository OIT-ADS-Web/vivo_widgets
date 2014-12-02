package edu.duke.oit.vw.models

import edu.duke.oit.vw.utils._
import edu.duke.oit.vw.solr.Vivo

case class PersonReference(uri:String,
                           vivoType:String,
                           label:String,
                           title:String,
                           attributes:Option[Map[String, String]])
     extends VivoAttributes(uri, vivoType, label, attributes) with AddToJson
{

  override def uris():List[String] = {
    uri :: super.uris
  }

}

object PersonReference extends AttributeParams {

  def fromUri(vivo: Vivo, uriContext:Map[String, Any], templatePath: String="sparql/organization/people.ssp") = {
    val data  = vivo.selectFromTemplate(templatePath, uriContext)
    val items = data.map(build(_))
    items.groupBy{_.uri}.map{_._2.head}.asInstanceOf[List[PersonReference]]
  }


  def build(person:Map[Symbol,String]) = {
    new PersonReference(uri         = person('person).stripBrackets(),
                        vivoType    = person('type).stripBrackets(),
                        label       = person('label),
                        title       = person('title),
                        attributes  = parseAttributes(person, List('person, 'type, 'label, 'title)))
  }

}

