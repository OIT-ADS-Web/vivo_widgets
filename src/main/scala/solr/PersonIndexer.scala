package edu.duke.oit.vw.solr

import org.apache.solr.client.solrj.SolrServer
import org.apache.solr.common.SolrInputDocument

import edu.duke.oit.vw.models._
import edu.duke.oit.vw.utils._

import com.hp.hpl.jena.rdf.model.ModelFactory
import edu.duke.oit.vw.jena.Sparqler

import java.util.NoSuchElementException

object PersonIndexer extends SimpleConversion 
  with Timer
  with WidgetLogging 
{

  def index(uri: String,vivo: Vivo, solr: SolrServer) = {
    log.debug("pull uri: " + uri)
    try {
      val uriContext = Map("uri" -> uri)

      val personData = vivo.selectFromTemplate("sparql/personData.ssp", uriContext)
      if (personData.size > 0) {

        log.debug("pull pubs")
        val pubs          = Publication.fromUri(vivo, uriContext)
        log.debug("pull awards")
        val awards        = Award.fromUri(vivo, uriContext)
        log.debug("pull artisticWorks")
        val artisticWorks = ArtisticWork.fromUri(vivo, uriContext)
        log.debug("pull grants")
        val grants        = Grant.fromUri(vivo, uriContext)
        log.debug("pull courses")
        val courses       = Course.fromUri(vivo, uriContext)
        log.debug("pull professionalActivities")
        val professionalActivities = ProfessionalActivity.fromUri(vivo, uriContext)
        log.debug("pull positions")
        val positions     = Position.fromUri(vivo, uriContext)
        log.debug("pull addresses")
        val addresses     = Address.fromUri(vivo, uriContext)
        log.debug("pull educations")
        val educations    = Education.fromUri(vivo, uriContext)
        log.debug("pull rAreas")
        val rAreas        = ResearchArea.fromUri(vivo, uriContext)
        log.debug("pull webpages")
        val webpages      = Webpage.fromUri(vivo, uriContext)
        log.debug("pull geoFocus")
        val geoFocus      = GeographicFocus.fromUri(vivo, uriContext)

        val p = Person.build(uri, personData.head, pubs, awards,
                             artisticWorks, grants, courses,
                             professionalActivities, positions,
                             addresses, educations, rAreas, webpages,
                             geoFocus)
        timer("add person to solr [" + uri + "]") {
          val solrDoc = new SolrInputDocument()
          solrDoc.addField("id",p.uri)
          solrDoc.addField("alternateId", personData.head('alternateId))
          solrDoc.addField("group","people")
          solrDoc.addField("json",p.toJson)
          p.uris.map {uri => solrDoc.addField("uris",uri)}
          solr.add(solrDoc)
        }
      }
      
    } catch {
      case e:NoSuchElementException => {
        log.error("PersonIndexer error: ", e)
      }
      case e:Throwable => {
        log.error("PersonIndexer error: ", e)
      }
    }
    log.debug("done with uri: " + uri)
  }

}

