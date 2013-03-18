package edu.duke.oit.vw.solr

import edu.duke.oit.vw.scalatra.ScalateTemplateStringify

import edu.duke.oit.vw.jena._
import com.hp.hpl.jena.sdb.SDBFactory
import com.hp.hpl.jena.sdb.sql.SDBConnection
import com.hp.hpl.jena.query._
import com.hp.hpl.jena.rdf.model.{Model => JModel, ModelFactory}

class Vivo(url: String, user: String, password: String, dbType: String, driver: String) extends ScalateTemplateStringify{

  def loadDriver() = {
    Class.forName(driver)
  }

  def initializeJenaCache() = {
    loadDriver()
    JenaCache.setFromDatabase(new JenaConnectionInfo(url,user,password,dbType),
                              "urn:x-arq:UnionGraph")
  }

  def setupConnectionPool() = {
    Jena.setupConnectionPool(new JenaConnectionInfo(url,user,password,dbType), driver)
  }

  def queryJenaCache(sparql: String): List[Map[Symbol,String]] = {
    JenaCache.getModel match {
      case Some(m: JModel) => true
      case _ => initializeJenaCache()
    }
    JenaCache.queryModel(sparql)
  }

  def queryLive(sparql: String): List[Map[Symbol,String]] = {
    Jena.sdbModel(new JenaConnectionInfo(url,user,password,dbType),"urn:x-arq:UnionGraph") { queryModel =>
      Sparqler.selectingFromModel(queryModel,sparql) { resultSet => Sparqler.simpleResults(resultSet) }
    }
  }

  def select(sparql: String, useCache: Boolean = false): List[Map[Symbol,String]] = {
    if (useCache) {
      queryJenaCache(sparql)
    } else {
      queryLive(sparql)
    }
  }

  def numPeople(useCache: Boolean = false) = select(renderFromClassPath("sparql/numberOfPeople.ssp"),useCache)(0)('numPeople).toInt

}
