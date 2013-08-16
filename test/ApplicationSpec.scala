package test

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import controllers.{Admin, AuthConfigImpl}
import jp.t2v.lab.play2.auth.test.Helpers._
/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
class ApplicationSpec extends Specification {

  object config extends AuthConfigImpl

  "Admin Index" should {
    "return list when user is authorized" in new WithApplication {
      val res = Admin.index(FakeRequest().withLoggedIn(config)("1"))
      contentType(res).get must equalTo("text/html")
    }
  }
}
