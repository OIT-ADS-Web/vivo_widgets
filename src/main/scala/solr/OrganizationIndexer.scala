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

import net.liftweb.json._

import edu.duke.oit.vw.scalatra.WidgetsConfig
 
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

  def checkExisting(uri: String): Option[Organization] = {
    val vsi = new VivoSolrIndexer(WidgetsConfig.server, WidgetsConfig.widgetServer)
 
    val organization = vsi.getOrganization(uri)

    return organization
  }

  def hasChanges(existing: Organization, toCheck: Organization): Boolean = {
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


  def buildDoc(uri: String,vivo: Vivo): Option[SolrInputDocument] = {
    buildOrganization(uri,vivo).foreach{ o =>

      var organization:Organization = o.copy()
      val existing = checkExisting(o.uri)
      
      if (existing.isDefined && existing.get.updatedAt.isDefined) {
        // NOTE: need to compare with a person with the same updatedAt value so 
        // it doesn't diff merely on that field alone
        val changes:Boolean = hasChanges(existing.get, o.copy(updatedAt=existing.get.updatedAt))

        if (!changes) {
          // if we are skipping (no changes) reset updated at
          organization = o.copy(updatedAt = existing.get.updatedAt)
          log.debug(String.format("Skipping index for %s. No changes detected", uri))
       } 
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
