package edu.duke.oit.vw.sparql.test

import org.specs._
import edu.duke.oit.test.helpers.TestModels
import edu.duke.oit.vw.sparql.Sparqler
import com.hp.hpl.jena.query.{QueryFactory,Query,QueryExecutionFactory,QueryExecution,ResultSet}

class SparqlerSpec extends Specification {

  "A Sparqler" should {

    "just getting started" in {
      val sparql = "select * where { ?s ?p ?o } LIMIT 10"
      val simpleList = Sparqler.selectingFromModel(TestModels.sampleInstanceModel,sparql) { resultSet =>
        Sparqler.simpleResults(resultSet)
      }
      println(simpleList)
    }

  }

}
