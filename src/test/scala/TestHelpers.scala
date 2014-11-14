package edu.duke.oit.test.helpers

import edu.duke.oit.vw.solr._
import org.specs2.mutable._
// import org.specs2.specification.BeforeExample
import org.specs2.specification.Scope
import com.hp.hpl.jena.rdf.model.{Model => JModel,ModelFactory}


object TestServers {
  val currentDirectory = new java.io.File(".").getCanonicalPath
  val widgetSolrCfg = new SolrConfig(currentDirectory+"/solr/solr.xml",currentDirectory+"/solr","vivowidgetcoretest")
  val widgetSolr = Solr.solrServer(widgetSolrCfg)
  Solr.addCore(widgetSolr, "vivowidgetcoretest", currentDirectory+"/solr")
  val vivoSolr = Solr.solrServer("http://localhost:8983/solr")

  // in memory database - need the DB_CLOSE_DELAY in order for the db to perist across connections
  val url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"
  val user = "sa"
  val password = ""
  val dbType = "H2"

  def vivo : Vivo = {
    new Vivo(url,user,password,dbType,"org.h2.Driver")
  }


  def loadSampleData(filePath: String) = {
    import edu.duke.oit.vw.jena._
    import com.hp.hpl.jena.rdf.model._
    import com.hp.hpl.jena.util.FileManager
    import java.io.InputStream

    print("loading sample data...")
    val sampleInstanceFile = currentDirectory + filePath

    Class.forName("org.h2.Driver")
    val cInfo = new JenaConnectionInfo(url,user,password,dbType)

    Jena.truncateAndCreateStore(cInfo)
    Jena.sdbModel(cInfo, "http://vitro.mannlib.cornell.edu/default/vitro-kb-2") { dbModel =>
      val in = FileManager.get.open(sampleInstanceFile)
                                                                                 dbModel.read(in, null);
                                                                               }
    println("[DONE]")

  }
}

object TestModels {
  import com.hp.hpl.jena.util.FileManager

  val currentDirectory = new java.io.File(".").getCanonicalPath

  val sampleInstanceFile = currentDirectory+"/src/test/resources/kb2.rdf"

  val sampleInstanceModel: JModel = ModelFactory.createDefaultModel().read(FileManager.get.open(sampleInstanceFile),null)
}
