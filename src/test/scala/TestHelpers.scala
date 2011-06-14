package edu.duke.oit.test.helpers

import edu.duke.oit.vw.solr._
import org.specs._
import com.hp.hpl.jena.rdf.model.{Model => JModel,ModelFactory}

object TestServers {
  val currentDirectory = new java.io.File(".").getCanonicalPath
  val widgetSolrCfg = new SolrConfig(currentDirectory+"/solr/solr.xml",currentDirectory+"/solr","vivowidgetcore")
  val widgetSolr = Solr.solrServer(widgetSolrCfg)
  val vivoSolrCfg = new SolrConfig(currentDirectory+"/solr/solr.xml",currentDirectory+"/solr","vivocore")
  val vivoSolr = Solr.solrServer(vivoSolrCfg)

  // no longer used
  //val vivo = new Vivo("jdbc:mysql://localhost:3306/vitrodb","root","","MySQL","com.mysql.jdbc.Driver")

  // in memory database - need the DB_CLOSE_DELAY in order for the db to perist across connections
  val url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"
  val user = "sa"
  val password = ""
  val dbType = "H2"

  val vivo = new Vivo(url,user,password,dbType,"org.h2.Driver")


  def loadSampleData = {
    import edu.duke.oit.vw.connection._
    import com.hp.hpl.jena.rdf.model._
    import com.hp.hpl.jena.util.FileManager
    import java.io.InputStream

   print("loading sample data...")
   val sampleInstanceFile = currentDirectory+"/src/test/resources/kb2.rdf"
   val sampleOntologyFile = currentDirectory+"/src/test/resources/vivo_core_ontology.rdf"

   Class.forName("org.h2.Driver")
    val cInfo = new JenaConnectionInfo(url,user,password,dbType)

    Jena.truncateAndCreateStore(cInfo)
    Jena.sdbModel(cInfo, "http://vitro.mannlib.cornell.edu/default/vitro-kb-2") { dbModel =>
      val in = FileManager.get.open(sampleInstanceFile)
      dbModel.read(in, null);
    }
    Jena.sdbModel(cInfo, "http://vitro.mannlib.cornell.edu/filegraph/tbox/vivo-core-1.2.owl") { dbModel =>
      val in = FileManager.get.open(sampleOntologyFile)
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

/**
 * Extend your specificiation with this trait to have the sample RDF model loaded.
 */
trait SampleLoader extends Specification {

  // load sample data from a base vivo instance
  doBeforeSpec {
    TestServers.loadSampleData
  }

}
