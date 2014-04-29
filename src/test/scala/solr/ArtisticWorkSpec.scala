package edu.duke.oit.solr.test

import org.specs2.mutable._
import edu.duke.oit.vw.models.ArtisticWork

class ArtisticWorkSpec extends Specification with Tags {

  "An ArtisticWork" should {

    val artisticWorkMap = Map('work -> "http://duke.edu/artistic_work/xyz123",
                              'type -> "<http://duke.edu/type/abc123>",
                              'label -> "Some Art Work")
    
    "know its vivo type" in {
      val artisticWork = ArtisticWork.build(artisticWorkMap)
      artisticWork.vivoType mustEqual "http://duke.edu/type/abc123"
    }

    "must have required types of work, vivoType, label" in {
      List('work, 'type, 'label).foreach { item =>
        ArtisticWork.build(artisticWorkMap - item) must throwA[NoSuchElementException] 
      }
    }
  }
}
