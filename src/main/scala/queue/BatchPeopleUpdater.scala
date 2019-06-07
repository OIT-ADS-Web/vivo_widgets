package edu.duke.oit.vw.queue

import java.net.URI

import edu.duke.oit.vw.solr.VivoSolrIndexer

import akka.actor.Actor._
import akka.actor.{Actor,ActorSystem,Props}

import com.github.nscala_time.time.Imports._

object BatchPeopleUpdater {

  val system = ActorSystem("BatchPeopleUpdater")
  val actor = system.actorOf(Props[BatchPeopleUpdater],name = "batchUpdateActor" )

}

import edu.duke.oit.vw.utils._

class BatchPeopleUpdater extends Actor with WidgetLogging {

  def receive = {
    case msg:String => {
      log.debug(msg)
      val startAt = DateTime.now()
      val updateMessage = BatchUpdateMessage(msg)

      import edu.duke.oit.vw.solr.VivoSolrIndexer
      import edu.duke.oit.vw.scalatra.WidgetsConfig
      log.debug(updateMessage.uris.mkString(","))
      val vsi = new VivoSolrIndexer(WidgetsConfig.server, WidgetsConfig.widgetServer)
      vsi.reindexPeople(updateMessage.uris)
      val endAt = DateTime.now()
      val duration = (startAt to endAt).millis
      MetricsRecorder.recordProcessedBatch("people",updateMessage.uris.length,duration)
    }
    case _ => { 
      log.debug("no message!!")
    }
  }

}
