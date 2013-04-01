package edu.duke.oit.solr.test

import org.specs2.mutable._
import edu.duke.oit.vw.solr.Course

class CourseSpec extends Specification with Tags {

  "A Course" should {

    val courseMap = Map('course -> "http://duke.edu/course/xyzc123",
                        'type -> "<http://duke.edu/type/abcc123>",
                        'courseName -> "123 Park")
    
    "know its vivo type" in {
      val course = Course.build(courseMap)
      course.vivoType mustEqual "http://duke.edu/type/abcc123"
    }

    "must have required types of course, vivoType, courseName" in {
      List('course, 'type, 'courseName).foreach { item =>
        Course.build(courseMap - item) must throwA[NoSuchElementException] 
      }
    }

  }

}
