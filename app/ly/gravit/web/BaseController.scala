package ly.gravit.web

import play.api.mvc.Controller
import play.api.Play
import play.api.Play.current

/**
 * Created with IntelliJ IDEA.
 * User: ginduc
 * Date: 8/29/13
 * Time: 9:32 PM
 * To change this template use File | Settings | File Templates.
 */
trait BaseController extends Controller {
  lazy val WS_TIMEOUT = Play.application.configuration.getInt("gravitly.ws.timeout").getOrElse(30)

  private lazy val IMAGE_SERVER_URL = Play.application.configuration.getString("image.server.url").get
  private lazy val BASE_IMAGE_URI = Play.application.configuration.getString("s3.uploads.bucket").get
  implicit val imagePath = "%s/%s".format(IMAGE_SERVER_URL, BASE_IMAGE_URI)
}
