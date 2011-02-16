package models
import com.google.gson._

class VivoWidgetJsonResult(results: List[AnyRef]) {

  def json = {
    val gson = new Gson()
    val jsonResults = results.map { gson.toJsonTree(_) }
    val headerObj = new JsonObject()

    val jsonResultsArray = new JsonArray()
    jsonResults.foreach { jsonResultsArray.add(_) }

    headerObj.addProperty("items",results.size)
    headerObj.addProperty("date",(new java.util.Date).toString())

    val resultObj = new JsonObject()
    resultObj.add("resultHeader",headerObj)
    resultObj.add("results",jsonResultsArray)
    resultObj
  }

  def jsonp = { "vivoWidgetResult("+this.json.toString()+");" }
}
