package edu.duke.oit.api.test

import org.scalatra.test.specs2._

import edu.duke.oit.vw._
import net.liftweb.json._
import edu.duke.oit.vw.scalatra._
import edu.duke.oit.vw.solr._
import edu.duke.oit.vw.models._
import edu.duke.oit.test.helpers.TestServers

class PersonApiSpec extends ScalatraSpec { def is = s2"""
  The Person API should
    return 'uri' $e1
    return 'vivoType' $e2
    return 'label' $e3
    return 'title' $e4
    return 'attributes(lastName)' $e5
    return 'attributes(firstName)' $e6
    return 'attributes(preferredTitle)' $e7
    return 'attributes(alternateId)' $e8
    return 'attributes(middleName)' $e9
  """

  addFilter(new WidgetsFilter, "/*")
  val vivo = TestServers.vivo
  vivo.setupConnectionPool()
  TestServers.loadSampleData("/src/test/resources/minimal_person.rdf")
  val solrServer = TestServers.widgetSolr
  val indexer = new VivoSolrIndexer(vivo, solrServer)

  indexer.indexPeople()

  def e1 = get("/api/v0.9/people/complete/all.json?uri=http://localhost/individual/n503") {
    val json = JsonParser.parse(body).values.asInstanceOf[Map[String, Any]]
    println("json: " + json)
    json("uri") must_== "http://localhost/individual/n503"
  }

  def e2 = get("/api/v0.9/people/complete/all.json?uri=http://localhost/individual/n503") {
    val json = JsonParser.parse(body).values.asInstanceOf[Map[String, Any]]
    json("vivoType") must_== "http://vivoweb.org/ontology/core#FacultyMember"
  }
    
  def e3 = get("/api/v0.9/people/complete/all.json?uri=http://localhost/individual/n503") {
    val json = JsonParser.parse(body).values.asInstanceOf[Map[String, Any]]
    json("label") must_== "Smith, Richard"
  }
    
  def e4 = get("/api/v0.9/people/complete/all.json?uri=http://localhost/individual/n503") {
    val json = JsonParser.parse(body).values.asInstanceOf[Map[String, Any]]
    json("title") must_== "Programming CIO"
  }
    
  def e5 = get("/api/v0.9/people/complete/all.json?uri=http://localhost/individual/n503") {
    val json = JsonParser.parse(body).values.asInstanceOf[Map[String, Any]]
    val attributes = json("attributes").asInstanceOf[Map[String, Any]]
    println("attributes:" + attributes)
    attributes("lastName") must_== "Smith"
  }
    
  def e6 = get("/api/v0.9/people/complete/all.json?uri=http://localhost/individual/n503") {
    val json = JsonParser.parse(body).values.asInstanceOf[Map[String, Any]]
    val attributes = json("attributes").asInstanceOf[Map[String, Any]]
    attributes("firstName") must_== "Richard"
  }
    
  def e7 = get("/api/v0.9/people/complete/all.json?uri=http://localhost/individual/n503") {
    val json = JsonParser.parse(body).values.asInstanceOf[Map[String, Any]]
    val attributes = json("attributes").asInstanceOf[Map[String, Any]]
    attributes("preferredTitle") must beEqualTo("Programming CIO")
  }
    
  def e8 = get("/api/v0.9/people/complete/all.json?uri=http://localhost/individual/n503") {
    val json = JsonParser.parse(body).values.asInstanceOf[Map[String, Any]]
    val attributes = json("attributes").asInstanceOf[Map[String, Any]]
    attributes("alternateId") must_== "0123456"
  }
    
  def e9 = get("/api/v0.9/people/complete/all.json?uri=http://localhost/individual/n503") {
    val json = JsonParser.parse(body).values.asInstanceOf[Map[String, Any]]
    val attributes = json("attributes").asInstanceOf[Map[String, Any]]
    attributes("middleName") must_== "Big"
  }
    
    //attributes("primaryEmail") must_== "rsmith@example.com"
    //attributes("") must_==
    //attributes("") must_==
    //attributes("") must_==
    //attributes("") must_==
    //attributes("") must_==
    //attributes("") must_==
    //attributes("") must_==
    //attributes("") must_==
    //attributes("") must_==
    //attributes("") must_==
    //attributes("") must_==
}
