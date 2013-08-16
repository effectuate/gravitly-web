package controllers

import play.api.mvc.{Action, Controller}
import views.html._
import jp.t2v.lab.play2.auth.LoginLogout

/**
 * Created with IntelliJ IDEA.
 * User: ginduc
 * Date: 8/16/13
 * Time: 1:01 PM
 * To change this template use File | Settings | File Templates.
 */
object Admin extends Controller with LoginLogout with AuthConfigImpl {
  def index = Action {
    Ok(admin.index())
  }
}
