package edu.duke.oit.vw.solr

import org.apache.solr.client.solrj.SolrServer
import org.apache.solr.common.SolrInputDocument

import edu.duke.oit.vw.models._
import edu.duke.oit.vw.utils._

import com.hp.hpl.jena.rdf.model.ModelFactory
import edu.duke.oit.vw.jena.Sparqler

import java.util.NoSuchElementException
import java.util.Date
import scala.collection.JavaConversions._

import java.text.SimpleDateFormat

object PersonIndexer extends SimpleConversion
  with WidgetLogging
{
  def index(uri: String,vivo: Vivo, solr: SolrServer) = {
    indexAll(List(uri),vivo,solr)
  }

  def indexAll(uris: List[String],vivo: Vivo, solr: SolrServer) = {
    log.info("Buidling URIS:" + uris)
    uris.grouped(100).foreach{ groupedUris =>
      log.info("Grouped URIS:" + uris)
      val docs = groupedUris.map( uri => buildDoc(uri,vivo)).flatten
      solr.add(docs.toIterable)
    }
  }

  def buildDoc(uri: String,vivo: Vivo): Option[SolrInputDocument] = {
    buildPerson(uri,vivo).foreach{ p =>
      val solrDoc = new SolrInputDocument()
      solrDoc.addField("id",p.uri)
      solrDoc.addField("alternateId", p.personAttributes.get("alternateId").get)
      solrDoc.addField("group","people")
      solrDoc.addField("json",p.toJson)
      solrDoc.addField("updatedAt", p.updatedAt)
     
      p.uris.map {uri => solrDoc.addField("uris",uri)}
      return Option(solrDoc)
    }
    return None
  }

  def buildPerson(uri: String,vivo: Vivo): Option[Person] = {
    log.debug("pull uri: " + uri)
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
      log.debug("pull newsfeeds")
      val newsfeeds     = Newsfeed.fromUri(vivo, uriContext)

      val now = new Date

      val p = Person.build(uri, now, personData.head, 
                           pubs, awards,
                           artisticWorks, grants, courses,
                           professionalActivities, positions,
                           addresses, educations, rAreas, webpages,
                           geoFocus, newsfeeds)
      log.info("buildPerson uri: " + uri)
      return Option(p)
    }
    None
  }
}

