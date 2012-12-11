package edu.duke.oit.vw.scalatra

import edu.duke.oit.vw.utils.{ElvisOperator,Json,Int,WidgetLogging}
import edu.duke.oit.vw.solr.VivoSolrIndexer
import edu.duke.oit.vw.solr.{Person,VivoSearcher}
import java.net.URL
import org.scalatra._
import scalate.ScalateSupport
import edu.duke.oit.vw.scalatra.BasicAuth.AuthenticationSupport
import akka.actor.Actor._
import akka.actor.Actor
import edu.duke.oit.vw.queue._

class WidgetUpdatesFilter extends ScalatraFilter 
  with AuthenticationSupport 
  with WidgetLogging {
  
  post("/updates/rebuild/index") {
    basicAuth
    WidgetsConfig.prepareCore
    val vsi = new VivoSolrIndexer(WidgetsConfig.server, WidgetsConfig.widgetServer)
    vsi.indexAll(false,false)
    Json.toJson(Map("complete" -> true))
  }

  post("/updates/uri") {
    basicAuth
    WidgetsConfig.prepareCore
    params.get("message") match {
      case Some(message:String) => {
        val vsi = new VivoSolrIndexer(WidgetsConfig.server, WidgetsConfig.widgetServer)
        val actors = Actor.registry.actorsFor[IndexUpdater] 
        actors.length match {
          case 0 => {
            log.error(">> ERROR: actor not found IndexUpdater in registry")
            Json.toJson(Map("error" -> "IndexUpdater not in registry"))
          }
          case _ => {
            val indexUpdater = actors(0)
            indexUpdater ! message
            Json.toJson(Map("message" -> "Sent to Indexupdater"))
          }
        }
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
