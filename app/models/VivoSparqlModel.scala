package models

import org.scardf.{Node, TypedLiteral, PlainLiteral}
import org.scardf.NodeConverter._

object VivoPerson {
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

  def finderSparql(uri: String): String  = """
    PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
    PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
    PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
    PREFIX owl: <http://www.w3.org/2002/07/owl#>
    PREFIX swrl: <http://www.w3.org/2003/11/swrl#>
    PREFIX swrlb: <http://www.w3.org/2003/11/swrlb#>
    PREFIX vitro: <http://vitro.mannlib.cornell.edu/ns/vitro/0.7#>
    PREFIX bibo: <http://purl.org/ontology/bibo/>
    PREFIX dcelem: <http://purl.org/dc/elements/1.1/>
    PREFIX dcterms: <http://purl.org/dc/terms/>
    PREFIX event: <http://purl.org/NET/c4dm/event.owl#>
    PREFIX foaf: <http://xmlns.com/foaf/0.1/>
    PREFIX geo: <http://aims.fao.org/aos/geopolitical.owl#>
    PREFIX pvs: <http://vivoweb.org/ontology/provenance-support#>
    PREFIX ero: <http://purl.obolibrary.org/obo/>
    PREFIX scires: <http://vivoweb.org/ontology/scientific-research#>
    PREFIX skos: <http://www.w3.org/2004/02/skos/core#>
    PREFIX core: <http://vivoweb.org/ontology/core#>

    SELECT distinct ?name ?title
    WHERE{
      <"""+uri+"""> rdfs:label ?name
      OPTIONAL{ <"""+uri+"""> vitro:moniker ?title } .
    }
  """

  def find(uri: String): VivoPerson = {
    val result = Vivo.queryJenaCache(finderSparql(uri))(0)
    new VivoPerson(uri,getString(result('name)),getString(result('title)))
  }

}


class VivoPerson(val uri: String, val name: String, val title: String) {


}
