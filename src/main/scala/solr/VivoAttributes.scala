package edu.duke.oit.vw.solr

import scala.collection.JavaConversions._

import edu.duke.oit.vw.utils._


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
