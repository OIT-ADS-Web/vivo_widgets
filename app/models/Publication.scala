package models.vivo

object Publication {

  def test_publications = {
    val  test_authors_1 = List("Smith, JM", "Cook, DE", "Rogers, MR")

    List(new Publication("Book","Scala for Dummies",List("Smith, JM", "Cook, DE", "Rogers, MR")),
         new Publication("Article","Scala on the Web",List("Smith, JM")),
         new Publication("Article","Scala and Concurrency",List("Lawrence GL","Smith J")))
  }

}

class Publication(val publicationType: String,
                  val title: String,
                  val  authors: List[String]) {
}
