package edu.duke.oit.vw.solr

import edu.duke.oit.vw.utils._
import edu.duke.oit.vw.models.SolrModel

object VivoSearcher extends SolrModel

class VivoSearchResult(val numFound: Long,val  groups: Map[String,Long],val  items: List[VivoSearchResultItem]) extends AddToJson

class VivoSearchResultItem(val uri: String,val  name: String,val  group: String)
