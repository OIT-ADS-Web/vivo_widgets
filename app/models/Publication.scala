package models

object Publication {

  val test_publications = Map("http://localhost:9000/smithjm" -> List(new Book(uri="http://localhost:9000/pub001",
                                                                               title="Scala for Dummies",
                                                                               authors=List("Smith, JM", "Cook, DE", "Rogers, MR"),
                                                                               year=2007,
                                                                               publishedBy="Webposse Press",
                                                                               edition="1st",
                                                                               numPages=359),
                                                                      new Article(uri="http://localhost:9000/pub002",
                                                                                  title="Scala on the Web",
                                                                                  authors=List("Smith, JM"),
                                                                                  year=2009,
                                                                                  publishedIn="Web Weekly",
                                                                                  volume="12",
                                                                                  issue="3",
                                                                                  startPage=34,
                                                                                  endPage=37),
                                                                      new Article("http://localhost:9000/pub003",
                                                                                  title="Scala and Concurrency",
                                                                                  authors=List("Lawrence GL","Smith J"),
                                                                                  year=2010,
                                                                                  publishedIn="Scaling Times",
                                                                                  volume="5",
                                                                                  issue="15",
                                                                                  startPage=34,
                                                                                  endPage=37)))

  def find_all_by_person_uri(person_uri: String) = {
    test_publications.get(person_uri)
  }

}

//class Publication(val uri: String,
                  //val title: String,
                  //val authors: List[String],
                  //val year: Int) {
//}

class Publication()

class Article(val uri: String,
              val title: String,
              val authors: List[String],
              val year: Int,
              val publishedIn: String,
              val volume: String,
              val issue: String,
              val startPage: Int,
              val endPage: Int) extends Publication() {

}

class Book(val uri: String,
           val title: String,
           val authors: List[String],
           val year: Int,
           val publishedBy: String,
           val edition: String,
           val numPages: Int) extends Publication() {

}
