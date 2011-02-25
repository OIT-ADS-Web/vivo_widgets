import play.test._;
import models._;
import org.scalatest.matchers.ShouldMatchers

class BasicScalaTest extends UnitSpec with ShouldMatchers {

  describe("the truth") {
    it("should be truthy") {
      1 should equal (1)
    }
  }

}
