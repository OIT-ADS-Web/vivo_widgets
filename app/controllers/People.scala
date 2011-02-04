package controllers

import play._
import play.mvc._

import models._

object People extends Controller {

  def publicationsStub(vivoId: String) = {
    val resultMock = """
        vivoWidgetResult({
          "resultHeader" : {"name" : "Smith, Joseph M", "id" : "smithjm", "date" : "2011-02-04"},
          "results"      : [{"citation":"Smith, JM, Cook, DE, Rogers, MR. Scala for Dummies. Webposse Press, 2007."},
                            {"citation":"Smith, JM, Cook, DE, Rogers, MR. Scala for Dummies. Webposse Press, 2007."},
                            {"citation":"Lawrence GL, Smith J (2010). Scala and Concurrency. Scaling Times. 5(15). 34-37."}]
        });
    """
    Json(resultMock)
  }

  def publications(vivoId: String) = {
    Publication.findAllForPerson(Vivo.baseUri+vivoId) match {
      case Some(publications) => Template("publications" -> publications)
      case _ => NoContent
    }
  }

}


