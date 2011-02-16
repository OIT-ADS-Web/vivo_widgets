package controllers

import play._
import play.mvc._

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
      case Some(publications) => Template("publications" -> publications)
      case _ => NoContent
    }
  }

}


