package controllers

import scala.concurrent.ExecutionContext.Implicits.global
import play.api.mvc._
import views.html._
import ly.gravit.web._
import ly.gravit.web.auth.Account
import play.api.libs.json.JsObject
import scala.collection.mutable
import java.util.Date
import play.data.format.Formats.DateTime

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
        //println("#### json: " + json)
        val photo = Option(Photo.fromJson(json))

        val account = Option(Account((json \ "user" \ "objectId").as[String],
          null, null, (json \ "user" \ "username").as[String], null))

        val location = Option(Location((json \ "location" \ "objectId").as[String],
          (json \ "location" \ "name").as[String]))

        val category = Option(Category((json \ "category" \ "objectId").as[String],
          (json \ "category" \ "name").as[String]))

        Ok(photos.photo(photo, account, location, category))
      }
    }
  }

  def photosByUser(id: String) = Action {
    val reqParams = new StringBuilder(512)
    reqParams.append(""""user":{"__type":"Pointer","className":"_User","objectId":"%s"}""".format(id))

    val query = parseApiConnect(CLASS_PHOTO)
      .withQueryString("where" -> "{%s}".format(reqParams.toString))
      .withQueryString("order" -> "-createdAt")
      .withQueryString("include" -> "user,location,category")

    Async {
      query.get.map {res =>
        val resultJson = res.json
        //println("#### photosByJson: " + resultJson)

        val photoMap = new mutable.LinkedHashMap[String, Tuple4[Photo, Account, Location, Category]]()

        (resultJson \ "results").as[List[JsObject]].map {json =>
        val photo = Option(Photo.fromJson(json))

        val account = Option(Account((json \ "user" \ "objectId").as[String],
          null, null, (json \ "user" \ "username").as[String], null))

        val location = Option(Location((json \ "location" \ "objectId").as[String],
          (json \ "location" \ "name").as[String]))

        val category = Option(Category((json \ "category" \ "objectId").as[String],
          (json \ "category" \ "name").as[String]))

          photoMap put (photo.get.id.get, (photo.get, account.get, location.get, category.get))

        }
        Ok(photos.stream(photoMap))
      }
    }
  }

  def photosByTag(tag: String) = Action {
    val reqParams = new StringBuilder(512)
    reqParams.append(""""hashTags":{"$all":["%s"]} """.format(tag))

    val query = parseApiConnect(CLASS_PHOTO)
      .withQueryString("where" -> "{%s}".format(reqParams.toString))
      .withQueryString("order" -> "-createdAt")
      .withQueryString("include" -> "user,location,category")

    Async {
      query.get.map {res =>
        val resultJson = res.json
        //println("#### photosByJson: " + resultJson)

        val photoMap = new mutable.LinkedHashMap[String, Tuple4[Photo, Account, Location, Category]]()

        (resultJson \ "results").as[List[JsObject]].map {json =>
          val photo = Option(Photo.fromJson(json))

          val account = Option(Account((json \ "user" \ "objectId").as[String],
            null, null, (json \ "user" \ "username").as[String], null))

          val location = Option(Location((json \ "location" \ "objectId").as[String],
            (json \ "location" \ "name").as[String]))

          val category = Option(Category((json \ "category" \ "objectId").as[String],
            (json \ "category" \ "name").as[String]))

          photoMap put (photo.get.id.get, (photo.get, account.get, location.get, category.get))

        }

        Ok(photos.stream(photoMap))
      }
    }
  }

  def siteMap = Action {implicit request =>
    val query = parseApiConnect(CLASS_PHOTO)
      .withQueryString("order" -> "-createdAt")

    Async {
      query.get.map {res =>
        val resultJson = res.json
        //println("#### photosByJson: " + resultJson)

        val photoMap = new mutable.LinkedHashMap[String, Photo]()

        (resultJson \ "results").as[List[JsObject]].map {json =>
          val photo = Option(Photo.fromJson(json))

          photoMap put (photo.get.id.get, photo.get)
        }

        Ok(views.xml.photos.stream(photoMap))
      }
    }
  }
}
