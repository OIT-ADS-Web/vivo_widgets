package edu.duke.oit.vw.solr

import org.apache.solr.client.solrj.SolrServer
import org.apache.solr.common.SolrInputDocument

import edu.duke.oit.vw.models._
import edu.duke.oit.vw.utils._

import com.hp.hpl.jena.rdf.model.ModelFactory
import edu.duke.oit.vw.jena.Sparqler

import java.util.NoSuchElementException
import java.util.Date
import java.util.Calendar 
import scala.collection.JavaConversions._

import java.text.SimpleDateFormat

import net.liftweb.json._

import edu.duke.oit.vw.scalatra.WidgetsConfig
 
object PersonIndexer extends SimpleConversion
  with WidgetLogging {
 
  def index(uri: String,vivo: Vivo, solr: SolrServer) = {
    indexAll(List(uri),vivo,solr)
  }

  def indexAll(uris: List[String],vivo: Vivo, solr: SolrServer) = {
    log.info("Building PersonIndexer.indexAll URIS:" + uris)
    uris.grouped(100).foreach{ groupedUris =>
      log.info("_Grouped URIS:" + uris)
      val docs = groupedUris.map( uri => buildDoc(uri,vivo)).flatten
      solr.add(docs.toIterable)
    }
  }

  def updateAll(uris: List[String],vivo: Vivo, solr: SolrServer) = {
    log.info("Building PersonIndexer.updateAll URIS:" + uris)
    uris.grouped(100).foreach{ groupedUris =>
      log.info("_Grouped URIS:" + uris)
      val docs = groupedUris.map( uri => updateDoc(uri,vivo)).flatten
      solr.add(docs.toIterable)
    }
  }

  def checkExisting(uri: String): Option[Person] = {
    val vsi = new VivoSolrIndexer(WidgetsConfig.server, WidgetsConfig.widgetServer)
    return vsi.getPerson(uri)
  }

  def hasChanges(existing: Person, toCheck: Person): Boolean = {
    val existingJson = existing.toJson
    val toCheckJson = toCheck.toJson

    log.debug("existing="+existingJson)
    log.debug("check="+toCheckJson)

    // http://scala-tools.org/mvnsites/liftweb-2.2-RC5/framework/lift-base_2.7.7/scaladocs/net/liftweb/json/Diff.html
    val Diff(changed, added, deleted) = parse(existingJson) diff parse(toCheckJson)

    log.debug("changed="+changed+";added="+added+";deleted="+deleted)
    
    if (changed == JNothing && added == JNothing && deleted == JNothing) {
      return false
    } else {
      return true
    }

  }

  def update(uri: String,vivo: Vivo, solr: SolrServer) = {
    updateAll(List(uri),vivo,solr)
  }


  def buildDoc(uri: String,vivo: Vivo): Option[SolrInputDocument] = {
    buildPerson(uri,vivo).foreach{ p =>

      var person:Person = p.copy()
      val existing = checkExisting(p.uri)
      
      if (existing.isDefined && existing.get.updatedAt.isDefined) {
        // NOTE: need to compare with a person with the same updatedAt value so 
        // it doesn't diff merely on that field alone
        val changes:Boolean = hasChanges(existing.get, p.copy(updatedAt=existing.get.updatedAt))

        if (!changes) {
          // if we are skipping (no changes) reset updated at
          person = p.copy(updatedAt = existing.get.updatedAt)
          log.info(String.format("Skipping index for %s. No changes detected", uri))
       } 
      }
     
      val solrDoc = new SolrInputDocument()
      
      solrDoc.addField("alternateId", person.personAttributes.get("alternateId").get)
      solrDoc.addField("id",person.uri)
      solrDoc.addField("group","people")

      val personJson = person.toJson

      solrDoc.addField("json",personJson)
      solrDoc.addField("updatedAt", person.updatedAt.get)
      solrDoc.addField("active_b",true)
      person.uris.map {uri => solrDoc.addField("uris",uri)}
     
      return Option(solrDoc)
      
    }
    return None
  }

   def updateDoc(uri: String,vivo: Vivo): Option[SolrInputDocument] = {
    buildPerson(uri,vivo).foreach{ p =>

      var person:Person = p.copy()
      val solrDoc = new SolrInputDocument()
      
      solrDoc.addField("id",person.uri)
      solrDoc.addField("alternateId", person.personAttributes.get("alternateId").get)

      val dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
      //solrDoc.set("updatedAt",dateFormatter.format(Calendar.getInstance().getTime()));
      //solrDoc.addField("active_b",false)

      /*HashMap<String, Object> date_value = new HashMap<String, Object>(); 
      date_value.put("set",dateFormatter.format(Calendar.getInstance().getTime()));
      solrDoc.addField("updatedAt",date_value)

      HashMap<String, Object> bool_value = new HashMap<String, Object>(); 
      bool_value.put("set",false); 
      solrDoc.set("active_b",bool_value)*/

      /*Map<String,Object> fieldModifier = new HashMap<>(1);
      fieldModifier.put("set",false);
      sdoc.addField("active_b", fieldModifier);  */

      solrDoc.setField("updatedAt",dateFormatter.format(Calendar.getInstance().getTime()));
      solrDoc.setField("active_b",false);
      
      //solrDoc.removeField("active_b");
      //solrDoc.addField("active_b",false);

      person.uris.map {uri => solrDoc.addField("uris",uri)}
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
      log.debug("pull artisticEvents")
      val artisticEvents = ArtisticEvent.fromUri(vivo, uriContext)
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
      log.debug("pull academic positions")
      val academicPositions     = AcademicPosition.fromUri(vivo, uriContext)
      log.debug("pull gifts")
      val gifts     = Gift.fromUri(vivo, uriContext)
      log.debug("pull licenses")
      val licenses     = License.fromUri(vivo, uriContext)

      log.debug("pull past appointments")
      var pastAppointments = PastAppointment.fromUri(vivo, uriContext)

      var now = new Date
 
      var cvInfo = new PersonCVInfo(gifts, academicPositions, licenses, pastAppointments)

      var p = Person.build(uri, Option.apply(true), Option.apply(now), personData.head, 
                           pubs, awards,
                           artisticWorks, artisticEvents, 
                           grants, courses,
                           professionalActivities, positions,
                           addresses, educations, rAreas, webpages,
                           geoFocus, newsfeeds, Option.apply(cvInfo))
      log.info("buildPerson uri: " + uri)
      return Option(p)
    }
    None
  }
}

