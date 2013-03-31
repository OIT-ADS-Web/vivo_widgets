package edu.duke.oit.solr.test

import org.specs2.mutable._
import edu.duke.oit.vw.solr.Grant

class GrantSpec extends Specification with Tags {

  "A Grant" should {

    val grantMap = Map('agreement -> "http://duke.edu/grant/xyz123",
                       'type -> "<http://duke.edu/type/abc123>",
                       'grantName -> "Grant for This")
    
    "know its vivo type" in {
      val grant = Grant.build(grantMap)
      grant.vivoType mustEqual "http://duke.edu/type/abc123"
    }

    "must have required types of agreement, vivoType, grantName" in {
      List('agreement, 'type, 'grantName).foreach { item =>
        Grant.build(grantMap - item) must throwA[NoSuchElementException] 
      }
    }

  }

}