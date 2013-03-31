package edu.duke.oit.solr.test

import org.specs2.mutable._
import org.specs2.mock._
import edu.duke.oit.vw.solr.Vivo
import edu.duke.oit.vw.solr.Publication

class PublicationSpec extends Specification with Tags with Mockito {

  "A Publication" should {

    val publicationMap = Map('publication -> "https://duke.edu/publication/abc123",
                           'type -> "https://duke.edu/type/aoeush12",
                           'title -> "Study of Something")

    val vivo = mock[Vivo]
    def authors(pubURI: String, vivo: Vivo,useCache:Boolean = false): List[String] = {
      List()
    }

    "contain the title in the publication" in {
      val pub = Publication.build(vivo, publicationMap, false, authors)
      pub.title mustEqual "Study of Something"
    }

    "must have required types of publication, vivoType, title" in {
      List('publication, 'type, 'title).foreach { item =>
        Publication.build(vivo, publicationMap - item) must throwA[NoSuchElementException] 
      }
    }

  }

}
