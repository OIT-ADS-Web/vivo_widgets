package controllers

import play._
import play.mvc._
import play.templates._

import models.{SolrConnection,VivoConnection,VivoWidgetJsonResult}
import edu.duke.oit.vw.solr._

object People extends Controller {

  def collectionData(vivoId: String, collectionName:String, items: Int) = {
    Person.find(VivoConnection.baseUri+vivoId, SolrConnection.widgetServer) match {
      case Some(person) => { 
        collectionName match {
          case "publications" => renderCollectionData(person.publications)
          case "grants" => renderCollectionData(person.grants)
          case "courses" => renderCollectionData(person.courses)
          case _ => NoContent
        }
      }
      case _ => NotFound
    }

  }

  def renderCollectionData(collection: List[AnyRef]) = {
    val vwr = new VivoWidgetJsonResult(collection)
    request.format match {
      case "json" => Json(vwr.json.toString)
      case "jsonp" => Json(vwr.jsonp)
      case _ => NoContent
    }

  }

  def collection(vivoId: String, collectionName:String, items: Int, formatting: String = "detailed", style: String = "yes") = {
    Person.find(VivoConnection.baseUri+vivoId, SolrConnection.widgetServer) match {
      case Some(person) => {
        collectionName match {
          case "publications" => renderCollection(collectionName,person.publications.sortBy{p => p.getOrElse("year","")}.reverse,items,formatting,style)
          case "grants" => renderCollection(collectionName,person.grants,items,formatting,style)
          case "courses" => renderCollection(collectionName,person.courses,items,formatting,style)
          case _ => NoContent
        }
      }
      case _ => "Not Found"
    }

  }

  def renderCollection(collectionName: String, collection: List[AnyRef], items: Int, formatting: String, style: String) = {
    val modelData = new java.util.HashMap[java.lang.String,java.lang.Object]
    if (items > 0) {
      modelData.put(collectionName,collection.slice(0,items))
    } else {
      modelData.put(collectionName,collection)
    }
    modelData.put("style",style)
    modelData.put("formatting",formatting)
    val htmlString = TemplateLoader.load("People/"+collectionName+".html").render(modelData)
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

}


