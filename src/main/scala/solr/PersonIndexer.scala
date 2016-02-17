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

import net.liftweb.json._

import edu.duke.oit.vw.scalatra.WidgetsConfig
 
object PersonIndexer extends SimpleConversion
  with WidgetLogging
  with JsonDiff {

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

  def checkExisting(uri: String): Option[Person] = {

    val vsi = new VivoSolrIndexer(WidgetsConfig.server, WidgetsConfig.widgetServer)
 
    val person = vsi.getPerson(uri)

    return person
  }

  def buildDoc(uri: String,vivo: Vivo): Option[SolrInputDocument] = {
    buildPerson(uri,vivo).foreach{ p =>
  
      val existing = checkExisting(p.uri)

      var skip:Boolean = false

      var person:Person = p.copy()

      if (existing.isDefined && existing.get.updatedAt.isDefined) {
        val updatedAt = existing.get.updatedAt

        // need to make a person with same updatedAt value so 
        // it doesn't diff merely on that field alone
        person = p.copy(updatedAt = updatedAt)

        val changes:Boolean = hasChanges(existing.get, person)
        skip = !(changes)
      }
      
      if (skip) {
         val updatedAt = existing.get.updatedAt
         person = p.copy(updatedAt = updatedAt)
         
         log.debug(String.format("Skipping index for %s. No changes detected", uri))
      } else {
         // if we do NOT skip, then make the person the same as the p 
         // e.g. (with NOW as the updatedAt value)
         person = p.copy()
      }
      
      val solrDoc = new SolrInputDocument()
      
      solrDoc.addField("alternateId", person.personAttributes.get("alternateId").get)
      solrDoc.addField("id",person.uri)
      solrDoc.addField("group","people")
      solrDoc.addField("json",person.toJson)
      
      solrDoc.addField("updatedAt", person.updatedAt.get)
      person.uris.map {uri => solrDoc.addField("uris",uri)}
     
      return Option(solrDoc)
      
    }
    return None
  }

  def buildPerson(uri: String,vivo: Vivo): Option[Person] = {
    log.debug("pull uri: " + uri)
    val uriContext = Map("uri" -> uri)
    
    val personData = vivo.selectFromTemplate("sparql/personData.ssp", uriContext)
    
    //
    //if (personData.size > 0 && anyChanges) {
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

      // might have to check here
      var now = new Date
      
      //val existing = checkExisting(uri)
      //if (existing.isDefined) {
      //  now = existing.get("updatedAt")
      //}

      // 
      var p = Person.build(uri, Option.apply(now), personData.head, 
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

