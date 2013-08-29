package controllers

import scala.concurrent.ExecutionContext.Implicits.global
import play.api.mvc._
import ly.gravit.web.ParseApi
import ly.gravit.web.Photo
import java.text.SimpleDateFormat
import util.Random
import ly.gravit.web.dao.parseapi.AccountDaoImpl

/**
 * Created with IntelliJ IDEA.
 * User: ginduc
 * Date: 8/16/13
 * Time: 12:52 PM
 * To change this template use File | Settings | File Templates.
 */
object Photos extends Controller {

  def index(id: String) = Action {
    //Async {
      //ParseApi.get("_User", "XQa0OaRUET").map { res =>
      val res = AccountDaoImpl.getByEmail("admin@gravit.ly")
      //ParseApi.find("_User", Map("email" -> "admin@gravit.ly")).map { res =>
        //Ok("JSON: " + res.json)
        //Ok("ObjectId: " + (res.json \ "objectId").as[String])
      //}
    //}
    Ok(views.html.photos.photo(res.get.id))
  }

  def getMockPhotoGalleryGridView(id : String) = Action {
    ParseApi.get("User", "Ij8j7HtrxC")
    Ok(views.html.photos._grid(mockPhotoGalleryGridView.toString))

  }

  def getMockPhotoGalleryScrollView(id : String) = Action {
      ParseApi.get("User", "Ij8j7HtrxC")
      Ok(views.html.photos._scroll(mockPhotoGalleryScrollView.toString))
    }

  def mockPhotoGalleryGridView() =  {
  val date = new SimpleDateFormat("yyyy/MM/dd")
  Seq(
    Photo(Random.nextInt(1234567).toString,"caption1",date.parse("2013/03/" + (Random.nextInt(29) + 1))),
    Photo(Random.nextInt(1234567).toString,"caption2",date.parse("2013/03/" + (Random.nextInt(29) + 1))),
    Photo(Random.nextInt(1234567).toString,"caption3",date.parse("2013/03/" + (Random.nextInt(29) + 1))),
    Photo(Random.nextInt(1234567).toString,"caption4",date.parse("2013/03/" + (Random.nextInt(29) + 1))),
    Photo(Random.nextInt(1234567).toString,"caption5",date.parse("2013/03/" + (Random.nextInt(29) + 1))),
    Photo(Random.nextInt(1234567).toString,"caption6",date.parse("2013/03/" + (Random.nextInt(29) + 1))),
    Photo(Random.nextInt(1234567).toString,"caption7",date.parse("2013/03/" + (Random.nextInt(29) + 1))),
    Photo(Random.nextInt(1234567).toString,"caption8",date.parse("2013/03/" + (Random.nextInt(29) + 1)))
  )

  }
  def mockPhotoGalleryScrollView() =  {
    val date = new SimpleDateFormat("yyyy/MM/dd")
    Seq(
      Photo(Random.nextInt(1234567).toString,"caption1",date.parse("2013/03/" + (Random.nextInt(29) + 1))),
      Photo(Random.nextInt(1234567).toString,"caption2",date.parse("2013/03/" + (Random.nextInt(29) + 1))),
      Photo(Random.nextInt(1234567).toString,"caption3",date.parse("2013/03/" + (Random.nextInt(29) + 1))),
      Photo(Random.nextInt(1234567).toString,"caption4",date.parse("2013/03/" + (Random.nextInt(29) + 1))),
      Photo(Random.nextInt(1234567).toString,"caption5",date.parse("2013/03/" + (Random.nextInt(29) + 1))),
      Photo(Random.nextInt(1234567).toString,"caption6",date.parse("2013/03/" + (Random.nextInt(29) + 1))),
      Photo(Random.nextInt(1234567).toString,"caption7",date.parse("2013/03/" + (Random.nextInt(29) + 1))),
      Photo(Random.nextInt(1234567).toString,"caption8",date.parse("2013/03/" + (Random.nextInt(29) + 1)))
    )

    }
}
