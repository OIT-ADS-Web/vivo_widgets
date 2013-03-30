package edu.duke.oit.vw.solr

import edu.duke.oit.vw.utils._

case class Publication(uri:String,
                       vivoType:String,
                       title:String,
                       authors:List[String],
                       extraItems:Option[Map[String, String]]) 
     extends ExtraItems(extraItems) with AddToJson
{

  override def uris() = {
    uri :: super.uris
  }

  def subType = {
    val regex = "(.*)/([a-zA-Z0-9.\\-]+)(#)?([a-zA-Z0-9.\\-]+)?".r
    vivoType match {
      case regex(_,subType1,_,subType2) => if(subType2 == null) subType1 else subType2
      case _ => this.getClass.getName
    }
  }
}

object Publication extends ExtraParams {

  def fromUri(vivo: Vivo, uriContext:Map[String, Any], useCache: Boolean = false) = {
    val publicationData  = vivo.selectFromTemplate("sparql/publications.ssp", uriContext, useCache)
    publicationData.map(build(vivo, _)).asInstanceOf[List[Publication]]
  }

  def build(vivo: Vivo, pub:Map[Symbol,String], useCache: Boolean = false) = {
    new Publication(uri      = pub('publication).replaceAll("<|>",""),
                    vivoType = pub('type).replaceAll("<|>",""),
                    title    = pub('title),
                    authors  = getAuthors(pub('publication).replaceAll("<|>",""), vivo, useCache),
                    extraItems = parseExtraItems(pub,List('publication,'type,'title)))
  }

  def getAuthors(pubURI: String, vivo: Vivo,useCache:Boolean = false): List[String] = {
    // val authorSparql = renderFromClassPath("sparql/authors.ssp", Map("uri" -> pubURI))a
    // val authorData = vivo.select(authorSparql,useCache)
    val authorData = vivo.selectFromTemplate("sparql/authors.ssp", Map("uri" -> pubURI), useCache)

    val authorsWithRank = authorData.map(a => (a('authorName),a.getOrElse('rank, 0))).distinct
    authorsWithRank.sortWith((a1,a2) => (Int(a1._2.toString).get < Int(a2._2.toString).get)).map(_._1)
  }

}
