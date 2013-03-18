package edu.duke.oit.vw.scalatra

import edu.duke.oit.vw.utils.Timer
import java.io.{StringWriter,PrintWriter}
import javax.servlet.ServletContext
import org.fusesource.scalate.servlet.ServletResourceLoader
import org.fusesource.scalate._
import org.fusesource.scalate.util.{Resource,FileResource,FileResourceLoader}

trait ScalateTemplateStringify {
  protected var stringifyTemplateEngine: Option[TemplateEngine] = None

  def renderTemplateString(servletContext: ServletContext, templatePath: String, context: Map[String, Any]):String = {
    /**
     * Grab the template engine from a cached version of the engine.
     */
    val engine = stringifyTemplateEngine match {
      case None => {
        var engine = new TemplateEngine
        engine.resourceLoader = new ServletResourceLoader(servletContext)
        stringifyTemplateEngine = Some(engine)
        engine
      }
      case Some(engine) => engine
    }
    
    renderFromEngine(engine, templatePath, context)
  }

  def renderFromClassPath(templatePath: String, context: Map[String, Any]=Map[String,Any]()):String = {

    val engine = stringifyTemplateEngine match {
      case None => {
        var engine = new TemplateEngine
        engine.resourceLoader = new FileResourceLoader
        stringifyTemplateEngine = Some(engine)
        engine
      }
      case Some(engine) => engine
    }

    renderFromEngine(engine, templatePath, context)
  }
  
  def renderFromEngine(engine:TemplateEngine, templatePath: String, context: Map[String, Any]):String = {
    val buffer = new StringWriter()
    
    engine.allowReload = false
    engine.allowCaching = true

    val templateSource = engine.source(templatePath)
    val template = engine.load(templateSource)

    val renderContext = new DefaultRenderContext(templatePath, engine, new PrintWriter(buffer))

    context.foreach({case (key, value) => renderContext.attributes(key) = value})
    template.render(renderContext)

    buffer.toString
  }


}
