package test

import scala.concurrent.duration._
import org.specs2.mutable.Specification
import ly.gravit.web.ParseApi
import play.api.libs.Codecs
import ly.gravit.web.auth.NormalUser
import scala.concurrent._
import play.api.test.WithApplication
import java.util.UUID
import play.api.libs.json.JsObject

/**
 * Created with IntelliJ IDEA.
 * User: ginduc
 * Date: 8/28/13
 * Time: 12:05 AM
 * To change this template use File | Settings | File Templates.
 */
class ParseApiSpec extends Specification {
  lazy val timeOutInMillis = 30000

  "ParseApi" should {
    val testEmail = UUID.randomUUID().toString + "@test.com"
    val password = "password"
    implicit var sessionToken: String = null

    "create a new record on create()" in new WithApplication {
      val req = ParseApi.create("_User", Map(
        "email" -> testEmail,
        "password" -> Codecs.md5(password.getBytes),
        "username" -> testEmail,
        "permission" -> NormalUser.toString
      ))
      val res =  Await.result(req, timeOutInMillis milli)

      res.status must_== 201
      res.json must_!= null
      (res.json \ "objectId").as[String] must_!= null
    }

    "authenticate user on authenticate()" in new WithApplication {
      val req = ParseApi.authenticate(testEmail, Codecs.md5(password.getBytes))
      val res =  Await.result(req, timeOutInMillis milli)

      res.status must_== 200
      res.json must_!= null
      (res.json \ "email").as[String] must_== testEmail
      sessionToken = (res.json \ "sessionToken").as[String]
      sessionToken must_!= null
    }

    "delete a record on delete()" in new WithApplication {
      val res = Await.result(ParseApi.find("_User", Map("email" -> testEmail)), timeOutInMillis milli)

      res.status must_== 200
      res.json must_!= null
      (res.json \ "results").as[List[JsObject]].map {result =>
        (result \ "email").as[String] must_== testEmail

        val getRes = Await.result(ParseApi.get("_User", (result \ "objectId").as[String]), timeOutInMillis milli)
        getRes.status must_== 200
        getRes.json must_!= null

        (getRes.json \ "email").as[String] must_== testEmail

        val delRes = Await.result(ParseApi.delete("_User", (getRes.json \ "objectId").as[String]), timeOutInMillis milli)
        delRes.status must_== 200
        delRes.json must_!= null
      }
    }
  }
}
