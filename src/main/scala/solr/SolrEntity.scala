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

}

