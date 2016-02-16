package edu.duke.oit.vw.solr

import org.apache.solr.client.solrj.SolrServer
import org.apache.solr.common.SolrInputDocument

import edu.duke.oit.vw.utils._
import edu.duke.oit.vw.models._
import edu.duke.oit.vw.scalatra.ScalateTemplateStringify

import com.hp.hpl.jena.rdf.model.ModelFactory
import edu.duke.oit.vw.jena.Sparqler
import scala.collection.JavaConversions._

import java.util.Date

object OrganizationIndexer extends SimpleConversion
  with ScalateTemplateStringify
  with WidgetLogging {

  def index(uri: String,vivo: Vivo,solr: SolrServer) = {
    indexAll(List(uri),vivo,solr)
  }

  def indexAll(uris: List[String],vivo: Vivo, solr: SolrServer) = {
    uris.grouped(10).foreach{ groupedUris =>
      val docs = groupedUris.map( uri => buildDoc(uri,vivo)).flatten
      solr.add(docs.toIterable)
    }
  }

  // FIXME: probably a way to centralize this from Person and Organization
  // instead of duplicating
  def detectChanges(existing: Organization, toCheck: Organization): Boolean = {
 
    val existingJson = existing.toJson
    val toCheckJson = toCheck.toJson

    // http://scala-tools.org/mvnsites/liftweb-2.2-RC5/framework/lift-base_2.7.7/scaladocs/net/liftweb/json/Diff.html
    val changed, added, deleted = existingJson diff toCheckJson
 
    if (changed.toString.equals("") && added.toString.equals("") && deleted.toString.equals("")) {
      return false
    } else {
      return true
    }

  }

  def checkExisting(uri: String): Option[Organization] = {

    import edu.duke.oit.vw.solr.VivoSolrIndexer
    import edu.duke.oit.vw.scalatra.WidgetsConfig
 
    val vsi = new VivoSolrIndexer(WidgetsConfig.server, WidgetsConfig.widgetServer)
 
    val organization = vsi.getOrganization(uri)

    return organization
  }



  def buildDoc(uri: String,vivo: Vivo): Option[SolrInputDocument] = {
    buildOrganization(uri,vivo).foreach{ o =>
  
      val existing = checkExisting(o.uri)

      var skip:Boolean = false

      var organization:Organization = o.copy()

      if (existing.isDefined && existing.get.updatedAt.isDefined) {
        val updatedAt = existing.get.updatedAt

        organization = o.copy(updatedAt = updatedAt)

        val changes:Boolean = detectChanges(existing.get, organization)
        skip = !(changes)
      }
      
      if (skip) {
         val updatedAt = existing.get.updatedAt
         organization = o.copy(updatedAt = updatedAt)
         
         log.debug(String.format("Skipping index for %s. No changes detected", uri))
      } else {
        // NOTE: if it IS different, need to just copy the version from buildOrganization
        organization = o.copy()
      }
      
      val solrDoc = new SolrInputDocument()
      
      solrDoc.addField("id",organization.uri)
      solrDoc.addField("group","organizations")
      solrDoc.addField("json",organization.toJson)
      
      solrDoc.addField("updatedAt", organization.updatedAt.get)
      organization.uris.map {uri => solrDoc.addField("uris",uri)}
     
      return Option(solrDoc)
      
    }
    return None
  }

  /*
  def buildDoc(uri: String,vivo: Vivo): Option[SolrInputDocument] = {
    buildOrganization(uri,vivo).foreach{ o =>
      val solrDoc = new SolrInputDocument()
      solrDoc.addField("id",o.uri)
      solrDoc.addField("group","organizations")
      solrDoc.addField("json",o.toJson)
      solrDoc.addField("updatedAt", o.updatedAt)
 
      o.uris.map {uri => solrDoc.addField("uris",uri)}
      return Option(solrDoc)
    }
    return None
  }
  */


  def buildOrganization(uri: String,vivo: Vivo): Option[Organization] = {
    log.debug("pull uri: " + uri)
    val uriContext = Map("uri" -> uri)
    val organizationData = vivo.selectFromTemplate("sparql/organizationData.ssp", uriContext)
    if (organizationData.size > 0) {
      val grants = Grant.fromUri(vivo, uriContext, "sparql/organization/grants.ssp")
      val people = PersonReference.fromUri(vivo, uriContext)

      val now = new Date
      val o = Organization.build(uri, Option.apply(now), organizationData.head, people, grants)
      return Option(o)
     }
     None
  }


}
