package ly.gravit.web

import play.api.Play
import play.api.Play.current
import play.api.libs.ws.WS

/**
 * Created with IntelliJ IDEA.
 * User: ginduc
 * Date: 8/29/13
 * Time: 1:26 PM
 * To change this template use File | Settings | File Templates.
 */
trait ParseApiConnectivity {
  lazy val PARSE_API_URL = "https://api.parse.com"
  lazy val PARSE_API_URL_CLASSES = "/1/classes/"
  lazy val PARSE_API_AUTH_URL = "/1/login"
  lazy val PARSE_API_HEADER_APP_ID = "X-Parse-Application-Id"
  lazy val PARSE_API_HEADER_REST_API_KEY = "X-Parse-REST-API-Key"
  lazy val PARSE_API_HEADER_CONTENT_TYPE = "Content-Type"
  lazy val PARSE_API_HEADER_SESSION = "X-Parse-Session-Token"
  lazy val CONTENT_TYPE_JSON = "application/json"

  lazy val APP_ID = Play.application.configuration.getString("parseapi.app.id").get
  lazy val REST_API_KEY = Play.application.configuration.getString("parseapi.restapi.key").get

  private val parseBaseUrl = "%s%s".format(PARSE_API_URL, PARSE_API_URL_CLASSES)

  def parseApiConnect(className: String, objectId: Option[String] = None) = objectId match {
    case Some(id) => WS.url("%s%s/%s".format(parseBaseUrl, className, id))
      .withHeaders(PARSE_API_HEADER_APP_ID -> APP_ID)
      .withHeaders(PARSE_API_HEADER_REST_API_KEY -> REST_API_KEY)

    case None => WS.url("%s%s".format(parseBaseUrl, className))
      .withHeaders(PARSE_API_HEADER_APP_ID -> APP_ID)
      .withHeaders(PARSE_API_HEADER_REST_API_KEY -> REST_API_KEY)
  }

  def login(username: String, password: String) = {
    val query = WS.url("%s%s".format(PARSE_API_URL, PARSE_API_AUTH_URL))
      .withHeaders(PARSE_API_HEADER_APP_ID -> APP_ID)
      .withHeaders(PARSE_API_HEADER_REST_API_KEY -> REST_API_KEY)
      .withQueryString("username" -> username)
      .withQueryString("password" -> password)

    query.get
  }
}
