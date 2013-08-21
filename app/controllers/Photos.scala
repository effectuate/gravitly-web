package controllers

import play.api._
import play.api.mvc._
//import dynobjx.play.parseapi.ParseApi

/**
 * Created with IntelliJ IDEA.
 * User: ginduc
 * Date: 8/16/13
 * Time: 12:52 PM
 * To change this template use File | Settings | File Templates.
 */
object Photos extends Controller {
  def index(id: String) = Action {
    //ParseApi.get("User", "Ij8j7HtrxC")
    Ok(views.html.photo("Hello World"))
  }
}
