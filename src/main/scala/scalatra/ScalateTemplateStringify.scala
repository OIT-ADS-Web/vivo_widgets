package edu.duke.oit.vw.scalatra

import edu.duke.oit.vw.utils.Timer
import java.io.{StringWriter,PrintWriter}
import javax.servlet.ServletContext
import org.fusesource.scalate.servlet.ServletResourceLoader
import org.fusesource.scalate._

trait ScalateTemplateStringify extends Timer {
  protected var stringifyTemplateEngine: Option[TemplateEngine] = None

  def renderTemplateString(servletContext: ServletContext, templatePath: String, context: Map[String, Any]) = {
    val buffer = new StringWriter()
    
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

    val templateSource = engine.source(templatePath)
    // val template = timer("template 1") { engine.load(templateSource, Nil) }.asInstanceOf[Template]
    val template = engine.load(templateSource)

    val renderContext = new DefaultRenderContext(templatePath, engine, new PrintWriter(buffer))

    context.foreach({case (key, value) => renderContext.attributes(key) = value})
    template.render(renderContext)

    buffer.toString
  }

}
