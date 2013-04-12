package edu.duke.oit.vw.solr

import edu.duke.oit.vw.scalatra.WidgetsConfig

object SolrEntity extends SolrModel {
  
  def getByUri(uri:String): Option[Any] = {
    this.getDocumentById(uri, WidgetsConfig.widgetServer) match {
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

