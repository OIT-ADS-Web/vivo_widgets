package edu.duke.oit.vw.jena.test

import com.hp.hpl.jena.rdf.model.{ModelFactory, Model => JModel}
import org.specs2.mutable._

import org.scardf.jena.JenaGraph
import org.scardf._
import org.scardf.Node
import org.scardf.NodeConverter._

import edu.duke.oit.vw.utils._
import edu.duke.oit.vw.jena._

object JenaConnectionSpec extends Specification with Timer with SimpleConversion with Tags {

  skipAllIf(true)

  val dbURL = "jdbc:mysql://localhost:3306/dev_vivo"
  val dbUser = "vivodev"
  val dbPassword = "local_vivo_work"
  val dbType = "MYSQL"
  var className = "com.mysql.jdbc.Driver"

  Class.forName(className)

  def jenaConnection = new JenaConnectionInfo(dbURL, dbUser, dbPassword, dbType)

  "A Jena Connection" should {

    "populate basic connection info" in {
      val c = new JenaConnectionInfo("url", "user", "pass", "type")
      c.url must_== "url"
    }

    "run queries againts sdb" in {
      Jena.sdbModel(jenaConnection, "urn:x-arq:UnionGraph") {
        dbModel =>
          println("------------------------------------\nMemory Model:")
          var model = ModelFactory.createDefaultModel
          timer("loading memory model: ") {
            model.add(dbModel)
          }
          QueryRunner.run(model)
          println("------------------------------------\ndbModel:")
          QueryRunner.run(dbModel)
      }


      Jena.sdbModel(jenaConnection, "urn:x-arq:UnionGraph") {
        dbModel =>
          println("------------------------------------\ndbModel:")
          QueryRunner.run(dbModel)
          println("------------------------------------\nMemory Model:")
          var model = ModelFactory.createDefaultModel
          timer("loading memory model: ") {
            model.add(dbModel)
          }
          QueryRunner.run(model)
      }

      println("------------------------------------\nMemory Model (disconnect from db):")
      var tModel = ModelFactory.createDefaultModel
      Jena.sdbModel(jenaConnection, "urn:x-arq:UnionGraph") {
        dbModel =>
          timer("loading memory model: ") {
            tModel.add(dbModel)
          }
      }
      println("sleeping...")
      Thread.sleep(10000)
      QueryRunner.run(tModel)
      success
    }

    "use Jena cache" in {
      JenaCache.setFromDatabase(jenaConnection, "urn:x-arq:UnionGraph")
      timer("results 1") {
        val results = JenaCache.queryModel(QueryRunner.queryString3)
        println(" r: " + results.size)
      }
      timer("results 2") {
        val results2 = JenaCache.queryModel(QueryRunner.queryString3)
        println("r2: " + results2.size)
      }
      timer("results 3") {
        val results2 = JenaCache.queryModel(QueryRunner.queryString2)
        println("r3: " + results2.size)
      }
      for (i <- 1 to 10) {
        timer("results 4") {
          val results2 = JenaCache.queryModel(QueryRunner.queryString3)
          println("r4: " + results2.size)
        }
      }
      success
     
    }

  }


}

object QueryRunner extends SimpleConversion with Timer {

  def run(dbModel: JModel) = {
    //var model = ModelFactory.createDefaultModel
    //model.add(dbModel)
    //var model = dbModel

    var jg = new JenaGraph(dbModel)
    runMultipleTimes(jg, queryString, "query 1 /", 3)
    runMultipleTimes(jg, queryString2, "query 2 /", 3)
    runMultipleTimes(jg, queryString3, "query 3 /", 3)
  }

  def runMultipleTimes(graph: JenaGraph, query: String, label: String, count: Int) = {
    var rows = 0
    for (i <- 0.until(count)) {
      rows = queryTimerDefault(graph, query, (">> run " + i + " | " + label))
    }
    println("Rows returned: " + rows)
  }

