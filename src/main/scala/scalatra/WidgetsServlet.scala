package edu.duke.oit.vw.scalatra

import org.eclipse.jetty.webapp.WebAppContext
import edu.duke.oit.vw.utils.{ElvisOperator,Json,Int,Timer}
import edu.duke.oit.vw.solr._
import edu.duke.oit.vw.models._
import java.net.URL
import java.text.SimpleDateFormat
import org.scalatra._
import scalate.ScalateSupport

// use scala collections with java iterators
import scala.collection.JavaConversions._

import java.util.Date
import java.lang.NumberFormatException

import org.slf4j.{Logger, LoggerFactory}


trait FormatType
object FormatHTML extends FormatType
object FormatJS extends FormatType
object FormatJSON extends FormatType
object FormatJSONP extends FormatType

class WidgetsFilter(val coreName: String, val coreDirectory: String) extends ScalatraFilter
  with ScalateSupport
  with ScalateTemplateStringify
  with Timer {

  val log =  LoggerFactory.getLogger(getClass)

  def this() {
    this("vivowidgetcore", WidgetsConfig.loadProperties()("WidgetsSolr.directory"))
  }

  // GET /people/{collectionName}/5.jsonp?uri={uri}
  get("/api/v0.9/people/:collectionName/:count.:format") {
    renderPeople
  }

  // GET /organizations/{collectionName}/5.jsonp?uri={uri}
  get("/api/v0.9/organizations/:collectionName/:count.:format") {
    renderOrganizations
  }

  protected def parseDate(dateParam: String):Date = {
    val dateFormat = new SimpleDateFormat("yyyy-MM-dd")
    // FIXME: there's bound to be a more Scala way to do this
    val startDate =
      if(!dateParam.isEmpty()) {
        dateFormat.parse(dateParam)
      }
      else {
        dateFormat.parse("1000-01-01")
      }
    return startDate
  }

  def safeOffset(str: String): Option[Integer] = try {
      Some(str.toInt)
    } catch {
    case e: NumberFormatException => None
  }

  // GET /search/modified.json?since={YYYY-MM-DD}
  get("/search/modified.:format") {
    WidgetsConfig.prepareCore(coreName, coreDirectory)
    requestSetup

    var dateSinceParam = params.getOrElse("since","")

    var since = try {
      parseDate(dateSinceParam)
    } catch {
      case e: java.text.ParseException => halt(status=400, body = String.format("Could not parse date %s", dateSinceParam))
    }

    log.debug("search from:" + since)

    val offset: Option[Integer] = safeOffset(params.getOrElse("offset", "0"))

    val results = WidgetsSearcher.searchByUpdatedAt(since, offset.getOrElse(0), WidgetsConfig.widgetServer)

    renderSearchResults(results)

  }


  // GET /search.json?query=theory*
  get("/search.:format") {
    requestSetup
    val result = VivoSearcher.search(params.getOrElse("query",""), WidgetsConfig.vivoServer)
    format(FormatJSONP) match {
      case FormatJSON => result.toJson
      case FormatHTML => "Not available: html"
      case FormatJSONP => jsonpCallback() + "("+result.toJson+")"
      case _ => "NoContent"
    }
  }

  get("/builder") {
    WidgetsConfig.prepareCore(coreName, coreDirectory)
    SolrEntity.getByUri(params("uri")) match {
      case Some(p:Person) => {
        val d = uriParams ++ Map("person" -> p)
        contentType = "text/html"
        layoutTemplate(TemplateHelpers.tpath("builder/person.jade"), d.toSeq: _*)
      }
      case Some(o:Organization) => {
        val d = uriParams ++ Map("organization" -> o)
        contentType = "text/html"
        layoutTemplate(TemplateHelpers.tpath("builder/organization.jade"), d.toSeq: _*)
      }
      case _ => "NoContent"
    }
  }

  // GET /api/v0.9/people/endpoints.json?uri=<uri>
  get("/api/v0.9/people/endpoints.:format") {
    WidgetsConfig.prepareCore(coreName, coreDirectory)
    requestSetup
    val context = request.getContextPath()
    val fmt = params("format")
    val uri = params("uri")
    Person.find(uri, WidgetsConfig.widgetServer) match {
      case Some(person) => {
        val result = Map(
          "data" -> Map(
            "uri"       -> uri,
            "label"     -> person.label,
            "active"    -> person.active,
            "complete"  -> personEndpointUrl(context,"complete",fmt,uri),
            "updatedAt" -> person.updatedAt,
            "vivoType"  -> person.vivoType,
            "sections"  -> Map(
              "academicPositions" -> personEndpointUrl(context,"academic_positions",fmt,uri),
              "addresses"         -> personEndpointUrl(context,"addresses",fmt,uri),
              "artisticEvents"    -> personEndpointUrl(context,"artistic_events",fmt,uri),
              "artisticWorks"     -> personEndpointUrl(context,"artistic_works",fmt,uri),
              "awards"            -> personEndpointUrl(context,"awards",fmt,uri),
              "courses"           -> personEndpointUrl(context,"courses",fmt,uri),
              "educations"        -> personEndpointUrl(context,"educations",fmt,uri),
              "geographicalFocus" -> personEndpointUrl(context,"geographical_focus",fmt,uri),
              "gifts"             -> personEndpointUrl(context,"gifts",fmt,uri),
              "grants"            -> personEndpointUrl(context,"grants",fmt,uri),
              "licenses"          -> personEndpointUrl(context,"licenses",fmt,uri),
              "newsfeeds"         -> personEndpointUrl(context,"newsfeeds",fmt,uri),
              "overview"          -> personEndpointUrl(context,"overview",fmt,uri),
              "pastAppointments"  -> personEndpointUrl(context,"past_appointments",fmt,uri),
              "positions"         -> personEndpointUrl(context,"positions",fmt,uri),
              "professionalActvities" -> personEndpointUrl(context,"professional_activities",fmt,uri),
              "publications"      -> personEndpointUrl(context,"publications",fmt,uri),
              "researchAreas"     -> personEndpointUrl(context,"research_areas",fmt,uri),
              "webpages"          -> personEndpointUrl(context,"webpages",fmt,uri)
            )
          )
        )
        format(FormatJSONP) match {
          case FormatJSON => Json.toJson(result)
          case FormatHTML => "Not available: html"
          case FormatJSONP => jsonpCallback() + "("+Json.toJson(result)+")"
          case _ => "NoContent"
        }
      }
      case _ => NotFound("Not Found")
    }
  }

  protected def personEndpointUrl(context: String, collection: String, format: String, uri: String): String = {
    val contextPath = context match {
      case "/" => ""
      case p: String => p
    }
    val url = WidgetsConfig.properties("Widgets.baseProtocolAndDomain") + context + "/api/v0.9/people/" + collection + "/all." + format + "?uri=" + uri
    return url
  }

  protected def renderPeople = {
    WidgetsConfig.prepareCore(coreName, coreDirectory)
    requestSetup
    Person.find(params("uri"), WidgetsConfig.widgetServer) match {
      case Some(person) => {
        params.getOrElse('collectionName, "") match {
          case "academic_positions" => renderPersonCollection(person,person.academicPositions)
          case "addresses"       => renderPersonCollection(person,person.addresses)
          case "artistic_events" => renderPersonCollection(person,person.artisticEvents)
          case "artistic_works"  => renderPersonCollection(person,person.artisticWorks)
          case "awards"          => renderPersonCollection(person,person.awards)
          case "complete"        => render(person)
          case "contact"         => renderPersonCollection(person,List(person.personAttributes()))
          case "courses"         => renderPersonCollection(person,person.courses)
          case "educations"      => renderPersonCollection(person,person.educations)
          case "geographical_focus" => renderPersonCollection(person,person.geographicalFocus)
          case "gifts"           => renderPersonCollection(person,person.gifts)
          case "grants"          => renderPersonCollection(person,person.grants)
          case "licenses"        => renderPersonCollection(person,person.licenses)
          case "newsfeeds"       => renderPersonCollection(person,person.newsfeeds)
          case "overview"        => renderPersonCollection(person,List(person.personAttributes()))
          case "past_appointments"  => renderPersonCollection(person,person.pastAppointments)
          case "positions"       => renderPersonCollection(person,person.positions)
          case "professional_activities" => renderPersonCollection(person,person.professionalActivities)
          case "publications"    => renderPersonCollection(person,person.publications)
          case "research_areas"  => renderPersonCollection(person,person.researchAreas)
          case "webpages"        => renderPersonCollection(person,person.webpages)
          case x                 => "Collection not found: " + x
        }
      }
      case _ => NotFound("Not Found")
    }
  }

  protected def renderOrganizations = {
    WidgetsConfig.prepareCore(coreName, coreDirectory)
    requestSetup
    Organization.find(params("uri"), WidgetsConfig.widgetServer) match {
      case Some(organization) => {
        params.getOrElse('collectionName, "") match {
          case "people" => renderOrganizationCollection(organization,organization.people)
          case "grants" => renderOrganizationCollection(organization,organization.grants)
          case x => "Collection not found: " + x
        }
      }
      case _ => NotFound("Not Found")
    }
  }

  protected def formatPersonCollection(person: Person, formatType: FormatType, collectionName: String, collection: List[AnyRef], items: Option[Int], formatting: String, style: String, start: String, end: String):String  = {
    var modelData = scala.collection.mutable.Map[String,Any]()
    modelData.put("person", person)

    items match {
      case Some(x:Int) => modelData.put(collectionName, collection.slice(0, x))
      case _ => modelData.put(collectionName, collection)
    }

    modelData.put("style", style)
    modelData.put("formatting", formatting)
    modelData.put("layout", "")

    val template = TemplateHelpers.tpath(collectionName + ".jade")

    timer("WidgetsServlet.formatCollection") {
      renderTemplateString(servletContext, template, modelData.toMap)
    }.asInstanceOf[String]
  }

  protected def filterCollectionByCount(collection: List[AnyRef], items: Option[Int]) = {
    var values = items match {
      case Some(x:Int) =>  collection.slice(0, x)
      case _ => collection
    }
    Json.toJson(values)
  }

  protected def formatOrganizationCollection(organization: Organization, formatType: FormatType, collectionName: String, collection: List[AnyRef], items: Option[Int], formatting: String, style: String, start: String, end: String):String  = {
    var modelData = scala.collection.mutable.Map[String,Any]()
    modelData.put("organization", Organization)

    items match {
      case Some(x:Int) => modelData.put(collectionName, collection.slice(0, x))
      case _ => modelData.put(collectionName, collection)
    }

    modelData.put("style", style)
    modelData.put("formatting", formatting)
    modelData.put("layout", "")

    val template = TemplateHelpers.tpath(collectionName + ".jade")

    timer("WidgetsServlet.formatCollection") {
      renderTemplateString(servletContext, template, modelData.toMap)
    }.asInstanceOf[String]
  }

  protected def renderSearchResults(results: WidgetsSearchResult) = {
    // NOTE: I believe we only want JSON results for this (right now)
    request("format") match {
      case FormatJSON => Json.toJson(results)
      case _ => "not content"
    }

  }

  protected def renderPersonCollection(person: Person, collection: List[AnyRef]) = {
    val start = params.getOrElse("start", "")
    val end = params.getOrElse("end", "")
    val dateFormat = new SimpleDateFormat("yyyy-MM-dd")
    val startDate =
      if(!start.isEmpty()) {
        dateFormat.parse(start) }
      else {
        dateFormat.parse("1000-01-01")
      }
    val endDate =
      if(!end.isEmpty()) {
        dateFormat.parse(end)}
      else {
        dateFormat.parse("9999-12-31")
      }
    val dateFilteredCollection = collection.filter(x => {
      if (x.isInstanceOf[VivoAttributes]) {
        val attributeItem = x.asInstanceOf[VivoAttributes]
        attributeItem.withinTimePeriod(startDate, endDate)
      } else {
        true
      }
    })

    request("format") match {
      case FormatJSON => filterCollectionByCount(dateFilteredCollection, Int(params.getOrElse("count", "all")))
      case FormatJSONP => jsonpCallback + "(" + filterCollectionByCount(dateFilteredCollection,
                                                Int(params.getOrElse("count", "all"))) + ");"
      case FormatHTML => {
        timer("WidgetsServlet.renderCollection") {
          formatPersonCollection(person, FormatHTML, params("collectionName"),
                           dateFilteredCollection,
                           Int(params.getOrElse("count", "all")),
                           params.getOrElse("formatting", "detailed"),
                           params.getOrElse("style", "yes"),
                           params.getOrElse("start", ""),
                           params.getOrElse("end", ""))
        }
      }
      case FormatJS => {
        val output = formatPersonCollection(person, FormatJS, params("collectionName"),
                                      dateFilteredCollection,
                                      Int(params.getOrElse("count", "all")),
                                      params.getOrElse("formatting", "detailed"),
                                      params.getOrElse("style", "yes"),
                                      params.getOrElse("start", ""),
                                      params.getOrElse("end", ""))
        val lines = output.split('\n').toList
        val documentWrites = lines.map { "document.write('"+_.replaceAll("'","\\\\'")+"');" }
        documentWrites.mkString("\n")
      }
      case _ => "no content"
    }
  }

  protected def renderOrganizationCollection(organization: Organization, collection: List[AnyRef]) = {
    val start = params.getOrElse("start", "")
    val end = params.getOrElse("end", "")
    val dateFormat = new SimpleDateFormat("yyyy-MM-dd")
    val startDate =
      if(!start.isEmpty()) {
        dateFormat.parse(start) }
      else {
        dateFormat.parse("1000-01-01")
      }
    val endDate =
      if(!end.isEmpty()) {
        dateFormat.parse(end)}
      else {
        dateFormat.parse("9999-12-31")
      }
    val dateFilteredCollection = collection.filter(x => {
      if (x.isInstanceOf[VivoAttributes]) {
        val attributeItem = x.asInstanceOf[VivoAttributes]
        attributeItem.withinTimePeriod(startDate, endDate)
      } else {
        true
      }
    })

    request("format") match {
      case FormatJSON => filterCollectionByCount(dateFilteredCollection, Int(params.getOrElse("count", "all")))
      case FormatJSONP => jsonpCallback + "(" + filterCollectionByCount(dateFilteredCollection,
                                                Int(params.getOrElse("count", "all"))) + ");"
      case FormatHTML => {
        timer("WidgetsServlet.renderCollection") {
          formatOrganizationCollection(organization, FormatHTML, params("collectionName"),
                           dateFilteredCollection,
                           Int(params.getOrElse("count", "all")),
                           params.getOrElse("formatting", "detailed"),
                           params.getOrElse("style", "yes"),
                           params.getOrElse("start", ""),
                           params.getOrElse("end", ""))
        }
      }
      case FormatJS => {
        val output = formatOrganizationCollection(organization, FormatJS, params("collectionName"),
                                      dateFilteredCollection,
                                      Int(params.getOrElse("count", "all")),
                                      params.getOrElse("formatting", "detailed"),
                                      params.getOrElse("style", "yes"),
                                      params.getOrElse("start", ""),
                                      params.getOrElse("end", ""))
        val lines = output.split('\n').toList
        val documentWrites = lines.map { "document.write('"+_.replaceAll("'","\\\\'")+"');" }
        documentWrites.mkString("\n")
      }
      case _ => "not content"
    }
  }

  protected def render(person: Person) = {
    request("format") match {
      case FormatJSON => Json.toJson(person)
      case FormatJSONP => jsonpCallback + "(" + Json.toJson(person) + ");"
      case _ => redirect(person.uri)
    }
  }


  protected def requestSetup = {
    request.put("format", format())
    setContentType(request("format").asInstanceOf[FormatType])
  }

  protected def setContentType(formatType:FormatType) = {
    formatType match {
      case FormatJSON => contentType = "application/json"
      case FormatJSONP => contentType = "application/javascript"
      case FormatJS => contentType = "application/javascript"
      case FormatHTML => contentType = "text/html"
      case _ => contentType = "text/unknown"
    }
  }

  protected def format(defaultType:FormatType=FormatJSON):FormatType = {
    params.getOrElse("format", "") match {
      case "json" => FormatJSON
      case "jsonp" => FormatJSONP
      case "html" => FormatHTML
      case "js" => FormatJS
      case _ => defaultType
    }
  }

  protected def uriParams = {
    import edu.duke.oit.vw.utils.ElvisOperator._
    Map(
      "uriPrefix" -> uriPrefix(),
      "contextUri" -> (request.getContextPath() ?: ""),
      "theme" -> WidgetsConfig.theme,
      "version" -> defaultVersion
    )
  }

  protected def defaultVersion = "v0.9"

  protected def uri(s:String) = {
    uriPrefix + s
  }

  protected def uriPrefix() = {
    import edu.duke.oit.vw.utils.ElvisOperator._
    (request.getContextPath() ?: "") + (request.getServletPath() ?: "")
  }

  protected def jsonpCallback() = params.getOrElse("callback", "vivoWidgetResult")

  notFound {
    filterChain.doFilter(request, response)
  }

}
