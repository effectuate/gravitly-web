package controllers

import play.api._
import play.api.mvc._

/**
 * Created with IntelliJ IDEA.
 * User: ginduc
 * Date: 8/16/13
 * Time: 12:52 PM
 * To change this template use File | Settings | File Templates.
 */
object Photos extends Controller {
  def index(id: String) = Action {
    Ok(views.html.photo("Hello World!"))
  }
}
