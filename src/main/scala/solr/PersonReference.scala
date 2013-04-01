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

object PersonReference extends ExtraParams {

  def fromUri(vivo: Vivo, uriContext:Map[String, Any], 
              useCache: Boolean = false, templatePath: String="sparql/organization/people.ssp") = {
    val personReferenceData = vivo.selectFromTemplate(templatePath, uriContext, useCache)
    personReferenceData.map(build(_)).asInstanceOf[List[PersonReference]]

  }

  def build(person:Map[Symbol,String]) = {
    new PersonReference(uri         = person('person).stripBrackets(),
                        vivoType    = person('type).stripBrackets(),
                        name        = person('name),
                        title       = person('title),
                        extraItems  = parseExtraItems(person, List('person, 'type, 'name, 'title)))
  }

}

