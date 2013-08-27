package controllers

import scala.concurrent.ExecutionContext.Implicits.global
import play.api.mvc._
import ly.gravit.web.ParseApi

/**
 * Created with IntelliJ IDEA.
 * User: ginduc
 * Date: 8/16/13
 * Time: 12:52 PM
 * To change this template use File | Settings | File Templates.
 */
object Photos extends Controller {
  def index(id: String) = Action {
    Async {
      //ParseApi.get("_User", "XQa0OaRUET").map { res =>
      ParseApi.find("_User", Map("email" -> "admin@gravitly.com")).map { res =>
        Ok("JSON: " + res.json)
        //Ok("ObjectId: " + (res.json \ "objectId").as[String])
      }
    }
    //Ok(views.html.photo("Hello World"))
  }
}
