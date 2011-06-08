package edu.duke.oit.jena.connection

import com.hp.hpl.jena.db.DBConnection
import com.hp.hpl.jena.ontology.OntModel

import com.hp.hpl.jena.sdb.SDBFactory
import com.hp.hpl.jena.sdb.Store
import com.hp.hpl.jena.sdb.StoreDesc
import com.hp.hpl.jena.sdb.sql.JDBC
import com.hp.hpl.jena.sdb.sql.SDBConnection
import com.hp.hpl.jena.sdb.store.DatabaseType
import com.hp.hpl.jena.sdb.store.DatasetStore
import com.hp.hpl.jena.sdb.store.LayoutType
import com.hp.hpl.jena.sdb.util.StoreUtils

import com.hp.hpl.jena.graph.Graph
import com.hp.hpl.jena.rdf.model.{Resource, ModelFactory, ModelMaker, Model => JModel}

import _root_.scala.None

class JenaConnectionInfo(val url: String,
                         val user: String,
                         val password: String,
                         val dbType: String)

object Jena {

  // RDB

  def connection(cInfo: JenaConnectionInfo)(mFactory: (ModelMaker) => Unit) = {
    val dbConn = new DBConnection(cInfo.url, cInfo.user, cInfo.password, cInfo.dbType);
    val mf: ModelMaker = ModelFactory.createModelRDBMaker(dbConn)
    try {
      mFactory(mf)
    } finally {
      mf.close
    }
  }

  // SDB methods

  // might want to make these configurable globally
  val layoutType = "layout2/hash"
  val defaultDbType = "MySQL"

  def storeDesc(dbType: Option[String] = None): StoreDesc = {
    val dt = dbType.getOrElse(defaultDbType)
    new StoreDesc(LayoutType.fetch(layoutType), DatabaseType.fetch(dt))
  }

  def connectionSDB(cInfo: JenaConnectionInfo)(mFactory: (SDBConnection) => Unit) = {
    val sdbConnection = new SDBConnection(cInfo.url, cInfo.user, cInfo.password)
    try {
      mFactory(sdbConnection)
    }
    finally {
      sdbConnection.close
    }
  }

  def truncateAndCreateStore(cInfo: JenaConnectionInfo) = {
    connectionSDB(cInfo) { sdbConnection =>
      val store = SDBFactory.connectStore(sdbConnection, storeDesc(Some(cInfo.dbType)))
      try {
        if (StoreUtils.isFormatted(store)) {
          store.getTableFormatter().truncate()
        }
        store.getTableFormatter().create()
      }
      finally {
        store.close
      }
    }
  }

  def sdbStore(cInfo: JenaConnectionInfo)(mFactory: (Store) => Unit) = {
    connectionSDB(cInfo) {
      sdbConnection =>
        val store = SDBFactory.connectStore(sdbConnection, storeDesc(Some(cInfo.dbType)))
      try {
        val graph: Graph = SDBFactory.connectDefaultGraph(storeDesc(Some(cInfo.dbType)))
        mFactory(store)
      }
      finally {
        store.close
      }
    }
  }

  def sdbModel(cInfo: JenaConnectionInfo, modelUri: String)(mFactory: (JModel) => Unit) = {
    sdbStore(cInfo) {
      store =>
        mFactory(SDBFactory.connectNamedModel(store, modelUri))
    }
  }

  def sdbDefaultModel(cInfo: JenaConnectionInfo)(mFactory: (JModel) => Unit) = {
    sdbStore(cInfo) {
      store =>
        mFactory(SDBFactory.connectDefaultModel(store))
    }
  }


  // ontogoly methods

  def ontConnection(model: JModel)(ontologyModel: (OntModel) => Unit) = {
    val ontology: OntModel = ModelFactory.createOntologyModel() // createModelRDBMaker(dbConn)
    ontology.addSubModel(model)
    try {
      ontologyModel(ontology)
    } finally {
      ontology.close
    }
  }


}

