package edu.duke.oit.vw

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.{DefaultServlet, ServletContextHandler}
import org.eclipse.jetty.webapp.WebAppContext
import org.eclipse.jetty.util.thread.QueuedThreadPool

import edu.duke.oit.vw.scalatra.WidgetsFilter 

object JettyLauncher {
  def main(args: Array[String]) {
    val port        = if(System.getenv("PORT") != null) System.getenv("PORT").toInt else 8080
    val maxThreads  = if(System.getenv("MAX_THREADS") != null) System.getenv("MAX_THREADS").toInt else 100
    val webDir      = classOf[WidgetsFilter].getClassLoader().getResource("webapp").toExternalForm()

    val server      = new Server(port)
    val context     = new WebAppContext()
    val threadPool  = new QueuedThreadPool()

    threadPool.setMaxThreads(maxThreads)

    context setContextPath "/widgets"
    context.setResourceBase(webDir)
    context.addServlet(classOf[DefaultServlet], "/")

    server.setHandler(context)
    server.setThreadPool(threadPool)

    server.start
    server.join
  }
}
