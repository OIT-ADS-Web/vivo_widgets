package edu.duke.oit.vw.solr

import edu.duke.oit.vw.utils._

case class Course(uri:String,
                  vivoType: String,
                  name: String,
                  attributes:Option[Map[String, String]])
     extends VivoAttributes(uri, vivoType, name, attributes) with AddToJson
{

  override def uris():List[String] = {
    uri :: super.uris
  }

}

object Course extends AttributeParams {

  def fromUri(vivo: Vivo, uriContext:Map[String, Any], useCache: Boolean = false) = {
    val courseData  = vivo.selectFromTemplate("sparql/courses.ssp", uriContext, useCache)
    courseData.map(build(_)).asInstanceOf[List[Course]]
  }

  def build(course:Map[Symbol,String]) = {
    new Course(uri        = course('course).stripBrackets(),
               vivoType   = course('type).stripBrackets(),
               name       = course('courseName),
               attributes = parseAttributes(course,List('course,'type,'courseName)))
  }

}
