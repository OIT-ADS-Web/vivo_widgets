package models
import play._
import edu.duke.oit.vw.solr.{SolrConfig,Solr}

object SolrConnection {
  val configuration = new SolrConfig(file      = Play.applicationPath + "/" + play.configuration.conf.getProperty("solr.file"),
                                     directory = Play.applicationPath + "/" +  play.configuration.conf.getProperty("solr.directory"),
                                     coreName  = play.configuration.conf.getProperty("solr.coreName"))
  val server = Solr.solrServer(configuration)
}
