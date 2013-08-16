package test

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import controllers._
import jp.t2v.lab.play2.auth.test.Helpers._

/**
 * http://www.playframework.com/documentation/2.0/ScalaFunctionalTest
 */
class ApplicationSpec extends Specification {

  object config extends AuthConfigImpl

  "Admin Index" should {
    "return login page when admin is not authenticated" in new WithApplication {
      val res = Admin.index(FakeRequest())

      status(res) must equalTo(SEE_OTHER)
      redirectLocation(res).map(_ must equalTo("/login")) getOrElse failure("missing redirect location")
    }

    "return admin index when admin is authorized" in new WithApplication {
      val res = Admin.index(FakeRequest().withLoggedIn(config)("1"))
      contentType(res) must beSome("text/html")
    }
  }

  "Admin Upload Page" should {
    "return login page when admin is not authenticated" in new WithApplication {
      val res = Admin.upload(FakeRequest())

      status(res) must equalTo(SEE_OTHER)
      redirectLocation(res).map(_ must equalTo("/login")) getOrElse failure("missing redirect location")
    }
    "return upload page when admin authorized" in new WithApplication {
      val res = Admin.upload(FakeRequest().withLoggedIn(config)("1"))
      contentType(res) must beSome("text/html")
      contentAsString(res) must contain("Upload")
    }
  }

  "Photos Page" should {
    "return a photo" in new WithApplication {
      val res = Photos.index("123")(FakeRequest())

      contentType(res) must beSome("text/html")
      status(res) must equalTo(OK)
      charset(res) must beSome("utf-8")
      contentAsString(res) must contain("Gravitly Photo")
    }
  }

  "Login Page" should {
    "return the Login page" in new WithApplication {
      val res = Application.login(FakeRequest())

      contentType(res) must beSome("text/html")
      status(res) must equalTo(OK)
      contentAsString(res) must contain("Admin Login")
    }
  }

  "SiteMap Page" should {
    "return a valid xml" in new WithApplication {
      val res = SiteMap.index(FakeRequest())

      status(res) must equalTo(OK)
      contentType(res) must beSome("text/xml")
      contentAsString(res) must contain("urlset")
    }
  }
}
