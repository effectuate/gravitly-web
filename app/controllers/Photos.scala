package controllers

import scala.concurrent.ExecutionContext.Implicits.global
import play.api.mvc._
import play.Play
import ly.gravit.web.{ParseApiConnectivity, Photo}

/**
 * Created with IntelliJ IDEA.
 * User: ginduc
 * Date: 8/16/13
 * Time: 12:52 PM
 * To change this template use File | Settings | File Templates.
 */
object Photos extends Controller with ParseApiConnectivity {
  private lazy val IMAGE_SERVER_URL = Play.application.configuration.getString("image.server.url")
  private lazy val BASE_IMAGE_URI = Play.application.configuration.getString("s3.uploads.bucket")
  implicit val imagePath = "%s/%s".format(IMAGE_SERVER_URL, BASE_IMAGE_URI)

  /*
    Retaining this for posterity; old, 'modular', asynchronous way of retrieving data from a web service.

    def index(id: String) = Action {
      Async {
        for {
          photo <- Future{ PhotoDaoImpl.getById(id) }
        } yield {
          Ok(views.html.photos.photo(photo))
        }
      }
    }

    New index() represents the Play 2.0 way of doing things
  */
  def index(id: String) = Action {

    val query = parseApiConnect("Photo", Option(id))

    Async{
      query.get.map { res =>
        val photo = Option(Photo(
          (res.json \ "objectId").as[String],
          (res.json \ "caption").as[String],
          (res.json \ "filename").as[String]
        ))
        Ok(views.html.photos.photo(photo))
      }
    }
  }
}
