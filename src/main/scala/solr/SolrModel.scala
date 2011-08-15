package edu.duke.oit.vw.solr

import org.apache.solr.client.solrj.{SolrServer,SolrQuery}
import org.apache.solr.client.solrj.response.FacetField
import org.apache.solr.common.{SolrInputDocument,SolrDocumentList,SolrDocument}
import java.util.ArrayList
import scala.collection.JavaConversions._

import edu.duke.oit.vw.utils._

// use scala collections with java iterators
import scala.collection.JavaConversions._

trait SolrModel {

  def getDocumentById(id: String,solr: SolrServer): Option[SolrDocument] = {
    val query = new SolrQuery().setQuery("id:\"" + id + "\"")
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

/**
 * The <code>extraItems</code> value is a catch all hash that
 * can be used to add attributes from the originating SPARQL
 * that aren't explicitly defined.
 * <code>extraItems</code> must a Map[String, String].
 */
class ExtraItems(extraItems:Option[Map[String, String]]) extends ToMethods with AddToJson {
  
  import net.liftweb.json.JsonDSL._
  import net.liftweb.json.{JsonAST,Printer,Extraction}
  import net.liftweb.json.JsonAST.{JField,JObject}

  /**
   * 
   * @param key the key string to look for in the extraItems Map
   * @return Some(String) with the string value from the extraItems Map;
   *         otherwise None
   */
  def \(key:String): String = {
    extraItems match {
      case Some(m) => m.get(key).orNull
      case _ => null
    }
  }

  def get(key:String): String = {
    \(key)
  }

  def getOrElse(key:String,default:String): String = {
    \(key) match {
      case null => default
      case m:String => m
      case _ => null
    }
  }

  def uris():List[String] = {
    extraItems match {
      case Some(eitems) => {
        val l = for (i <- eitems if i._2.startsWith("http")) yield i._2
        l.toList
      }
      case _ => List()
    }
  }

}
