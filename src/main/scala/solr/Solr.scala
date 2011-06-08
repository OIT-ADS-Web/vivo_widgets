package edu.duke.oit.vw.solr

import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer
import org.apache.solr.client.solrj.SolrServer
import org.apache.solr.core.CoreContainer

import java.io.File


class SolrConfig(val file: String, val directory: String, val coreName: String)

object Solr {

  def solrServer(solrConfig: SolrConfig):SolrServer = {
    val configFile = new File( solrConfig.file )
    val container = new CoreContainer

    container.load( solrConfig.directory, configFile )
    new EmbeddedSolrServer( container, solrConfig.coreName )
  }

}
