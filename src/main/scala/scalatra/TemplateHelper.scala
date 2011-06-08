package edu.duke.oit.vw.scalatra

import edu.duke.oit.vw.utils.ElvisOperator

object TemplateHelpers extends ElvisOperator {
  import org.fusesource.scalate.RenderContext.capture
  
  val templateDir = "/WEB-INF/scalate/templates/"
  def tpath(template:String) = templateDir + template
  def empty(value: String) = if ((value ?: "") == "") true else false
  def notEmpty(value: String)(body: => Unit) = {
    if ((value ?: "") != "") {
      capture(body)
    }
  }
}
