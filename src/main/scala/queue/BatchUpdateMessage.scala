package edu.duke.oit.vw.queue

//import edu.duke.oit.vw.utils._

case class BatchUpdateMessage(uris:List[String], from:Option[String])


/**
 * Wraps the lift-json parsing and extraction of a person.
 */
object BatchUpdateMessage {
  def apply(json:String) = {
    import net.liftweb.json._
    // Brings in default date formats etc.
    implicit val formats = DefaultFormats 

    val j = JsonParser.parse(json)
    j.extract[BatchUpdateMessage]
  }
}



