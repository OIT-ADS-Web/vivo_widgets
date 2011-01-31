package models

object Publication {

  val test_publications = Map("http://localhost:9000/smithjm" -> List(new Publication("http://localhost:9000/pub001","Book","Scala for Dummies",List("Smith, JM", "Cook, DE", "Rogers, MR")),
                                                                      new Publication("http://localhost:9000/pub002","Article","Scala on the Web",List("Smith, JM")),
                                                                      new Publication("http://localhost:9000/pub003","Article","Scala and Concurrency",List("Lawrence GL","Smith J"))))

  def find_all_by_person_uri(person_uri: String) = {
    test_publications.get(person_uri)
  }

}

class Publication(val uri: String,
                  val publicationType: String,
                  val title: String,
                  val authors: List[String]) {
}
