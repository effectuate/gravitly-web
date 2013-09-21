package controllers

import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import ly.gravit.web.auth.Account
import jp.t2v.lab.play2.auth.LoginLogout
import ly.gravit.web.dao.parseapi.AccountDaoImpl

object Application extends Controller with LoginLogout with AuthConfigImpl {
  val loginForm = Form {
    mapping("email" -> text, "password" -> text)(AccountDaoImpl.authenticate)(_.map(u => (u.email, u.password)))
      .verifying("Invalid email or password", result => result.isDefined)
  }
  
  def index = Action {
    Ok(views.html.index())
  }

  def login = Action { implicit request =>
    Ok(views.html.login(loginForm))
  }

  def logout = Action { implicit request =>
    // invalidate session?
    gotoLogoutSucceeded
  }

  def authenticate = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => {
        println("formWithErrors: " + formWithErrors)
        BadRequest(views.html.login(formWithErrors))
      },
      user => {
        println("user login sucess: " + user.get.permission)
        gotoLoginSucceeded(user.get.email)
      }
    )
  }
}