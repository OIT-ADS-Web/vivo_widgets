import play._
import play.jobs._

import models._

//@OnApplicationStart
class LoadJenaCache extends Job {

  override def doJob() {
    Vivo.initializeJenaCache()
  }
}
