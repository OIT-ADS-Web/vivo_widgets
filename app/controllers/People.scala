package controllers

import play._
import play.mvc._

import models._

object People extends Controller {

  def publications(vivoId: String) = {
    Publication.findAllForPerson(Vivo.baseUri+vivoId) match {
      case Some(publications) => Template("publications" -> publications)
      case _ => NoContent
    }
  }

}


