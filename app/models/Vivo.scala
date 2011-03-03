package models
import edu.duke.oit.jena.connection._
import edu.duke.oit.jena.actor.JenaCache

object Vivo {
  val baseUri = play.configuration.conf.getProperty("vivo.baseUri")
  val jenaConnectionInfo = new JenaConnectionInfo(url      = play.configuration.conf.getProperty("vivo.jdbc.url"),
                                                  user     = play.configuration.conf.getProperty("vivo.jdbc.user"),
                                                  password = play.configuration.conf.getProperty("vivo.jdbc.password"),
                                                  dbType   = play.configuration.conf.getProperty("vivo.jdbc.dbType"))
  def initializeJenaCache() = {
    Class.forName(play.configuration.conf.getProperty("vivo.jdbc.driver"))
    JenaCache.setFromDatabase(jenaConnectionInfo, "http://vitro.mannlib.cornell.edu/default/vitro-kb-2")
  }

  def queryJenaCache(sparql: String) = {
    JenaCache.queryModel(sparql)
  }
}
