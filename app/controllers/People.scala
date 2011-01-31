package controllers

import play._
import play.mvc._

import models._

object People extends Controller {

  def publications(person_uri: String) = {
    Template("publications" -> Publication.find_all_by_person_uri(person_uri).get)
  }

}


