package edu.duke.oit.vw.solr

import edu.duke.oit.vw.scalatra.ScalateTemplateStringify

import edu.duke.oit.vw.jena._
import edu.duke.oit.vw.utils._

import com.hp.hpl.jena.sdb.SDBFactory
import com.hp.hpl.jena.sdb.sql.SDBConnection
import com.hp.hpl.jena.query._
import com.hp.hpl.jena.rdf.model.{Model => JModel, ModelFactory}


class Vivo(url: String, user: String, password: String, dbType: String, driver: String) 
  extends ScalateTemplateStringify
  with WidgetLogging 
  with Timer {

  def loadDriver() = {
    Class.forName(driver)
  }

  def initializeJenaCache() = {
    loadDriver()
    JenaCache.setFromDatabase(new JenaConnectionType(dbType),
                              "urn:x-arq:UnionGraph")
  }

  def setupConnectionPool() = {
    Jena.setupConnectionPool(new JenaConnectionInfo(url,user,password,dbType), driver)
  }

  // def queryJenaCache(sparql: String): List[Map[Symbol,String]] = {
  //   JenaCache.getModel match {
  //     case Some(m: JModel) => true
  //     case _ => initializeJenaCache()
  //   }
  //   JenaCache.queryModel(sparql)
  // }

  def queryLive(sparql: String): List[Map[Symbol,String]] = {
    Jena.sdbModel(new JenaConnectionType(dbType),"urn:x-arq:UnionGraph") { queryModel =>
      Sparqler.selectingFromModel(queryModel,sparql) { resultSet => Sparqler.simpleResults(resultSet) }
    }
  }

  def select(sparql: String): List[Map[Symbol,String]] = {
    queryLive(sparql)
  }

  def numPeople() = select(renderFromClassPath("sparql/numberOfPeople.ssp"))(0)('numPeople).toInt

  def selectFromTemplate(sparqlTemplate: String, 
                         context: Map[String, Any]): List[Map[Symbol, String]] = {
    val sparql = renderFromClassPath(sparqlTemplate, context)
    log.debug("sparql: " + sparql)
    timer("select $sparqlTemplate") { select(sparql) }.asInstanceOf[List[Map[Symbol, String]]]
  }

}
