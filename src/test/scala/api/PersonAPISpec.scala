package edu.duke.oit.api.test

import org.scalatra.test.specs2._

import edu.duke.oit.vw._
import net.liftweb.json._
import edu.duke.oit.vw.scalatra._
import edu.duke.oit.vw.solr._
import edu.duke.oit.vw.models._
import edu.duke.oit.test.helpers.TestServers
import org.specs2.specification.Step

class PersonApiSpec extends ScalatraSpec { def is = s2"""
  The Person API should return     ${ Step(getJson)}
    uri                            $e1
    vivoType                       $e2
    label                          $e3
    title                          $e4
    attributes(lastName)           $e5
    attributes(firstName)          $e6
    attributes(preferredTitle)     $e7
    attributes(alternateId)        $e8
    attributes(middleName)         $e9
    attributes(primaryEmail)       $e10
    attributes(overview)           $e11
    attributes(mentorshipOverview) $e12
  """

  var json:Map[String, Any] = _
  var attributes:Map[String, Any] = _

  def getJson = {
    get("/api/v0.9/people/complete/all.json?uri=http://localhost/individual/n503") {
      json = JsonParser.parse(body).values.asInstanceOf[Map[String, Any]]
      attributes = json("attributes").asInstanceOf[Map[String, Any]]
    }
  }

  addFilter(new WidgetsFilter, "/*")
  val vivo = TestServers.vivo
  vivo.setupConnectionPool()
  TestServers.loadSampleData("/src/test/resources/minimal_person.rdf")
  val solrServer = TestServers.widgetSolr
  val indexer = new VivoSolrIndexer(vivo, solrServer)

  indexer.indexPeople()

  def e1 = { json("uri") must_== "http://localhost/individual/n503" }
  def e2 = { json("vivoType") must_== "http://vivoweb.org/ontology/core#FacultyMember" } 
  def e3 = { json("label") must_== "Smith, Richard" } 
  def e4 = { json("title") must_== "Programming CIO" }
  def e5 = { attributes("lastName") must_== "Smith" }
  def e6 = { attributes("firstName") must_== "Richard" }
  def e7 = { attributes("preferredTitle") must_== ("Programming CIO") }
  def e8 = { attributes("alternateId") must_== "0123456" }
  def e9 = { attributes("middleName") must_== "Big" }
  def e10 = { attributes("primaryEmail") must_== "rsmith@example.com" }
  def e11 = { attributes("overview") must_== "This is an overview." }
  def e12 = { attributes("mentorshipOverview") must_== "A mentor overview." }
  }
