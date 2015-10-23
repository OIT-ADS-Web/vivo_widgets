package edu.duke.oit.vw.solr

import org.apache.solr.client.solrj.SolrServer
import org.apache.solr.common.SolrInputDocument

import edu.duke.oit.vw.models._
import edu.duke.oit.vw.utils._

import com.hp.hpl.jena.rdf.model.ModelFactory
import edu.duke.oit.vw.jena.Sparqler

import java.util.NoSuchElementException
import scala.collection.mutable.ListBuffer
import scala.collection.JavaConversions._

object PersonIndexer extends SimpleConversion
  with WidgetLogging
{
  def index(uri: String,vivo: Vivo, solr: SolrServer) = {
    indexAll(List(uri),vivo,solr)
  }

  def indexAll(uris: List[String],vivo: Vivo, solr: SolrServer) = {
    val docs: ListBuffer[SolrInputDocument] = new ListBuffer()
    uris.foreach{ uri =>
      buildDoc(uri,vivo).foreach { doc =>
        docs.append(doc)
      }
    }
    solr.add(docs.toIterable)
    solr.commit(false,false)
  }

  def buildDoc(uri: String,vivo: Vivo): Option[SolrInputDocument] = {
    try {
      buildPerson(uri,vivo).foreach{ p =>
        val solrDoc = new SolrInputDocument()
        solrDoc.addField("id",p.uri)
        solrDoc.addField("alternateId", p.personAttributes.get("alternateId"))
        solrDoc.addField("group","people")
        solrDoc.addField("json",p.toJson)
        p.uris.map {uri => solrDoc.addField("uris",uri)}
        return Option(solrDoc)
      }
      return None
    } catch {
      case e:Throwable => {
        log.error("PersonIndexer error: ", e)
        None
      }
    }
  }

  def buildPerson(uri: String,vivo: Vivo): Option[Person] = {
    log.debug("pull uri: " + uri)
    try {
      val uriContext = Map("uri" -> uri)

      val personData = vivo.selectFromTemplate("sparql/personData.ssp", uriContext)
      if (personData.size == 0) {
        None
      } else {
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
        log.info("buildPerson uri: " + uri)
        Option(p)
      }
    } catch {
      case e:NoSuchElementException => {
        log.error("PersonIndexer error: ", e)
        None
      }
      case e:Throwable => {
        log.error("PersonIndexer error: ", e)
        None
      }
    }
  }

}

