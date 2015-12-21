package edu.duke.oit.vw.queue

import java.net.URI

import edu.duke.oit.vw.solr.VivoSolrIndexer

import akka.actor.Actor._
import akka.actor.{Actor,ActorSystem,Props}

object BatchOrganizationsUpdater {

  val system = ActorSystem("BatchOrganizationsUpdater")
  val actor = system.actorOf(Props[BatchOrganizationsUpdater],name = "batchUpdateActor" )

}


import edu.duke.oit.vw.utils._

/*
case class BatchUpdateMessage(uris:List[String], from:Option[String])
*/

/**
 * Wraps the lift-json parsing and extraction of a person.
 */
/*
object BatchUpdateMessage {
  def apply(json:String) = {
    import net.liftweb.json._
    // Brings in default date formats etc.
    implicit val formats = DefaultFormats 

    val j = JsonParser.parse(json)
    j.extract[BatchUpdateMessage]
  }
}
*/


class BatchOrganizationsUpdater extends Actor {

  def receive = {
    case msg:String => {
      val updateMessage = BatchUpdateMessage(msg)

      import edu.duke.oit.vw.solr.VivoSolrIndexer
      import edu.duke.oit.vw.scalatra.WidgetsConfig

      val vsi = new VivoSolrIndexer(WidgetsConfig.server, WidgetsConfig.widgetServer)
      //vsi.reindexOrganizationsUris(updateMessage.uris)
      vsi.reindexOrganizations(updateMessage.uris)
    }
    case _ => { 
      println(">> no message!!")
    }
  }

}
