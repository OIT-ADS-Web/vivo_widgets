package edu.duke.oit.vw.scalatra

import akka.actor.Actor._
import akka.actor.{Actor,ActorSystem,Props}

import edu.duke.oit.vw.utils.{ElvisOperator,Json,Int,WidgetLogging}
import edu.duke.oit.vw.solr.VivoSolrIndexer
import edu.duke.oit.vw.solr.{Person,VivoSearcher}
import edu.duke.oit.vw.jena.JenaCache
import java.net.URL
import org.scalatra._
import scalate.ScalateSupport
import edu.duke.oit.vw.scalatra.BasicAuth.AuthenticationSupport
import edu.duke.oit.vw.queue._

class WidgetUpdatesFilter extends ScalatraFilter with AuthenticationSupport with WidgetLogging {
  
  post("/updates/rebuild/index") {
    basicAuth
    WidgetsConfig.prepareCore
    val vsi = new VivoSolrIndexer(WidgetsConfig.server, WidgetsConfig.widgetServer)
    vsi.indexAll(false)
    Json.toJson(Map("complete" -> true))
  }

  post("/updates/uri") {
    basicAuth
    WidgetsConfig.prepareCore
    params.get("message") match {
      case Some(message:String) => {
        // val vsi = new VivoSolrIndexer(WidgetsConfig.server, WidgetsConfig.widgetServer)
        val system =  JenaCache.system
        // val actors = Actor.registry.actorsFor[IndexUpdater] 
        val indexUpdater = system.actorOf(Props[IndexUpdater], name = "indexupdater")
        indexUpdater ! message
        Json.toJson(Map("message" -> "Sent to Indexupdater"))
      }
      case _ => "Not a valid request"
    }
  }
  
  get("/updates/test") {
    "test---"
  }

  notFound {
    filterChain.doFilter(request, response)
  }
  
}
