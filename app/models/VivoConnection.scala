package models
import edu.duke.oit.vw.solr.Vivo

object VivoConnection {
  val baseUri = play.configuration.conf.getProperty("vivo.baseUri")
  val server = new Vivo( url      = play.configuration.conf.getProperty("vivo.jdbc.url"),
                         user     = play.configuration.conf.getProperty("vivo.jdbc.user"),
                         password = play.configuration.conf.getProperty("vivo.jdbc.password"),
                         dbType   = play.configuration.conf.getProperty("vivo.jdbc.dbType")
                         driver   = play.configuration.conf.getProperty("vivo.jdbc.driver"))
}
