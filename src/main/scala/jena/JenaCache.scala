package edu.duke.oit.vw.jena

import akka.actor.Actor._
import akka.actor.{Actor,ActorSystem,Props}

import com.hp.hpl.jena.rdf.model.{Model => JModel, ModelFactory}
import com.hp.hpl.jena.tdb.TDBFactory
import edu.duke.oit.vw.utils.WidgetLogging

object GetGraph
object ClearModel
case class SetModel(model: JModel)

class JenaActor extends Actor with WidgetLogging {

  var model: Option[JModel] = None

  def receive = {
    case SetModel(m: JModel) => {
      log.debug("set model")
      model = Some(m)
    }
    case GetGraph => {
      // self.reply(model)
      sender ! model
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

import akka.pattern.{ ask, pipe }
import akka.util.Timeout
import scala.concurrent.Await
import scala.concurrent.duration._

object JenaCache {

  val system = ActorSystem("JenaCache")
  val jenaActor = system.actorOf(Props[JenaActor], name = "jenacache")
  implicit val timeout = Timeout(5 seconds)

  def setFromDatabase(cBase: JenaConnectionBase, modelUri: String) {
    Jena.sdbModel(cBase, modelUri) {
      dbModel =>
        var model = TDBFactory.createModel // ModelFactory.createDefaultModel
        model.add(dbModel)
        setModel(model)
    }
  }

  def setModel(m: JModel) = {
    jenaActor ! new SetModel(m)
  }

  def getModel = {
    Await.result((jenaActor ? GetGraph), timeout.duration).asInstanceOf[Option[JModel]] match {
      case Some(m: JModel) => Some(m)
      case (Some(m: Option[JModel])) => m
      case _ => None
    }
  }

  def clear = {
    jenaActor ! ClearModel
  }

  def queryModel(query: String) = {
    Await.result((jenaActor ? GetGraph), timeout.duration).asInstanceOf[Option[JModel]].getOrElse(None) match {
      case Some(m: JModel) => {
        Sparqler.selectingFromModel(m,query){resultsSet => Sparqler.simpleResults(resultsSet)}
      }
      case _ => List()
    }
  }

}
