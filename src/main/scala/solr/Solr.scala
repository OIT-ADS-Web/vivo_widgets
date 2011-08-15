package edu.duke.oit.vw.solr

import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer
import org.apache.solr.client.solrj.SolrServer
import org.apache.solr.core.CoreContainer
import org.apache.solr.client.solrj.request.CoreAdminRequest
import org.apache.solr.client.solrj.response.CoreAdminResponse

import java.io.File


class SolrConfig(val file: String, val directory: String, val coreName: String)

object Solr {

  def solrServer(solrConfig: SolrConfig):SolrServer = {
    val configFile = new File( solrConfig.file )
    val container = new CoreContainer

    container.load( solrConfig.directory, configFile )
    new EmbeddedSolrServer( container, solrConfig.coreName )
  }

  def solrServer(url: String):SolrServer = {
    new CommonsHttpSolrServer(url)
  }

  def addCore(solrServer: SolrServer, name: String, instanceDir: String): Boolean = {
    val statusResponse: CoreAdminResponse = CoreAdminRequest.getStatus(name, solrServer);
    val coreExists = statusResponse.getCoreStatus(name).size() > 0
    if (!coreExists) {
      val create: CoreAdminRequest.Create = new CoreAdminRequest.Create()
      create.setCoreName(name)
      create.setInstanceDir(instanceDir)
      create.process(solrServer)
    }
    true
  }

}
