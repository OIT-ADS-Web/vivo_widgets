package controllers

import play._
import play.mvc._
import play.templates._

import models.{SolrConnection,VivoConnection,VivoWidgetJsonResult}
import edu.duke.oit.vw.solr._

object Search extends Controller {

  def index(query: String) = {
    val result = VivoSearcher.search(query,SolrConnection.vivoServer)
    request.format match {
      case "json" => Json(result.toJson)
      case _ => NoContent
    }
  }
}

