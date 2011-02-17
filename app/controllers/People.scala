package controllers

import play._
import play.mvc._
import play.templates._

import models._

object People extends Controller {

  def publicationsData(vivoId: String, items: Int) = {
    Publication.findAllForPerson(Vivo.baseUri+vivoId,items) match {
      case Some(publications) =>
        val vwr = new VivoWidgetJsonResult(publications.asInstanceOf[List[Publication]])
        request.format match {
          case "json" => Json(vwr.json.toString)
          case "jsonp" => Json(vwr.jsonp)
          case _ => NoContent
        }
      case _ => NoContent
    }
  }

  def publications(vivoId: String, items: Int, style: String = "yes") = {
    Publication.findAllForPerson(Vivo.baseUri+vivoId,items) match {
      case Some(publications) =>
        val modelData = new java.util.HashMap[java.lang.String,java.lang.Object]
        modelData.put("publications",publications)
        modelData.put("style",style)
        val htmlString = TemplateLoader.load("People/publications.html").render(modelData)
        request.format.toString match {
          case "js" => 
            val lines = htmlString.split('\n').toList
            val documentWrites = lines.map { "document.write('"+_+"');" }
            val documentWritesString = documentWrites.mkString("\n")
            Json(documentWritesString)
          case "html" => Html(htmlString)
          case _ => NoContent
        }
      case _ => NoContent
    }
  }

}


