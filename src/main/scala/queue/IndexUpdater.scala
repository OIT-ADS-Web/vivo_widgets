package edu.duke.oit.vw.queue

import java.net.URI

import edu.duke.oit.vw.solr.VivoSolrIndexer

import akka.actor.Actor._
import akka.actor.{Actor,ActorSystem,Props}

object IndexUpdater {

  val system = ActorSystem("IndexUpdater")
  val actor = system.actorOf(Props[IndexUpdater],name = "updateActor" )

}

import edu.duke.oit.vw.utils._

case class UpdateMessage(uri:String, from:Option[String])

/**
 * Wraps the lift-json parsing and extraction of a person.
 */
object UpdateMessage {
  def apply(json:String) = {
    import net.liftweb.json._
    // Brings in default date formats etc.
    implicit val formats = DefaultFormats

    val j = JsonParser.parse(json)
    j.extract[UpdateMessage]
  }
}


class IndexUpdater extends Actor {

  def receive = {
    case msg:String => {
      // log.debug(">> received message")
      val msgString = msg // .bodyAs[String]
      // log.debug(">> msgString: " +msgString)
      val updateMessage = UpdateMessage(msgString)

      // log.debug(">> reindex: " + updateMessage.uri)

      import edu.duke.oit.vw.solr.VivoSolrIndexer
      import edu.duke.oit.vw.scalatra.WidgetsConfig

      val vsi = new VivoSolrIndexer(WidgetsConfig.server, WidgetsConfig.widgetServer)  
      vsi.reindexUri(updateMessage.uri)
      // log.debug(">> finished reindexing " + updateMessage.uri)
    }
    case _ => { 
      println(">> no message!!")
    }
  }

}
