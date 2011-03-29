package controllers

import play._
import play.mvc._
import play.templates._

import models.{SolrConnection,VivoConnection,VivoWidgetJsonResult}
import edu.duke.oit.vw.solr._

object People extends Controller {

  def publicationsData(vivoId: String, items: Int) = {
    Person.find("<"+VivoConnection.baseUri+"/"+vivoId+">", SolrConnection.server) match {
      case Some(person) => { 
        val vwr = new VivoWidgetJsonResult(person.publications)
        request.format match {
          case "json" => Json(vwr.json.toString)
          case "jsonp" => Json(vwr.jsonp)
          case _ => NoContent
        }
      }
      case _ => NotFound
    }

  }

  def publications(vivoId: String, items: Int, formatting: String = "detailed", style: String = "yes") = {
    Person.find("<"+VivoConnection.baseUri+"/"+vivoId+">", SolrConnection.server) match {
      case Some(person) => {
        val modelData = new java.util.HashMap[java.lang.String,java.lang.Object]
        modelData.put("publications",person.publications)
println("<<<pubs>>>" + person.publications)
        modelData.put("style",style)
        modelData.put("formatting",formatting)
        val htmlString = TemplateLoader.load("People/publications.html").render(modelData)
        request.format.toString match {
          case "js" => 
            val lines = htmlString.split('\n').toList
            val documentWrites = lines.map { "document.write('"+_.replaceAll("'","\\\\'")+"');" }
            val documentWritesString = documentWrites.mkString("\n")
            Json(documentWritesString)
          case "html" => Html(htmlString)
          case _ => NoContent
        }
      }
      case _ => "Not Found"
    }

  }

}


