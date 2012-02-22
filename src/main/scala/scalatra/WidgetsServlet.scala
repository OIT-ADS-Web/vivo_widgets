package edu.duke.oit.vw.scalatra

import edu.duke.oit.vw.utils.{ElvisOperator,Json,Int}
import edu.duke.oit.vw.solr.{Person,VivoSearcher}
import java.net.URL
import org.scalatra._
import scalate.ScalateSupport

// use scala collections with java iterators
import scala.collection.JavaConversions._

trait FormatType
object FormatHTML extends FormatType
object FormatJS extends FormatType
object FormatJSON extends FormatType
object FormatJSONP extends FormatType

class WidgetsFilter extends ScalatraFilter
  with ScalateSupport 
  with ScalateTemplateStringify {
  
  // GET /people/{vivoId}/{collectionName}/5.jsonp
  get("/people/:collectionName/:count.:format") {
    WidgetsConfig.prepareCore
    requestSetup
    Person.find(params("uri"), WidgetsConfig.widgetServer) match {
      case Some(person) => { 
        params.getOrElse('collectionName, "") match {
          case "publications" => renderCollectionData(person.publications)
          case "grants" => renderCollectionData(person.grants)
          case "courses" => renderCollectionData(person.courses)
          case x => "Collection not found: " + x
        }
      }
      case _ => "Not Found"
    }
  }
  
  // GET /search.json?query=theory*
  get("/search.:format") {
    requestSetup
    val result = VivoSearcher.search(params.getOrElse("query",""), WidgetsConfig.vivoServer)
    format(FormatJSONP) match {
      case FormatJSON => result.toJson
      case FormatHTML => "TODO: html" // Template('query -> query, 'result -> result)
      case FormatJSONP => "vivoSearchResult("+result.toJson+")" //.toJson
      case _ => "NoContent"
    }
  }
  
  get("/builder") {
    WidgetsConfig.prepareCore
    import edu.duke.oit.vw.utils.ElvisOperator._
    Person.find(params("uri"), WidgetsConfig.widgetServer) match {
      case Some(person) => {
        val d = Map(
          "uriPrefix" -> uriPrefix(),
          "contextUri" -> (request.getContextPath() ?: ""),
          "person" -> person,
          "theme" -> WidgetsConfig.theme
          )
        contentType = "text/html"
        templateEngine.layout(TemplateHelpers.tpath("builder/index.jade"), d)
      }
      case _ => "NoContent"
    }
  }
  
  
  
  
  protected def formatCollection(formatType: FormatType, collectionName: String, collection: List[AnyRef], items: Option[Int], formatting: String, style: String) = {
    var modelData = scala.collection.mutable.Map[String,Any]()
    items match {
      case Some(x:Int) => modelData.put(collectionName, collection.slice(0, x))
      case _ => modelData.put(collectionName, collection)
    }
    modelData.put("style",style)
    modelData.put("formatting",formatting)
    modelData.put("searchURI",uri("/search.html"))

    val template = TemplateHelpers.tpath(collectionName + ".jade")
    formatType match {
      case FormatJS => renderTemplateString(servletContext, template, modelData.toMap)
      case FormatHTML => templateEngine.layout(template, modelData.toMap)
    }
  }
  
  protected def renderCollectionData(collection: List[AnyRef]) = {
    request("format") match {
      case FormatJSON => Json.toJson(collection)
      case FormatJSONP => "vivoWidgetResult(" + Json.toJson(collection) + ");"
      case FormatHTML => {
        formatCollection(FormatHTML, params("collectionName"),
                         collection,
                         Int(params.getOrElse("count", "all")),
                         params.getOrElse("formatting", "detailed"),
                         params.getOrElse("style", "yes"))
      }
      case FormatJS => {
        val output = formatCollection(FormatJS, params("collectionName"),
                                      collection,
                                      Int(params.getOrElse("count", "all")),
                                      params.getOrElse("formatting", "detailed"),
                                      params.getOrElse("style", "yes"))
        val lines = output.split('\n').toList
        val documentWrites = lines.map { "document.write('"+_.replaceAll("'","\\\\'")+"');" }
        documentWrites.mkString("\n")
      }
      case _ => "not content"
    }
  }
  
  protected def requestSetup = {
    request.put("format", format())
    setContentType(request("format").asInstanceOf[FormatType])
  }
  
  protected def setContentType(formatType:FormatType) = {
    formatType match {
      case FormatJSON => contentType = "application/json"
      case FormatJSONP => contentType = "application/javascript"
      case FormatJS => contentType = "application/javascript"
      case FormatHTML => contentType = "text/html"
      case _ => contentType = "text/unknown"
    }
  }

  protected def format(defaultType:FormatType=FormatJSON):FormatType = {
    params.getOrElse("format", "") match {
      case "json" => FormatJSON
      case "jsonp" => FormatJSONP
      case "html" => FormatHTML
      case "js" => FormatJS
      case _ => defaultType
    }
  }
  
  protected def uri(s:String) = {
    uriPrefix + s
  }
  
  protected def uriPrefix() = {
    import edu.duke.oit.vw.utils.ElvisOperator._
    (request.getContextPath() ?: "") + (request.getServletPath() ?: "")
  }

  notFound {
    filterChain.doFilter(request, response)
  }
  
}
