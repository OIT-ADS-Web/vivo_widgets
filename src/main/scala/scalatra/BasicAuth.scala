package edu.duke.oit.vw.scalatra

import _root_.org.scalatra.auth.strategy.{BasicAuthStrategy, BasicAuthSupport}
import _root_.org.scalatra.auth.{ScentrySupport, ScentryConfig}
// import edu.duke.oit.vw.scalatra.BasicAuth.AuthenticationSupport
import _root_.org.scalatra._

object BasicAuth {

  case class MyUser(id: String)

  class OurBasicAuthStrategy(protected override val app: ScalatraBase, realm: String)
    extends BasicAuthStrategy[MyUser](app, realm) {

    protected def validate(userName: String, password: String): Option[MyUser] = {
      if(userName == WidgetsConfig.updatesUserName && 
         password == WidgetsConfig.updatesPassword) {
        Some(MyUser("scalatra"))
      }
      else None
    }

    protected def getUserId(user: MyUser): String = user.id
  }

  trait AuthenticationSupport extends ScentrySupport[MyUser] with BasicAuthSupport[MyUser] { 
    self: ScalatraBase =>
    
    val realm = "Widget Updates"
    // protected def contextPath = request.getContextPath

    protected def fromSession = { case id: String => MyUser(id)  }
    protected def toSession   = { case usr: MyUser => usr.id }

    protected val scentryConfig = (new ScentryConfig {}).asInstanceOf[ScentryConfiguration]

    override protected def configureScentry = {
      scentry.unauthenticated {
        scentry.strategies("Basic").unauthenticated()
      }
    }
    
    override protected def registerAuthStrategies = {
      scentry.register("Basic", app => new OurBasicAuthStrategy(app, realm))
    }
  }
}

