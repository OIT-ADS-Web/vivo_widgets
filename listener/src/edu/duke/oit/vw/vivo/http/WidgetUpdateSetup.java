package edu.duke.oit.vw.vivo.http;

import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.dao.jena.ModelContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 */
public class WidgetUpdateSetup implements ServletContextListener {

  private static final Log log = LogFactory.getLog(WidgetUpdateSetup.class.getName());

  /**
   * WidgetUpdateSetup.endpointUri= end point uri (like http://127.0.0.1:8080/updates/person/uri)
   * WidgetUpdateSetup.username   = (basic auth username
   * WidgetUpdateSetup.password   = (basic auth password)
   *
   * @param servletContextEvent
   */
  public void contextInitialized(ServletContextEvent servletContextEvent) {
    log.info("Starting WidgetUpdateSetup.");

    String endpointUri = ConfigurationProperties.getBean(servletContextEvent).getProperty("WidgetUpdateSetup.endpointUri");
    String username    = ConfigurationProperties.getBean(servletContextEvent).getProperty("WidgetUpdateSetup.username");
    String password    = ConfigurationProperties.getBean(servletContextEvent).getProperty("WidgetUpdateSetup.password");

    if ((endpointUri == null) || (username == null) || (password == null)) {
      log.info("Did not find an endpoint uri(" + endpointUri + "), username (" + username + ") or password (" + password + ").");
      return;
    }

    ServletContext ctx = servletContextEvent.getServletContext();
    try {

      WidgetUpdateListener widgetUpdateListener = new WidgetUpdateListener(endpointUri, username, password);
      ModelContext.registerListenerForChanges(ctx, widgetUpdateListener);
    } catch (Exception e) {
      log.error("Exception: " + e.toString());
      log.error(e);
      e.printStackTrace();
    }

    log.debug("done starting WidgetUpdateSetup.");

  }

  public void contextDestroyed(ServletContextEvent servletContextEvent) {
    log.debug("Stopping WidgetUpdateSetup.");
  }

}
