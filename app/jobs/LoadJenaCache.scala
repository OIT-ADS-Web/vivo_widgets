import play._
import play.jobs._

import models._

import edu.duke.oit.vw.solr._

//@OnApplicationStart
class LoadJenaCache extends Job {

  override def doJob() {
    val vsi = new VivoSolrIndexer(VivoConnection.server, SolrConnection.widgetServer)
    vsi.indexPeople
  }
}
