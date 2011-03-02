package controllers

import play._
import play.mvc._

import models._

object Builder extends Controller {

  def index(vivoId: String = "") = {
    Template
  }

  def root = Action(index())

}


