package edu.duke.oit.vw.scalatra

import edu.duke.oit.vw.utils.ElvisOperator
//import edu.duke.oit.vw.scalatra.WidgetsConfig

object TemplateHelpers extends ElvisOperator {
  import org.fusesource.scalate.RenderContext.capture
  
  val templateDir = "/WEB-INF/scalate/templates/"
  def tpath(template:String) = templateDir + template
  def empty(value: String):Boolean = if ((value ?: "") == "") true else false
  def empty(valueOption: Option[String]):Boolean = {
    valueOption match {
      case Some(value:String) => empty(value)
      case _ => true
    }
  }
  def notEmpty(value: String)(body: => Unit) = {
    if ((value ?: "") != "") {
      capture(body)
    }
  }

  def fixURL(url:String) = {
    WidgetsConfig.baseProtocolAndDomain match {
      /* match the protocol://domain.com  */
      case Some(base) => url.replaceFirst("^(\\w+:\\/\\/)?[\\w+.]+(?=(\\/|:\\d+))", base)
      case _ => url
    }
    
  }
}
