package models
import play._
import edu.duke.oit.vw.solr.{SolrConfig,Solr}

object SolrConnection {
  val widgetConfiguration = new SolrConfig(file      = Play.applicationPath + "/" + play.configuration.conf.getProperty("solr.file"),
                                           directory = Play.applicationPath + "/" +  play.configuration.conf.getProperty("solr.directory"),
                                           coreName  = play.configuration.conf.getProperty("solr.widgetCoreName"))
  val widgetServer = Solr.solrServer(widgetConfiguration)

  val vivoConfiguration = new SolrConfig(file      = Play.applicationPath + "/" + play.configuration.conf.getProperty("solr.file"),
                                         directory = Play.applicationPath + "/" +  play.configuration.conf.getProperty("solr.directory"),
                                         coreName  = play.configuration.conf.getProperty("solr.vivoCoreName"))
  val vivoServer = Solr.solrServer(vivoConfiguration)

}
