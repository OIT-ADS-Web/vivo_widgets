package edu.duke.oit.vw.solr

import edu.duke.oit.vw.utils._
import edu.duke.oit.vw.models.SolrModel

import edu.duke.oit.vw.scalatra.WidgetsConfig
import edu.duke.oit.vw.models.SolrModel

import org.apache.solr.client.solrj.{SolrServer,SolrQuery}
//import org.apache.solr.client.solrj.response.FacetField
import org.apache.solr.common.{SolrInputDocument,SolrDocumentList,SolrDocument}
import java.util.ArrayList
import scala.collection.JavaConversions._

import edu.duke.oit.vw.solr._
import edu.duke.oit.vw.utils._

import java.util.Date
import java.text.SimpleDateFormat

object WidgetsSearcher extends SolrModel {

  def searchByUpdatedAt(since: Date, offset: Integer, solr:SolrServer): WidgetsSearchResult = {
    // NOTE: syntax for searh in SOLR looks this: 
    // updatedAt:[2016-02-10T00:00:00Z TO NOW]
    val format = new SimpleDateFormat("YYYY-MM-dd'T'00:00:00'Z'")
    //see: http://stackoverflow.com/questions/26037324/solrj-date-request

    val dateSince = format.format(since)
    val dateEnd = "NOW"

    val queryString = String.format("updatedAt:[%s TO %s] AND group=people", dateSince, dateEnd)

    val query = new SolrQuery()
   
    query.setQuery(queryString)
    query.setShowDebugInfo(true)

    // FIXME: could either make this configurable or a parameter (or not)
    val maxResults: Integer = 1000

    query.setRows(maxResults)
    query.setStart(offset)

    val response = solr.query(query)
    val docList = response.getResults()

    val items = parseWidgetsItemList(docList)
 
    var searchResult = new WidgetsSearchResult(docList.getNumFound(), docList.getStart(), items)

    return searchResult

  }


  def parseWidgetsItemList(docList: SolrDocumentList): List[WidgetsSearchResultItem] = {
    docList.toList filter  (_.get("group") != null ) map { doc =>
      new WidgetsSearchResultItem(doc.get("id").toString)
    }
  }

}

class WidgetsSearchResult(val numFound: Long, val offset: Long, val  items: List[WidgetsSearchResultItem]) extends AddToJson

class WidgetsSearchResultItem(val uri: String)
