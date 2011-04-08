package controllers

import play._
import play.mvc._

import models._
import models.{SolrConnection,VivoConnection,VivoWidgetJsonResult}
import edu.duke.oit.vw.solr._

object Builder extends Controller {

  def index(vivoId: String) = {
    Person.find(VivoConnection.baseUri+vivoId, SolrConnection.server) match {
      case Some(person) => Template("vivoId" -> vivoId, "person" -> person)
      case _ => NotFound
    }
  }

  def root = Action(index(""))

}


