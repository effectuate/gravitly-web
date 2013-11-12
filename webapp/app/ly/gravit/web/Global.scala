package ly.gravit.web

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
import play.api._
import play.api.Play.current
import play.api.mvc.{Handler, RequestHeader}
import ly.gravit.web.dao.parseapi.AccountDaoImpl
import ly.gravit.web.auth.{Administrator, Account}
import play.api.libs.json.JsObject

/**
 * Created with IntelliJ IDEA.
 * User: ginduc
 * Date: 8/22/13
 * Time: 2:10 PM
 * To change this template use File | Settings | File Templates.
 */

object Global extends GlobalSettings with ParseApiConnectivity {
  private lazy val WS_TIMEOUT = Play.application.configuration.getInt("gravitly.ws.timeout").getOrElse(30)
  private lazy val ADMIN_EMAIL = Play.application.configuration.getString("gravitly.admin.email").getOrElse(
    throw new IllegalStateException("Admin email is required"))

  override def onRouteRequest(req: RequestHeader): Option[Handler] = {
    println("### Req: %s - %s".format(req.method, req.uri))
    implicit val request = req

    req.method match {
      case "POST" => {
        req.uri match {
          case "/admin/upload" => {
            if (!isApiRequestValid) {
              return Some(controllers.Application.invalidApiRequest)
            }
          }
          case _ => /* noop */
        }
      }
      case "GET" => {
        if (req.uri.startsWith("/environment")) {
          if (!isApiRequestValid) {
            return Some(controllers.Application.invalidApiRequest)
          }
        }
      }
      case _ => /* noop */
    }
    super.onRouteRequest(req)
  }

  def isApiRequestValid(implicit request: RequestHeader): Boolean = {
    (request.headers.get("X-Gravitly-Client-Id"),
      request.headers.get("X-Gravitly-REST-API-Key")) match {
      case (Some(appId), Some(restKey)) => {
        val criteria = """{"objectId":"%s", "restKey":"%s"}"""

        val req = parseApiConnect("ApiClient")
          .withQueryString("where" -> criteria.format(appId, restKey))

        val res =  Await.result(req.get, WS_TIMEOUT seconds)
        res.status match {
          case 200 => {
            if ((res.json \ "results").as[List[JsObject]].size > 0) {
              true
            } else {
              false
            }
          }
          case _ => false
        }
      }
      case _ => false
    }
  }

  override def onStart(app: Application) {
    if (Logger.isDebugEnabled) {
      //Logger.debug("Gravitly Web has started")
    }

    if (Play.isDev(Play.current)) {
      if (Logger.isDebugEnabled) {
        Logger.debug("Entering dev mode")
      }
      initDevMode
    }
  }

  override def onStop(app: Application) {
    if (Logger.isDebugEnabled) {
      //Logger.debug("Gravitly Web shut down...")
    }
  }

  private def initDevMode = {
    AccountDaoImpl.getByEmail(ADMIN_EMAIL) match {
      case Some(account) => /* noop */
      case None => {
        val admin = new Account(null, ADMIN_EMAIL, "password", ADMIN_EMAIL, Administrator)
        AccountDaoImpl.create(admin) match {
          case Some(id) => {
            if (Logger.isDebugEnabled) {
              Logger.debug("Admin ID: %s".format(id))
            }
          }
          case None => throw new IllegalStateException("Cannot create default admin account")
        }
      }
    }
  }
}
