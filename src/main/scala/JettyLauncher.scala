package edu.duke.oit.vw

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.{DefaultServlet, ServletContextHandler}
import org.eclipse.jetty.webapp.WebAppContext

import edu.duke.oit.vw.scalatra.WidgetsFilter 

object JettyLauncher {
  def main(args: Array[String]) {
    val port = if(System.getenv("PORT") != null) System.getenv("PORT").toInt else 8080
    val webDir = classOf[WidgetsFilter].getClassLoader().getResource("webapp").toExternalForm();

    val server = new Server(port)
    val context = new WebAppContext()

    context setContextPath "/widgets"
    context.setResourceBase(webDir)
    context.addServlet(classOf[DefaultServlet], "/")

    server.setHandler(context)

    server.start
    server.join
  }
}
