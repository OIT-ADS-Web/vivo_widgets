package edu.duke.oit.vw.scalatra

import akka.actor.{Actor,ActorSystem,Props}
import akka.event.Logging
import edu.duke.oit.vw.solr.{Solr,Vivo,SolrConfig}
import org.apache.solr.client.solrj.SolrServer

// use scala collections with java iterators
import scala.collection.JavaConversions._

import org.slf4j.{Logger, LoggerFactory}

object WidgetsConfig {

  var _properties:Map[String,String] = _
  val system = ActorSystem()
  val log =  LoggerFactory.getLogger(getClass)

  // TODO: put somewhere else - temporary 
  WidgetsConfig.setProperties

  def setProperties() = {
    this._properties = loadProperties()
    setupConfig
  }
  def properties = this._properties
  
  def baseUri = {
    properties("Vitro.defaultNamespace")
  }

  def theme = {
    properties.getOrElse("Widgets.theme","default")
  }

  def topLevelOrg = {
    properties.getOrElse("Widgets.topLevelOrg",properties.get("visualization.topLevelOrg"))
  }

  def updatesUserName = {
    properties.getOrElse("WidgetUpdateSetup.username","widgets")
  }

  def updatesPassword = {
    properties.getOrElse("WidgetUpdateSetup.password","vivo")
  }

  def baseProtocolAndDomain = {
    properties.get("Widgets.baseProtocolAndDomain")
  }
  
  var server:Vivo = _
  var widgetConfiguration:SolrConfig = _
  var widgetServer:SolrServer = _
  var vivoConfiguration:SolrConfig = _
  var vivoServer:SolrServer = _

  def prepareCore = {
    log.debug("Adding Widgets core to VIVO solr index...")
    log.debug("getting from: " + properties("WidgetsSolr.directory"))
    Solr.addCore(vivoServer, "vivowidgetcore",properties("WidgetsSolr.directory"))

    log.info("Connecting Widgets Core")
    widgetServer = Solr.solrServer(properties("vitro.local.solr.url") + "/vivowidgetcore")
  }
  
  def setupConfig = {
    log.debug("Configuring VIVO Widgets...")
    
    server = new Vivo(url      = properties("VitroConnection.DataSource.url"),
                      user     = properties("VitroConnection.DataSource.username"),
                      password = properties("VitroConnection.DataSource.password"),
                      dbType   = properties("VitroConnection.DataSource.dbtype"),
                      driver   = properties("VitroConnection.DataSource.driver"))
    server.loadDriver()
    server.setupConnectionPool()

    log.info("Connecting VIVO Core")
    vivoServer = Solr.solrServer(properties("vitro.local.solr.url"))

    log.info("Start IndexUpdater")
    import edu.duke.oit.vw.queue._
    val indexUpdater = system.actorOf(Props[IndexUpdater])
    log.info("IndexUpdater started.")
  }

  def loadProperties() = {
    import java.io.InputStream
    import java.util.Properties
    import java.io.FileInputStream
    val props:Properties = new Properties
    
    try {
      var inStream:InputStream = classOf[WidgetInitialization].getClassLoader().
        getResourceAsStream("deploy.properties")
      // Load a properties object
      props.load(inStream)
    } catch  {
      case _ : Throwable  => {
        println(">>>> couldn't load deploy.properties, looking for system property.")
        try {
          var i = System.getProperty("properties.location")
          props.load(new FileInputStream(i))
        } catch {
          case _ : Throwable => {
            println(">>>> couldn't load the deploy properties from system property.")
          }
        }

      }
    } finally {
      
    }
    // create a real immutable map out of the java Properties
    props.foldLeft(Map[String,String]()){(m, p) => m ++ Map(p._1 -> p._2)}
  }


}
