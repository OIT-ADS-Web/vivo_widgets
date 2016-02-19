package edu.duke.oit.vw.utils

import org.scardf._
import org.scardf.NodeConverter._

import org.slf4j.Logger
import org.slf4j.LoggerFactory

trait ToMethods {
  implicit def toMethods(obj: AnyRef) = new {
    def methods = obj.getClass.getMethods.map(_.getName)
  }
}

trait WidgetLogging {
  val log = LoggerFactory.getLogger(this.getClass)
}

trait Timer {

  def timer(label: String = "")(continue: => Any) = {
    val timerLog = LoggerFactory.getLogger("widget.timer")
    val c = this.getClass
    val a = System.currentTimeMillis
    var r = continue
    val b = System.currentTimeMillis
    timerLog.debug(label + " | Total Time(msec): " + (b.toInt - a.toInt))
    r
  }
}

trait SimpleConversion {

  def getString(node: Node): String = {
    node match {
      case n: PlainLiteral => n / asString
      case b: TypedLiteral => {
        b.isLiteral match {
          case true => b / asLexic
          case _ => b.toString
        }
      }
      case _ => node.toString
    }
  }
}

import java.util.Date

trait Timestamped {
    val uri:String
    val updatedAt:Option[Date]
}


object Int {
  def isInteger(s : String) : Boolean = {
    apply(s) match {
      case Some(i) => true
      case _ => false
     }
  }
  
  def apply(s: String) : Option[Int] = try {
    Some(s.toInt)
  } catch {
    case _ : java.lang.NumberFormatException => None
  }
}

/**
 * Json helper methods
 */
object Json {
  
  /**
   * Covert <code>item</item> to a json string representation format.
   *
   * @param item convert the item of type T to a json string.
   */
  def toJson[T](item:T) = {
    import net.liftweb.json.{JsonAST,Printer,Extraction,Merge}
    implicit val formats = net.liftweb.json.DefaultFormats
    Printer.compact(JsonAST.render(Extraction.decompose(item)))
  }

  def toJson[T](collectionName:String, item:T) = {
    val wrappedItem = Map(collectionName -> item)
    import net.liftweb.json.{JsonAST,Printer,Extraction,Merge}
    implicit val formats = net.liftweb.json.DefaultFormats
    Printer.compact(JsonAST.render(Extraction.decompose(wrappedItem)))
    
  }

}

trait AddToJson {

  /**
   * Convert the current object to a json String.
   * @return String representation of json.
   */
  def toJson = {
    Json.toJson(this)
  }

}

trait ElvisOperator {
  implicit def elvisOperator[T](alt: =>T) = new {
    def ?:[A >: T](pred: A) = if (pred == null) alt else pred
  }
}

object ElvisOperator extends ElvisOperator

trait AttributeParams {

  implicit def addStripBackets(s:String)=new StripBracketsToString(s)

  def parseAttributes(resultMap: Map[Symbol,String], requiredKeys: List[Symbol]): Option[Map[String,String]] = {
    val extraItems = resultMap -- requiredKeys
    Option(extraItems.map(kvp => (kvp._1.name -> kvp._2)))
  }

}


class StripBracketsToString(underlying:String){
  def stripBrackets() = {
    underlying.replaceAll("<|>","")
  }
}
