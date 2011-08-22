package edu.duke.oit.vw.vivo.http;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.Lock;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.URL;
import java.util.List;

/**
 * This class pushed event notifications to a .
 */
public class WidgetUpdateListener implements ModelChangedListener {

  private static final Log log = LogFactory.getLog(WidgetUpdateListener.class.getName());

  private String endpointUri;
  private String username;
  private String password;

  public WidgetUpdateListener(String endpointUri, String username, String password) {
    this.endpointUri = endpointUri;
    this.username = username;
    this.password = password;
  }

  private String sendMessage(String uri, String from) {
    return "{\"uri\":\"" + uri + "\",\"from\":\"" + from + "\"}";
  }

  private void send(String msg) {
    log.debug("Sending message: " + msg);
    UsernamePasswordCredentials creds = new UsernamePasswordCredentials(this.username, this.password);

    try {
      URL url = new URL(this.endpointUri);

      HttpClient client = new HttpClient();
      HttpState state = new HttpState();

      AuthScope authScope = new AuthScope(url.getHost(),url.getPort());

      // TODO replace AuthScope.ANY with authScope for give host and port
      state.setCredentials(AuthScope.ANY, creds);
      client.setState(state);

      PostMethod post = new PostMethod(this.endpointUri);
      NameValuePair[] data = {
          new NameValuePair("message", msg)
      };
      post.setRequestBody(data);
      client.executeMethod(post);
      String response = post.getResponseBodyAsString();
      log.debug("response: " + response);

    } catch (Exception e) {
      log.error("Error when sending JMS message to server.");
      log.error(e);
    }
  }

  private synchronized void addChange(Statement stmt, String from) {

    if (stmt == null) return;
    if (log.isDebugEnabled()) {
      String sub = "unknown";
      String pred = "unknown";
      String obj = "unknown";

      if (stmt.getSubject().isURIResource()) {
        sub = stmt.getSubject().getURI();
      }
      if (stmt.getPredicate() != null) {
        pred = stmt.getPredicate().getURI();
      }
      if (stmt.getObject().isURIResource()) {
        obj = ((Resource) (stmt.getPredicate().as(Resource.class))).getURI();
      } else {
        obj = stmt.getObject().toString();
      }
      log.debug("JMS changed statement: sub='" + sub + "' pred='" + pred + "' obj='" + obj + "'");
    }


    if (stmt.getSubject().isURIResource()) {
      String msg = sendMessage(stmt.getSubject().getURI(), from);
      send(msg);
      log.debug("Adding subject: " + stmt.getSubject().getURI());
    }


    if (stmt.getObject().isURIResource()) {
      String msg = sendMessage(((Resource) stmt.getObject()).getURI(), from);
      send(msg);
      log.debug("Adding message: " + msg);
    }

  }

  public void addedStatement(Statement statement) {
    log.debug("Adding single statement");
    addChange(statement, "single statement");
  }

  public void addedStatements(Statement[] statements) {
    log.debug("Adding array of statements");
    for (Statement s : statements) {
      addChange(s, "array of statements");
    }
  }

  public void addedStatements(List<Statement> statements) {
    log.debug("Adding list of statements");
    for (Statement s : statements) {
      addChange(s, "list of statements");
    }
  }

  public void addedStatements(StmtIterator stmtIterator) {
    log.debug("Adding iterator of statements");
    try {
      while (stmtIterator.hasNext()) {
        Statement s = stmtIterator.nextStatement();
        addChange(s, "iterator of statements");
      }
    } finally {
      stmtIterator.close();
    }
  }

  public void addedStatements(Model model) {
    log.debug("Adding statements from model");
    model.enterCriticalSection(Lock.READ);
    StmtIterator it = null;
    try {
      it = model.listStatements();
      while (it.hasNext()) {
        addChange(it.nextStatement(), "statements from model");
      }
    } finally {
      if (it != null) it.close();
      model.leaveCriticalSection();
    }
  }

  public void removedStatement(Statement statement) {
    log.debug("Removing single statement");
    addChange(statement, "remove statement");
  }

  public void removedStatements(Statement[] statements) {
    log.debug("Removing array of statements");
    for (Statement s : statements) {
      addChange(s, "remove array of statements");
    }
  }

  public void removedStatements(List<Statement> statements) {
    log.debug("Removing list of statements");
    for (Statement s : statements) {
      addChange(s, "remove list of statements");
    }
  }

  public void removedStatements(StmtIterator stmtIterator) {
    log.debug("Removing iterator of statements");
    try {
      while (stmtIterator.hasNext()) {
        Statement s = stmtIterator.nextStatement();
        addChange(s, "remove iterator of statements");
      }
    } finally {
      stmtIterator.close();
    }
  }

  public void removedStatements(Model model) {
    log.debug("Removing statements from model");
    model.enterCriticalSection(Lock.READ);
    StmtIterator it = null;
    try {
      it = model.listStatements();
      while (it.hasNext()) {
        addChange(it.nextStatement(), "removing statements from model");
      }
    } finally {
      if (it != null) it.close();
      model.leaveCriticalSection();
    }
  }

  public void notifyEvent(Model model, Object o) {

  }
}
