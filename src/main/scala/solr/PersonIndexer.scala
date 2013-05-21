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
    try {
      val uriContext = Map("uri" -> uri)

      val personData = vivo.selectFromTemplate("sparql/personData.ssp", uriContext)
      if (personData.size > 0) {

        val pubs = Publication.fromUri(vivo, uriContext)
        val grants = Grant.fromUri(vivo, uriContext)
        val courses = Course.fromUri(vivo, uriContext)
        val positions = Position.fromUri(vivo, uriContext)

        val p = Person.build(uri, personData.head, pubs, grants, courses)
        timer("add person to solr") {
          val solrDoc = new SolrInputDocument()
          solrDoc.addField("id",p.uri)
          solrDoc.addField("group","people")
          solrDoc.addField("json",p.toJson)
          p.uris.map {uri => solrDoc.addField("uris",uri)}
          solr.add(solrDoc)
        }
      }
      
    } catch {
      case e:NoSuchElementException => {
        Console.err.println("PersonIndexer error: " + e.toString)
        e.printStackTrace()
      }
    }
  }

}

