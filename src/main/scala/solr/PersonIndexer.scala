package edu.duke.oit.vw.solr

import org.apache.solr.client.solrj.SolrServer
import org.apache.solr.common.SolrInputDocument

import edu.duke.oit.vw.utils._
import edu.duke.oit.vw.scalatra.ScalateTemplateStringify

import com.hp.hpl.jena.rdf.model.ModelFactory
import edu.duke.oit.vw.jena.Sparqler

import java.util.NoSuchElementException

object PersonIndexer extends SimpleConversion 
  with Timer
  with WidgetLogging 
{

  def index(uri: String,vivo: Vivo, solr: SolrServer, useCache: Boolean = false) = {
    try {
      val uriContext = Map("uri" -> uri)

      val personData = vivo.selectFromTemplate("sparql/personData.ssp", uriContext, useCache)
      if (personData.size > 0) {

        // val publicationData  = vivo.selectFromTemplate("sparql/publications.ssp", uriContext, useCache)
        // val pubs             = publicationData.map(Publication.build(vivo, _)).asInstanceOf[List[Publication]]
        val pubs = Publication.fromUri(vivo, uriContext, useCache)

        val grantData = vivo.selectFromTemplate("sparql/grants.ssp", uriContext, useCache)
        val grants    = grantData.map(Grant.build(_)).asInstanceOf[List[Grant]]

        // val courseData  = vivo.selectFromTemplate("sparql/courses.ssp", uriContext, useCache)
        // val courses     = courseData.map(Course.build(_)).asInstanceOf[List[Course]]
        val courses = Course.fromUri(vivo, uriContext, useCache)

        val p = Person.build(uri, personData(0), pubs, grants, courses)
        timer("add solr doc") {
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

