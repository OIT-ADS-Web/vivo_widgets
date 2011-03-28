package models
import edu.duke.oit.vw.solr.{SolrConfig,Solr}

object SolrConnection {
  val configuration = new SolrConfig(file      = play.configuration.conf.getProperty("solr.file"),
                                     directory = play.configuration.conf.getProperty("solr.directory"),
                                     coreName  = play.configuration.conf.getProperty("solr.coreName"))
  val server = Solr.solrServer(solrConfig)
}
