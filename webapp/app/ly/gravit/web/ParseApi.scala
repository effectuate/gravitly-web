package ly.gravit.web

import scala.concurrent.Future
import play.api.libs.ws._
import play.Logger

/**
 * Created with IntelliJ IDEA.
 * User: ginduc
 * Date: 8/26/13
 * Time: 8:53 PM
 * To change this template use File | Settings | File Templates.
 */
object ParseApi extends ParseApiConnectivity {

  def delete(className: String, objectId: String)(implicit sessionToken: String) = {
    if (objectId != null) {
      val query = parseApiConnect(className, Option(objectId))
        .withHeaders(PARSE_API_HEADER_SESSION -> sessionToken)

      if (Logger.isDebugEnabled) {
        Logger.debug("Delete: " + query.url)
      }

      query.delete
    } else {
      throw new IllegalStateException("Get methods must have an object id")
    }
  }

  def authenticate(username: String, password: String) = login(username, password)

  def create(className: String, properties: Map[String, Any]) = {
    if (properties != null) {
      val reqParams = new StringBuilder(512)
      var idx = 0

      properties.map { case (key, value) =>
        if (idx > 0) {
          reqParams.append(",")
        }
        value match {
          case s: String => reqParams.append("\"%s\":\"%s\"".format(key, value))
          case _ => reqParams.append("\"%s\":%s".format(key, value))
        }
        idx = idx + 1
      }

      val query = parseApiConnect(className)
        .withHeaders(PARSE_API_HEADER_CONTENT_TYPE -> CONTENT_TYPE_JSON)
      query.post("{%s}".format(reqParams.toString))

    } else {
      throw new IllegalStateException("Create needs class properties")
    }
  }

  def get(className: String, objectId: String): Future[Response] = {
    if (objectId != null) {
      val query = parseApiConnect(className, Option(objectId))

      if (Logger.isDebugEnabled) {
        Logger.debug("Get: " + query.url)
      }

      query.get
    } else {
      throw new IllegalStateException("Get methods must have an object id")
    }
  }

  def find(className: String, criteria: Map[String, Any]) = {
    if (criteria != null) {
      val reqParams = new StringBuilder(512)
      var idx = 0

      criteria.map { case (key, value) =>
        if (idx > 0) {
          reqParams.append(",")
        }
        value match {
          case s: String => reqParams.append("\"%s\":\"%s\"".format(key, value))
          case _ => reqParams.append("\"%s\":%s".format(key, value))
        }
        idx = idx + 1
      }

      val query = parseApiConnect(className)
        .withQueryString("where" -> "{%s}".format(reqParams.toString))

      if (Logger.isDebugEnabled) {
        Logger.debug("Find: " + query.url)
      }

      query.get
    } else {
      throw new IllegalStateException("Finder methods must have request parameters")
    }
  }
}
