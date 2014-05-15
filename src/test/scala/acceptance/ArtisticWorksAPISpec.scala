package edu.duke.oit.test.acceptance

import edu.duke.oit.vw._

import org.scalatra.test.specs2._

class ArtisticWorksAPISpec extends ScalatraSpec { def is =
  "The Artistic Works json feed should" ^
    "be available"                      ! be_available^
    "return nothing for bad uri"        ! return_nothing_for_bad_uri^
    "return empty for non-artist"       ! return_empty^
                                        end

  addServlet(classOf[WidgetsServlet], "/*")
  addFilter(classOf[edu.duke.oit.vw.scalatra.WidgetsFilter], "/*")

  def be_available = get("/api/v0.9/people/artistic_works/all.json?uri=uri") {
    status must_== 200
  }

  def return_nothing_for_bad_uri =
    get("/api/v0.9/people/artistic_works/all.json?uri=uri") {
      body must_== "Not Found"
    }

  def return_empty = 
    get("/api/v0.9/people/artistic_works/all.json?uri=http://vivo.duke.edu/person1") {
      body must_== "[]"
    }
}