  def queryTimerDefault(graph: JenaGraph, query: String, label: String): Int = {
    queryTimer(graph, query, label, (row: Map[QVar, Node]) => {
      var c = (getString(row('x)) + " / " + getString(row('y)) + " / " + getString(row('y)))
    })
  }

  def queryTimer(graph: JenaGraph, query: String, label: String, fun: (Map[QVar, Node]) => Unit): Int = {
    var count = 0
    timer(label) {
      var a = graph.select(query)
      println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^")
      println(a.getClass)
      println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^")
      count = a.size
      for (r <- a) {
        fun(r)
      }
    }
    return count
  }

  var queryString = """
    PREFIX rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
    PREFIX rdfs:  <http://www.w3.org/2000/01/rdf-schema#>
    PREFIX xsd:   <http://www.w3.org/2001/XMLSchema#>
    PREFIX owl:   <http://www.w3.org/2002/07/owl#>
    PREFIX swrl:  <http://www.w3.org/2003/11/swrl#>
    PREFIX swrlb: <http://www.w3.org/2003/11/swrlb#>
    PREFIX vitro: <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#>
    PREFIX p.1: <http://purl.obolibrary.org/obo/>
    PREFIX bibo: <http://purl.org/ontology/bibo/>
    PREFIX dcelem: <http://purl.org/dc/elements/1.1/>
    PREFIX dcterms: <http://purl.org/dc/terms/>
    PREFIX event: <http://purl.org/NET/c4dm/event.owl#>
    PREFIX foaf: <http://xmlns.com/foaf/0.1/>
    PREFIX geo: <http://aims.fao.org/aos/geopolitical.owl#>
    PREFIX pvs: <http://vivoweb.org/ontology/provenance-support#>
    PREFIX scires: <http://vivoweb.org/ontology/scientific-research#>
    PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
    PREFIX p.2: <http://vitro.mannlib.cornell.edu/ns/vitro/public#>
    PREFIX core: <http://vivoweb.org/ontology/core#>

    #
    # This example query gets 20 geographic locations
    # and (if available) their labels
    #

    #authorInAuthorship

    SELECT ?x ?y ?pLabel ?aLabel
    WHERE
    {
         ?x core:authorInAuthorship ?y .
         ?x rdfs:label ?pLabel .
         ?y core:linkedInformationResource ?linkedIR .
         ?linkedIR rdfs:label ?aLabel
    }
    LIMIT 20
    """

  var queryString2 = """
    PREFIX rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
    PREFIX rdfs:  <http://www.w3.org/2000/01/rdf-schema#>
    PREFIX xsd:   <http://www.w3.org/2001/XMLSchema#>
    PREFIX owl:   <http://www.w3.org/2002/07/owl#>
    PREFIX swrl:  <http://www.w3.org/2003/11/swrl#>
    PREFIX swrlb: <http://www.w3.org/2003/11/swrlb#>
    PREFIX vitro: <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#>
    PREFIX p.1: <http://purl.obolibrary.org/obo/>
    PREFIX bibo: <http://purl.org/ontology/bibo/>
    PREFIX dcelem: <http://purl.org/dc/elements/1.1/>
    PREFIX dcterms: <http://purl.org/dc/terms/>
    PREFIX event: <http://purl.org/NET/c4dm/event.owl#>
    PREFIX foaf: <http://xmlns.com/foaf/0.1/>
    PREFIX geo: <http://aims.fao.org/aos/geopolitical.owl#>
    PREFIX pvs: <http://vivoweb.org/ontology/provenance-support#>
    PREFIX scires: <http://vivoweb.org/ontology/scientific-research#>
    PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
    PREFIX p.2: <http://vitro.mannlib.cornell.edu/ns/vitro/public#>
    PREFIX core: <http://vivoweb.org/ontology/core#>

    #
    # This example query gets 20 geographic locations
    # and (if available) their labels
    #

    #authorInAuthorship

    SELECT ?x ?y
    WHERE
    {
         ?x core:authorInAuthorship ?y
    }
    LIMIT 20
    """


  var queryString3 = """
    PREFIX rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
    PREFIX rdfs:  <http://www.w3.org/2000/01/rdf-schema#>
    PREFIX xsd:   <http://www.w3.org/2001/XMLSchema#>
    PREFIX owl:   <http://www.w3.org/2002/07/owl#>
    PREFIX swrl:  <http://www.w3.org/2003/11/swrl#>
    PREFIX swrlb: <http://www.w3.org/2003/11/swrlb#>
    PREFIX vitro: <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#>
    PREFIX p.1: <http://purl.obolibrary.org/obo/>
    PREFIX bibo: <http://purl.org/ontology/bibo/>
    PREFIX dcelem: <http://purl.org/dc/elements/1.1/>
    PREFIX dcterms: <http://purl.org/dc/terms/>
    PREFIX event: <http://purl.org/NET/c4dm/event.owl#>
    PREFIX foaf: <http://xmlns.com/foaf/0.1/>
    PREFIX geo: <http://aims.fao.org/aos/geopolitical.owl#>
    PREFIX pvs: <http://vivoweb.org/ontology/provenance-support#>
    PREFIX scires: <http://vivoweb.org/ontology/scientific-research#>
    PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
    PREFIX p.2: <http://vitro.mannlib.cornell.edu/ns/vitro/public#>
    PREFIX core: <http://vivoweb.org/ontology/core#>

    #
    # This example query gets 20 geographic locations
    # and (if available) their labels
    #

    #authorInAuthorship

    SELECT ?x ?v ?y
    WHERE
    {
         ?x ?v ?y
    }

    """

}

