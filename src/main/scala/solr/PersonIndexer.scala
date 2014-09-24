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

        val pubs          = Publication.fromUri(vivo, uriContext)
        val awards        = Award.fromUri(vivo, uriContext)
        val artisticWorks = ArtisticWork.fromUri(vivo, uriContext)
        val grants        = Grant.fromUri(vivo, uriContext)
        val courses       = Course.fromUri(vivo, uriContext)
        val presentations = Presentation.fromUri(vivo, uriContext)
        val positions     = Position.fromUri(vivo, uriContext)
        val addresses     = Address.fromUri(vivo, uriContext)
        val educations    = Education.fromUri(vivo, uriContext)
        val rAreas        = ResearchArea.fromUri(vivo, uriContext)
        val webpages      = Webpage.fromUri(vivo, uriContext)
        val geoFocus      = GeographicFocus.fromUri(vivo, uriContext)

        val p = Person.build(uri, personData.head, pubs, awards,
                             artisticWorks, grants, 
                             courses, presentations, positions, addresses,
                             educations, rAreas, webpages,
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
        log.error("PersonIndexer error: " + e.toString)
        e.printStackTrace()
      }
      case e:Throwable => {
        log.error("PersonIndexer error: " + e.toString)
        e.printStackTrace()
      }
    }
    log.debug("done with uri: " + uri)
  }

}

