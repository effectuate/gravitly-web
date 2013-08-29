package controllers

import scala.concurrent.ExecutionContext.Implicits.global
import play.api.mvc._
import ly.gravit.web._
import ly.gravit.web.auth.Account

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
    val query = parseApiConnect(CLASS_PHOTO, Option(id))
      .withQueryString("include" -> "user,location,category")

    Async{
      query.get.map { res =>
        val json = res.json
        println("#### json: " + json)
        val photo = Option(Photo((json \ "objectId").as[String],
          (json \ "caption").as[String], (json \ "filename").as[String],
          (json \ "user" \ "objectId").as[String], (json \ "location" \ "objectId").as[String],
          (json \ "category" \ "objectId").as[String]))

        val account = Option(Account((json \ "user" \ "objectId").as[String],
          null, null, (json \ "user" \ "username").as[String], null))

        val location = Option(Location((json \ "location" \ "objectId").as[String],
          (json \ "location" \ "name").as[String]))

        val category = Option(Category((json \ "category" \ "objectId").as[String],
          (json \ "category" \ "name").as[String]))

        Ok(views.html.photos.photo(photo, account, location, category))
      }
    }
  }
}
