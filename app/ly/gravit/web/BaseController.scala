package ly.gravit.web

import play.api.mvc.Controller
import play.Play

/**
 * Created with IntelliJ IDEA.
 * User: ginduc
 * Date: 8/29/13
 * Time: 9:32 PM
 * To change this template use File | Settings | File Templates.
 */
trait BaseController extends Controller {
  lazy val S3_PHOTOS = Play.application.configuration.getString("s3.uploads.bucket")

  private lazy val IMAGE_SERVER_URL = Play.application.configuration.getString("image.server.url")
  private lazy val BASE_IMAGE_URI = Play.application.configuration.getString("s3.uploads.bucket")
  implicit val imagePath = "%s/%s".format(IMAGE_SERVER_URL, BASE_IMAGE_URI)
}
