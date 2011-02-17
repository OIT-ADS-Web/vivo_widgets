package models

object Publication {

  val test_publications = Map("http://localhost:9000/smithjm" -> List(new Book(vivoUri="http://localhost:9000/pub001",
                                                                               vivoType="http://purl.org/ontology/bibo/Book",
                                                                               title="Scala for Dummies",
                                                                               authors=List("Smith, JM", "Cook, DE", "Rogers, MR"),
                                                                               year=2007,
                                                                               publishedBy="Webposse Press",
                                                                               edition="1st",
                                                                               numPages=359),
                                                                      new Article(vivoUri="http://localhost:9000/pub002",
                                                                                  vivoType="http://purl.org/ontology/bibo/Article",
                                                                                  title="Scala on the Web",
                                                                                  authors=List("Smith, JM"),
                                                                                  year=2009,
                                                                                  publishedIn="Web Weekly",
                                                                                  volume="12",
                                                                                  issue="3",
                                                                                  startPage=34,
                                                                                  endPage=37),
                                                                      new Article(vivoUri="http://localhost:9000/pub003",
                                                                                  vivoType="http://purl.org/ontology/bibo/Article",
                                                                                  title="Scala and Concurrency",
                                                                                  authors=List("Lawrence GL","Smith J"),
                                                                                  year=2010,
                                                                                  publishedIn="Scaling Times",
                                                                                  volume="5",
                                                                                  issue="15",
                                                                                  startPage=34,
                                                                                  endPage=37),
                                                                      new Book(vivoUri="http://localhost:9000/pub001",
                                                                               vivoType="http://purl.org/ontology/bibo/Book",
                                                                               title="Scala for Dummies",
                                                                               authors=List("Smith, JM", "Cook, DE", "Rogers, MR"),
                                                                               year=2007,
                                                                               publishedBy="Webposse Press",
                                                                               edition="1st",
                                                                               numPages=359),
                                                                      new Article(vivoUri="http://localhost:9000/pub002",
                                                                                  vivoType="http://purl.org/ontology/bibo/Article",
                                                                                  title="Scala on the Web",
                                                                                  authors=List("Smith, JM"),
                                                                                  year=2009,
                                                                                  publishedIn="Web Weekly",
                                                                                  volume="12",
                                                                                  issue="3",
                                                                                  startPage=34,
                                                                                  endPage=37),
                                                                      new Article(vivoUri="http://localhost:9000/pub003",
                                                                                  vivoType="http://purl.org/ontology/bibo/Article",
                                                                                  title="Scala and Concurrency",
                                                                                  authors=List("Lawrence GL","Smith J"),
                                                                                  year=2010,
                                                                                  publishedIn="Scaling Times",
                                                                                  volume="5",
                                                                                  issue="15",
                                                                                  startPage=34,
                                                                                  endPage=37),
                                                                      new Book(vivoUri="http://localhost:9000/pub001",
                                                                               vivoType="http://purl.org/ontology/bibo/Book",
                                                                               title="Scala for Dummies",
                                                                               authors=List("Smith, JM", "Cook, DE", "Rogers, MR"),
                                                                               year=2007,
                                                                               publishedBy="Webposse Press",
                                                                               edition="1st",
                                                                               numPages=359),
                                                                      new Article(vivoUri="http://localhost:9000/pub002",
                                                                                  vivoType="http://purl.org/ontology/bibo/Article",
                                                                                  title="Scala on the Web",
                                                                                  authors=List("Smith, JM"),
                                                                                  year=2009,
                                                                                  publishedIn="Web Weekly",
                                                                                  volume="12",
                                                                                  issue="3",
                                                                                  startPage=34,
                                                                                  endPage=37),
                                                                      new Article(vivoUri="http://localhost:9000/pub003",
                                                                                  vivoType="http://purl.org/ontology/bibo/Article",
                                                                                  title="Scala and Concurrency",
                                                                                  authors=List("Lawrence GL","Smith J"),
                                                                                  year=2010,
                                                                                  publishedIn="Scaling Times",
                                                                                  volume="5",
                                                                                  issue="15",
                                                                                  startPage=34,
                                                                                  endPage=37),
                                                                      new Book(vivoUri="http://localhost:9000/pub001",
                                                                               vivoType="http://purl.org/ontology/bibo/Book",
                                                                               title="Scala for Dummies",
                                                                               authors=List("Smith, JM", "Cook, DE", "Rogers, MR"),
                                                                               year=2007,
                                                                               publishedBy="Webposse Press",
                                                                               edition="1st",
                                                                               numPages=359),
                                                                      new Article(vivoUri="http://localhost:9000/pub002",
                                                                                  vivoType="http://purl.org/ontology/bibo/Article",
                                                                                  title="Scala on the Web",
                                                                                  authors=List("Smith, JM"),
                                                                                  year=2009,
                                                                                  publishedIn="Web Weekly",
                                                                                  volume="12",
                                                                                  issue="3",
                                                                                  startPage=34,
                                                                                  endPage=37),
                                                                      new Article(vivoUri="http://localhost:9000/pub003",
                                                                                  vivoType="http://purl.org/ontology/bibo/Article",
                                                                                  title="Scala and Concurrency",
                                                                                  authors=List("Lawrence GL","Smith J"),
                                                                                  year=2010,
                                                                                  publishedIn="Scaling Times",
                                                                                  volume="5",
                                                                                  issue="15",
                                                                                  startPage=34,
                                                                                  endPage=37),
                                                                      new Book(vivoUri="http://localhost:9000/pub001",
                                                                               vivoType="http://purl.org/ontology/bibo/Book",
                                                                               title="Scala for Dummies",
                                                                               authors=List("Smith, JM", "Cook, DE", "Rogers, MR"),
                                                                               year=2007,
                                                                               publishedBy="Webposse Press",
                                                                               edition="1st",
                                                                               numPages=359),
                                                                      new Article(vivoUri="http://localhost:9000/pub002",
                                                                                  vivoType="http://purl.org/ontology/bibo/Article",
                                                                                  title="Scala on the Web",
                                                                                  authors=List("Smith, JM"),
                                                                                  year=2009,
                                                                                  publishedIn="Web Weekly",
                                                                                  volume="12",
                                                                                  issue="3",
                                                                                  startPage=34,
                                                                                  endPage=37),
                                                                      new Article(vivoUri="http://localhost:9000/pub003",
                                                                                  vivoType="http://purl.org/ontology/bibo/Article",
                                                                                  title="Scala and Concurrency",
                                                                                  authors=List("Lawrence GL","Smith J"),
                                                                                  year=2010,
                                                                                  publishedIn="Scaling Times",
                                                                                  volume="5",
                                                                                  issue="15",
                                                                                  startPage=34,
                                                                                  endPage=37),
                                                                      new Book(vivoUri="http://localhost:9000/pub001",
                                                                               vivoType="http://purl.org/ontology/bibo/Book",
                                                                               title="Scala for Dummies",
                                                                               authors=List("Smith, JM", "Cook, DE", "Rogers, MR"),
                                                                               year=2007,
                                                                               publishedBy="Webposse Press",
                                                                               edition="1st",
                                                                               numPages=359),
                                                                      new Article(vivoUri="http://localhost:9000/pub002",
                                                                                  vivoType="http://purl.org/ontology/bibo/Article",
                                                                                  title="Scala on the Web",
                                                                                  authors=List("Smith, JM"),
                                                                                  year=2009,
                                                                                  publishedIn="Web Weekly",
                                                                                  volume="12",
                                                                                  issue="3",
                                                                                  startPage=34,
                                                                                  endPage=37),
                                                                      new Article(vivoUri="http://localhost:9000/pub003",
                                                                                  vivoType="http://purl.org/ontology/bibo/Article",
                                                                                  title="Scala and Concurrency",
                                                                                  authors=List("Lawrence GL","Smith J"),
                                                                                  year=2010,
                                                                                  publishedIn="Scaling Times",
                                                                                  volume="5",
                                                                                  issue="15",
                                                                                  startPage=34,
                                                                                  endPage=37),
                                                                      new Book(vivoUri="http://localhost:9000/pub001",
                                                                               vivoType="http://purl.org/ontology/bibo/Book",
                                                                               title="Scala for Dummies",
                                                                               authors=List("Smith, JM", "Cook, DE", "Rogers, MR"),
                                                                               year=2007,
                                                                               publishedBy="Webposse Press",
                                                                               edition="1st",
                                                                               numPages=359),
                                                                      new Article(vivoUri="http://localhost:9000/pub002",
                                                                                  vivoType="http://purl.org/ontology/bibo/Article",
                                                                                  title="Scala on the Web",
                                                                                  authors=List("Smith, JM"),
                                                                                  year=2009,
                                                                                  publishedIn="Web Weekly",
                                                                                  volume="12",
                                                                                  issue="3",
                                                                                  startPage=34,
                                                                                  endPage=37),
                                                                      new Article(vivoUri="http://localhost:9000/pub003",
                                                                                  vivoType="http://purl.org/ontology/bibo/Article",
                                                                                  title="Scala and Concurrency",
                                                                                  authors=List("Lawrence GL","Smith J"),
                                                                                  year=2010,
                                                                                  publishedIn="Scaling Times",
                                                                                  volume="5",
                                                                                  issue="15",
                                                                                  startPage=34,
                                                                                  endPage=37),
                                                                      new Book(vivoUri="http://localhost:9000/pub001",
                                                                               vivoType="http://purl.org/ontology/bibo/Book",
                                                                               title="Scala for Dummies",
                                                                               authors=List("Smith, JM", "Cook, DE", "Rogers, MR"),
                                                                               year=2007,
                                                                               publishedBy="Webposse Press",
                                                                               edition="1st",
                                                                               numPages=359),
                                                                      new Article(vivoUri="http://localhost:9000/pub002",
                                                                                  vivoType="http://purl.org/ontology/bibo/Article",
                                                                                  title="Scala on the Web",
                                                                                  authors=List("Smith, JM"),
                                                                                  year=2009,
                                                                                  publishedIn="Web Weekly",
                                                                                  volume="12",
                                                                                  issue="3",
                                                                                  startPage=34,
                                                                                  endPage=37),
                                                                      new Article(vivoUri="http://localhost:9000/pub003",
                                                                                  vivoType="http://purl.org/ontology/bibo/Article",
                                                                                  title="Scala and Concurrency",
                                                                                  authors=List("Lawrence GL","Smith J"),
                                                                                  year=2010,
                                                                                  publishedIn="Scaling Times",
                                                                                  volume="5",
                                                                                  issue="15",
                                                                                  startPage=34,
                                                                                  endPage=37)))

  def findAllForPerson(person_uri: String, limit: Int = 0) = {
    val all_pubs = test_publications.get(person_uri)
    all_pubs match {
      case Some(publications) => Option(if(limit > 0) publications.slice(0,limit) else publications)
      case _ => all_pubs
    }
  }

}

abstract class Publication(vivoUri: String, vivoType: String)

class Article(val vivoUri: String,
              val vivoType: String,
              val title: String,
              val authors: List[String],
              val year: Int,
              val publishedIn: String,
              val volume: String,
              val issue: String,
              val startPage: Int,
              val endPage: Int) extends Publication(vivoUri,vivoType) {

  val citation = authors.mkString(", ")+" ("+year+"). "+title+". "+publishedIn+". "+volume+"("+issue+"). "+startPage+"-"+endPage+"."
  val abbreviated = title+" ("+year+")"
}

class Book(val vivoUri: String,
           val vivoType: String,
           val title: String,
           val authors: List[String],
           val year: Int,
           val publishedBy: String,
           val edition: String,
           val numPages: Int) extends Publication(vivoUri,vivoType) {

  val citation = authors.mkString(", ")+". "+title+". "+publishedBy+", "+year+"."
  val abbreviated = title+" ("+year+")"
}
