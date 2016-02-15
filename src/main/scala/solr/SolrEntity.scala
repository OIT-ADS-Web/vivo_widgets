package edu.duke.oit.vw.solr

import edu.duke.oit.vw.scalatra.WidgetsConfig
import edu.duke.oit.vw.models.SolrModel
import edu.duke.oit.vw.models.PersonExtraction
import edu.duke.oit.vw.models.OrganizationExtraction

import java.util.Date

import org.apache.solr.client.solrj.{SolrServer,SolrQuery}
//import org.apache.solr.client.solrj.response.FacetField
import org.apache.solr.common.{SolrInputDocument,SolrDocumentList,SolrDocument}
import java.util.ArrayList
import scala.collection.JavaConversions._

import edu.duke.oit.vw.solr._
import edu.duke.oit.vw.utils._


object SolrEntity extends SolrModel {
  
  def getByUri(uri:String): Option[Any] = {
    this.getDocumentByIdOrAlternateId(uri, WidgetsConfig.widgetServer) match {
      case Some(solrDocument) => {
        solrDocument.getFieldValue("group").asInstanceOf[String] match {
          case "people" => {
            Some(PersonExtraction(solrDocument.getFieldValue("json").asInstanceOf[String]))
          }
          case "organizations" => {
            Some(OrganizationExtraction(solrDocument.getFieldValue("json").asInstanceOf[String]))
          }
          case _ => None
        }
      }
      case _ => None
    }

  }

/*
  def searchByUpdatedAt(since: Date): List[WidgetsSearchResultItem] = {
    // NOTE: syntax for search is like this: updatedAt:[2016-02-10T00:00:00Z TO NOW]
   
    //this.searchByUpdatedAt(date, WidgetsConfig.widgetServer) match {
      


    //}

    val solr = WidgetsConfig.widgetServer
    val format = new java.text.SimpleDateFormat("YYYY-MM-dd'T'00:00:00'Z'")
    //see: http://stackoverflow.com/questions/26037324/solrj-date-request

    val dateInput = format.format(since)

    // FIXME: need to either do to NOW or to %s secondDate
    //
    //val queryString = String.format("updatedAt:[%s TO NOW]", ClientUtils.escapeQueryChars(dateInput));
    val queryString = String.format("updatedAt:[%s TO NOW]", dateInput)
    //val queryString = "id:\"https://scholars.duke.edu/individual/per4284062\""

    log.debug("searching for "+ queryString)

    //val query = new SolrQuery().setQuery(queryString).addFacetField("classgroup").setFacetMinCount(1).setRows(1000)
    val query = new SolrQuery()
   
    //val query = new SolrQuery()
    //query.setQuery("id:\"" + id + "\"")
    query.setQuery(queryString)
    query.setShowDebugInfo(true)
    //query.addFacetField("classgroup").setFacetMinCount(1).setRows(1000)

    val response = solr.query(query)
    
    val docList = response.getResults()
    log.debug("docList="+docList)

    val items = parseWidgetsItemList(docList)
 
    return items

  }


  def parseWidgetsItemList(docList: SolrDocumentList): List[WidgetsSearchResultItem] = {
    docList.toList filter  (_.get("group") != null ) map { doc =>
      new WidgetsSearchResultItem(doc.get("id").toString)
    }
  }

*/

}

