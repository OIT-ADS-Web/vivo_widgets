package edu.duke.oit.vw.scalatra

import akka.actor.Actor
import akka.util.Logging
import edu.duke.oit.vw.solr.{Solr,Vivo,SolrConfig}
import org.apache.solr.client.solrj.SolrServer

// use scala collections with java iterators
import scala.collection.JavaConversions._

object WidgetsConfig extends Logging {

  var _properties:Map[String,String] = _
  def setProperties() = {
    this._properties = loadProperties()
    setupConfig
  }
  def properties = this._properties
  
  def baseUri = {
    properties("Vitro.defaultNamespace")
  }

  def updatesUserName = "widgets"
  def updatesPassword = "vivo"
  
  var server:Vivo = _
  var widgetConfiguration:SolrConfig = _
  var widgetServer:SolrServer = _
  var vivoConfiguration:SolrConfig = _
  var vivoServer:SolrServer = _
  
  def setupConfig = {
    
    server = new Vivo(url      = properties("VitroConnection.DataSource.url"),
                      user     = properties("VitroConnection.DataSource.username"),
                      password = properties("VitroConnection.DataSource.password"),
                      dbType   = properties("VitroConnection.DataSource.dbtype"),
                      driver   = properties("VitroConnection.DataSource.driver"))

    widgetConfiguration = new SolrConfig(file      = properties("WidgetsSolr.directory") + "/solr.xml",
                                         directory = properties("WidgetsSolr.directory"),
                                         coreName  = "vivowidgetcore")
    widgetServer = Solr.solrServer(widgetConfiguration)

    vivoConfiguration = new SolrConfig(file      = properties("WidgetsSolr.directory") + "/solr.xml",
                                       directory = properties("WidgetsSolr.directory"),
                                       coreName  = "vivocore")
    vivoServer = Solr.solrServer(vivoConfiguration)

    log.info("Start IndexUpdater")
    import edu.duke.oit.vw.queue._
    val indexUpdater = Actor.actorOf[IndexUpdater].start
    log.info("IndexUpdater started.")
  }

  def loadProperties() = {
    import java.io.InputStream;
    import java.util.Properties;
    val props:Properties = new Properties
    
    try {
      var inStream:InputStream = classOf[WidgetInitialization].getClassLoader().getResourceAsStream("deploy.properties")
      // Load a properties object
      props.load(inStream)
    } catch  {
      case _  => println(">>>> couldn't load deploy.properties <<<<")
    } finally {
      
    }
    // create a real immutable map out of the java Properties
    props.foldLeft(Map[String,String]()){(m, p) => m ++ Map(p._1 -> p._2)}
  }
  
}
