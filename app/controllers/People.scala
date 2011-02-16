package controllers

import play._
import play.mvc._
import play.templates._

import models._

object People extends Controller {

  def publicationsStub(vivoId: String, items: Int) = {
    Publication.findAllForPerson(Vivo.baseUri+vivoId,items) match {
      case Some(publications) => Json(new VivoWidgetJsonResult(publications.asInstanceOf[List[Publication]]).jsonp)
      case _ => NoContent
    }
  }

  def publications(vivoId: String, items: Int) = {
    Publication.findAllForPerson(Vivo.baseUri+vivoId,items) match {
      case Some(publications) =>
        val modelData = new java.util.HashMap[java.lang.String,java.lang.Object]
        modelData.put("publications",publications)
        val htmlString = TemplateLoader.load("People/publications.html").render(modelData)
        request.format.toString match {
          case "js" => 
            val lines = htmlString.split('\n').toList
            val documentWrites = lines.map { "document.write('"+_+"')" }
            val documentWritesString = documentWrites.mkString("\n")
            Json(documentWritesString)

          case "html" => Html(htmlString)
          case _ => NoContent
        }
        // TODO: try make this pure Scala, something like:
        //val strR: String =  "People/publications.html".asTemplate("publications" -> publications).getContent()
      case _ => NoContent
    }
  }

}


