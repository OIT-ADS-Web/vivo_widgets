package edu.duke.oit.vw.solr

import org.apache.solr.client.solrj.SolrServer
import org.apache.solr.common.SolrInputDocument

import edu.duke.oit.vw.utils._
import edu.duke.oit.vw.models._
import edu.duke.oit.vw.scalatra.ScalateTemplateStringify

import com.hp.hpl.jena.rdf.model.ModelFactory
import edu.duke.oit.vw.jena.Sparqler

object OrganizationIndexer extends SimpleConversion 
  with Timer
  with ScalateTemplateStringify 
  with WidgetLogging {

  def index(uri: String,vivo: Vivo,solr: SolrServer) = {
    val uriContext = Map("uri" -> uri)

    val organizationData = vivo.selectFromTemplate("sparql/organizationData.ssp", uriContext)
    if (organizationData.size > 0) {
      val grants = Grant.fromUri(vivo, uriContext, "sparql/organization/grants.ssp")
      val people = PersonReference.fromUri(vivo, uriContext)

      val o = Organization.build(uri, organizationData.head, people, grants)
      timer("add org to solr") {
        val solrDoc = new SolrInputDocument()
        solrDoc.addField("id",o.uri)
        solrDoc.addField("group","organizations")
        solrDoc.addField("json",o.toJson)
        o.uris.map {uri => solrDoc.addField("uris",uri)}
        solr.add(solrDoc)
      }
    }
  }

}
