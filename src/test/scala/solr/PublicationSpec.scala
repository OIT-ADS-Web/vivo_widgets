package edu.duke.oit.solr.test

import org.specs2.mutable._
import org.specs2.mock._
import edu.duke.oit.vw.solr.Vivo
import edu.duke.oit.vw.solr.Publication

class PublicationSpec extends Specification with Tags with Mockito {

  "A Publication" should {

    val publicationMap = Map('publication -> "https://duke.edu/publication/abc123",
                             'type -> "https://duke.edu/type/aoeush12",
                             'label -> "Study of Something")

    val vivo = mock[Vivo]

    "contain the label in the publication" in {
      val pub = Publication.build(vivo, publicationMap, false)
      pub.label mustEqual "Study of Something"
    }

    "must have required types of publication, vivoType, label" in {
      List('publication, 'type, 'label).foreach { item =>
        Publication.build(vivo, publicationMap - item) must throwA[NoSuchElementException] 
      }
    }

  }

}
