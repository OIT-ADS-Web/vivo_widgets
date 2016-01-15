package edu.duke.oit.vw.models

import org.apache.solr.client.solrj.{SolrServer,SolrQuery}
import org.apache.solr.client.solrj.response.FacetField
import org.apache.solr.common.{SolrInputDocument,SolrDocumentList,SolrDocument}
import java.util.ArrayList
import scala.collection.JavaConversions._

import edu.duke.oit.vw.solr._
import edu.duke.oit.vw.utils._

// use scala collections with java iterators
import scala.collection.JavaConversions._

trait SolrModel {

  def getDocumentById(id: String,solr: SolrServer): Option[SolrDocument] = {
    val query = new SolrQuery().setQuery("id:\"" + id + "\"")
    getBySolrQuery(query, solr)
  }

  def getDocumentByAlternateId(alternateId: String,solr: SolrServer): Option[SolrDocument] = {
    val query = new SolrQuery().setQuery("alternateId:\"" + alternateId + "\"")
    getBySolrQuery(query, solr)
  }

  def getDocumentByIdOrAlternateId(idOrAlternateId: String, solr: SolrServer): Option[SolrDocument] = {
    val doc = getDocumentById(idOrAlternateId, solr)
    doc.isEmpty match {
      case true => getDocumentByAlternateId(idOrAlternateId, solr)
      case _ => doc
    }
  }

  /*
  def getDocumentByUpdatedDate(since: Date,solr: SolrServer): Option[SolrDocument] = {
    val query = new SolrQuery().setQuery("updatedAt:\" >= " + since + "\"")
    getBySolrQuery(query, solr)
  }
  */
  
  protected def getBySolrQuery(query: SolrQuery, solr: SolrServer): Option[SolrDocument] = {
    val docList = solr.query(query).getResults()
    if (docList.getNumFound() > 0) {
      Option(docList.head)
    } else {
      None
    }
  }


  def search(queryString: String, solr: SolrServer): VivoSearchResult = {
    val query = new SolrQuery().setQuery(queryString).addFacetField("classgroup").setFacetMinCount(1).setRows(1000)
    val response = solr.query(query)

    val docList = response.getResults()
    val items = parseItemList(docList)
    items.size match {
      case 0 => new VivoSearchResult(0,Map(),List())
      case _ => {
        val facetList = response.getLimitingFacets().toList
        new VivoSearchResult(items.size.toLong,parseFacetMap(facetList),items)
      }
    }
  }

  protected

  def parseItemList(docList: SolrDocumentList): List[VivoSearchResultItem] = {
    docList.toList filter  (_.get("classgroup") != null ) map { doc =>
      new VivoSearchResultItem(doc.get("URI").toString,
                               doc.get("nameRaw").toString,
                               doc.get("classgroup") match {
                                 case a: ArrayList[String] => parseClassGroupName(a(0))
                                 case s: String => parseClassGroupName(s)
                                 case _ => ""
                               })
    }
  }

  def parseClassGroupName(classgroup: String): String = {
    classgroup.replace("http://vivoweb.org/ontology#vitroClassGroup","")
  }

  def parseTypeName(vivoType: String): String = {
    vivoType.split("/").last.split("#").last
  }

  def parseFacetMap(facetList: List[FacetField]): Map[String,Long] = {
    facetList.size match {
      case 0 => Map()
      case _ => facetList(0).getValues().map { f => (parseClassGroupName(f.getName),f.getCount) }.toMap
    }
  }

}

