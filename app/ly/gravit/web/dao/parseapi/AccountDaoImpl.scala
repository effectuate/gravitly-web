package ly.gravit.web.dao.parseapi

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
import play.api.Play
import play.api.Play.current
import ly.gravit.web.dao.AccountDao
import ly.gravit.web.auth.{Permission, Account}
import ly.gravit.web.ParseApi
import play.Logger
import play.api.libs.json.JsObject
import play.api.libs.Codecs

/**
 * Created with IntelliJ IDEA.
 * User: ginduc
 * Date: 8/19/13
 * Time: 11:38 AM
 * To change this template use File | Settings | File Templates.
 */
object AccountDaoImpl extends AccountDao {
  private lazy val PARSE_USER  = "_User"
  private lazy val WS_TIMEOUT = Play.application.configuration.getInt("gravitly.ws.timeout").getOrElse(30)

  override def authenticate(email: String, password: String): Option[Account] = {
    val pw = Codecs.md5(password.getBytes)
    val req = ParseApi.authenticate(email,pw)
    val res =  Await.result(req, WS_TIMEOUT seconds)

    if (Logger.isDebugEnabled) {
      Logger.debug("Authenticate: " + res.status)
    }

    if (res.status == 200) {
      val result = res.json

      return Option(Account(
        (result \ "objectId").as[String],
        (result\ "email").as[String],
        null,
        (result \ "username").as[String],
        Permission.valueOf((result \ "permission").as[String])))
    }
    None
  }

  override def getById(id: String): Option[Account] = {
    val req = ParseApi.get(PARSE_USER, id)
    val res =  Await.result(req, WS_TIMEOUT seconds)

    if(Logger.isDebugEnabled) {
      Logger.debug("Account.getById: " + res.status)
    }
    if (res.status == 200) {
      return Option(Account(
        (res.json \ "objectId").as[String],
        (res.json \ "email").as[String],
        null,
        (res.json \ "username").as[String],
        Permission.valueOf((res.json \ "permission").as[String])
      ))
    }
    None
  }

  override def getByEmail(email: String): Option[Account] = {
    val req = ParseApi.find(PARSE_USER, Map("email" -> email))
    val res =  Await.result(req, WS_TIMEOUT seconds)

    if(Logger.isDebugEnabled) {
      Logger.debug("Account.getByEmail: " + res.status)
    }
    if (res.status == 200) {
      (res.json \ "results").as[List[JsObject]].map { result =>
        return Option(Account(
          (result \ "objectId").as[String],
          (result \ "email").as[String],
          null,
          (result \ "username").as[String],
          Permission.valueOf((result \ "permission").as[String])
        ))
      }
    }
    None
  }

  override def create(account: Account): Option[String] = {
    val req = ParseApi.create(PARSE_USER, Map(
      "email" -> account.email,
      "password" -> Codecs.md5(account.password.getBytes),
      "username" -> account.email,
      "permission" -> account.permission.toString
    ))
    val res =  Await.result(req, WS_TIMEOUT seconds)

    if (res.status == 201) {
      return Option((res.json \ "objectId").as[String])
    }

    None
  }
}
