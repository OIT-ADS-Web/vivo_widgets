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

  def buildDoc(uri: String,vivo: Vivo): Option[SolrInputDocument] = {
    buildOrganization(uri,vivo).foreach{ o =>
      val solrDoc = new SolrInputDocument()
      solrDoc.addField("id",o.uri)
      solrDoc.addField("group","organizations")
      solrDoc.addField("json",o.toJson)
      solrDoc.addField("updated", new Date, 1.0f)

      o.uris.map {uri => solrDoc.addField("uris",uri)}
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

      val o = Organization.build(uri, organizationData.head, people, grants)
      return Option(o)
     }
     None
  }

}
