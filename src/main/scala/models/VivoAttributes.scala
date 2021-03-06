package edu.duke.oit.vw.models

import scala.collection.JavaConversions._

import edu.duke.oit.vw.utils._
import java.util.Date
import java.text.SimpleDateFormat

import java.util.GregorianCalendar
import java.util.Calendar

/**
 * The <code>VivoAttributes</code> value is a catch all hash that
 * can be used to add attributes from the originating SPARQL
 * that aren't explicitly defined.
 * <code>attributes</code> must a Map[String, String].
 */
class VivoAttributes(uri:String,
                     vivoType:String,
                     label:String,
                     attributes:Option[Map[String, String]]) extends ToMethods with AddToJson {
  
  import net.liftweb.json.JsonDSL._
  import net.liftweb.json.{JsonAST,Printer,Extraction}
  import net.liftweb.json.JsonAST.{JField,JObject}

  /**
   * 
   * @param key the key string to look for in the attributes Map
   * @return Some(String) with the string value from the attributes Map;
   *         otherwise None
   */
  def \(key:String): String = {
    attributes match {
      case Some(m) => m.get(key).orNull
      case _ => null
    }
  }

  def get(key:String): String = {
    \(key)
  }

  def getOrElse(key:String,default:String): String = {
    \(key) match {
      case null => default
      case m:String => m
    }
  }

  def officialDateKey: String = "date"

  def officialDate: Date = {
    val date = get(officialDateKey)
    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(date)
  }


  def officialStartDate: Date = {
    val startDate = get(officialDateKey)
    new SimpleDateFormat("yyyy-MM-dd").parse(startDate)
  }


  def withinTimePeriod(start: Date, end: Date): Boolean = {
    val calendar1 = new GregorianCalendar()
    calendar1.setTime(start);
    val calendar2 = new GregorianCalendar()
    calendar2.setTime(end)
    if (get(officialDateKey) == null) {
      // defaults are: start="1000-01-01", end = "9999-12-31"
      // so if it's default (same as blank), include publications with null date, otherwise not
      if ((calendar1.get(Calendar.YEAR) == 1000) && (calendar2.get(Calendar.YEAR) == 9999)) { true } else { false }
    } else {
      (officialDate.after(start) || officialDate.equals(start)) &&
      (officialDate.before(end) || officialDate.equals(end))
    }
  }

  def uris():List[String] = {
    attributes match {
      case Some(eitems) => {
        val l = for (i <- eitems if i._2.startsWith("http")) yield i._2
        l.toList
      }
      case _ => List()
    }
  }

}
