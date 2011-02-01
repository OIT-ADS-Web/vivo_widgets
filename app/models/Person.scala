package models

object Person {

  val test_people = Map("http://localhost:9000/smithjm" -> new Person(uri = "http://localhost:9000/smithjm", name = "Smith, Joseph M"))

  def find_by_uri(uri: String) = {
    test_people.get(uri)
  }

}

class Person(val uri: String, val name: String) {

  def publications = Publication.findAllForPerson(uri)

}
