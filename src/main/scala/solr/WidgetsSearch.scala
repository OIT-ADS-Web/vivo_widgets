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

object WidgetsSearcher extends SolrModel {

    def searchByUpdatedAt(start: Date, end: Option[Date], solr:SolrServer): List[WidgetsSearchResultItem] = {
    // NOTE: syntax for searh in SOLR looks this: 
    // updatedAt:[2016-02-10T00:00:00Z TO NOW]
   
    val format = new java.text.SimpleDateFormat("YYYY-MM-dd'T'00:00:00'Z'")
    //see: http://stackoverflow.com/questions/26037324/solrj-date-request

    val dateStart = format.format(start)
    
    //val dateEnd = format.format(end)
    //http://blog.originate.com/blog/2014/06/15/idiomatic-scala-your-options-do-not-match/

    //val dateEnd = end.fold("NOW")(_ + format.format(end))
    
    val dateEnd = end match {
      case Some(end) => format.format(end)
      case None => "NOW"
    }

 
    // NOTE: need to either do to NOW or to %s secondDate
    //val dateEnd = "NOW"


    val queryString = String.format("updatedAt:[%s TO %s]", dateStart, dateEnd)

    log.debug("searching for "+ queryString)

    val query = new SolrQuery()
   
    query.setQuery(queryString)
    query.setShowDebugInfo(true)

    val response = solr.query(query)
    
    val docList = response.getResults()

    val items = parseWidgetsItemList(docList)
 
    // FIXME:do we need a 'return' statement
    return items

  }


  def parseWidgetsItemList(docList: SolrDocumentList): List[WidgetsSearchResultItem] = {
    docList.toList filter  (_.get("group") != null ) map { doc =>
      new WidgetsSearchResultItem(doc.get("id").toString)
    }
  }

}

class WidgetsSearchResult(val numFound: Long, val  items: List[WidgetsSearchResultItem]) extends AddToJson

class WidgetsSearchResultItem(val uri: String)
