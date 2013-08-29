package controllers

import scala.concurrent.ExecutionContext.Implicits.global
import play.api.mvc._
import ly.gravit.web.{BaseController, ParseApiConnectivity, Photo}
import ly.gravit.web.auth.{Account}

/**
 * Created with IntelliJ IDEA.
 * User: ginduc
 * Date: 8/16/13
 * Time: 12:52 PM
 * To change this template use File | Settings | File Templates.
 */
object Photos extends BaseController with ParseApiConnectivity {

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
      .withQueryString("include" -> "user")

    Async{
      query.get.map { res =>
        val json = res.json
        val photo = Option(Photo(
          (json \ "objectId").as[String],
          (json \ "caption").as[String],
          (json \ "filename").as[String],
          (json \ "user" \ "objectId").as[String]
        ))
        val account = Option(Account(
          (json \ "user" \ "objectId").as[String],
          null,
          null,
          (json \ "user" \ "username").as[String],
          null
        ))
        Ok(views.html.photos.photo(photo, account))
      }
    }
  }
}
