package edu.duke.oit.vw.scalatra

// import akka.servlet.AkkaLoader
// import akka.remote.BootableRemoteActorService
// import akka.actor.BootableActorLoaderService
import javax.servlet.{ServletContextListener, ServletContextEvent}

class VivoWidgetsInitializer extends ServletContextListener {
   // lazy val loader = new AkkaLoader
   def contextDestroyed(e: ServletContextEvent): Unit = {
     // loader.shutdown
   }
   def contextInitialized(e: ServletContextEvent): Unit = {
     // loader.boot(true, new BootableActorLoaderService with BootableRemoteActorService) //<--- Important
     
     // loader.boot(true, new BootableActorLoaderService {}) // If you don't need akka-remote
   }
 }
