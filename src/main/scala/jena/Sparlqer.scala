package edu.duke.oit.vw.jena

import com.hp.hpl.jena.rdf.model.{Model => JModel}
import com.hp.hpl.jena.query.{QueryFactory,Query,QueryExecutionFactory,QueryExecution,ResultSet,QuerySolution}
import scala.collection.JavaConversions._

object Sparqler {

  def selectingFromModel[T](model: JModel, sparql: String)(withSelected: (ResultSet) => T) =  {
    val query = QueryFactory.create(sparql);
    val qe = QueryExecutionFactory.create(query, model);
    try {
      val results = qe.execSelect();
      withSelected(results)
    } finally {
      qe.close();
    }
  }

  def simpleResults(resultSet: ResultSet): List[Map[Symbol,String]] = {
    resultSet.toList map {l => l.varNames.toList.foldLeft(Map[Symbol,String]()) { (m,v) => m.updated(Symbol(v),simpleGet(l,v)) } }
  }

  def simpleGet(qs: QuerySolution, varName: String): String = {
     val resultNode = qs.get(varName)
     if (resultNode.isLiteral) {
       resultNode.asLiteral.getString
     } else {
       resultNode.toString
     }
  }

}
