package edu.duke.oit.vw.jena

import akka.actor.Actor._
import akka.actor.Actor

import com.hp.hpl.jena.rdf.model.{Model => JModel, ModelFactory}
import com.hp.hpl.jena.tdb.TDBFactory
import edu.duke.oit.vw.utils.WidgetLogging

object GetGraph
object ClearModel
case class SetModel(model: JModel)

class JenaActor extends Actor {

  var model: Option[JModel] = None

  def receive = {
    case SetModel(m: JModel) => {
      log.debug("set model")
      model = Some(m)
    }
    case GetGraph => {
      self.reply(model)
    }
    case ClearModel => {
      // just making sure all statements are garbage collected
      model.getOrElse(TDBFactory.createModel).removeAll()
      model = None
      log.debug("cleared model")
    }
    case _ => println("JenaActor message not found.")
  }

}

object JenaCache {

  val jenaActor = actorOf[JenaActor].start

  def setFromDatabase(cInfo: JenaConnectionInfo, modelUri: String) {
    Jena.sdbModel(cInfo, modelUri) {
      dbModel =>
        var model = TDBFactory.createModel // ModelFactory.createDefaultModel
        model.add(dbModel)
        Jena.sdbModel(cInfo,"http://vitro.mannlib.cornell.edu/filegraph/tbox/vivo-core-1.3.owl") { vivoOwl => model.add(vivoOwl) }
        setModel(model)
    }

  }

  def setModel(m: JModel) = {
    jenaActor ! new SetModel(m)
  }

  def getModel = {
    (jenaActor !! GetGraph) match {
      case Some(m: JModel) => Some(m)
      case (Some(m: Option[JModel])) => m
      case _ => None
    }
  }

  def clear = {
    jenaActor !! ClearModel
  }

  def queryModel(query: String) = {
    val model = jenaActor !! GetGraph
    model.getOrElse(None) match {
      case Some(m: JModel) => {
        Sparqler.selectingFromModel(m,query){resultsSet => Sparqler.simpleResults(resultsSet)}
      }
      case _ => List()
    }
  }

}
