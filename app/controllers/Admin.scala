package controllers

import play.api.mvc.Controller
import views.html._
import jp.t2v.lab.play2.auth.AuthElement
import com.gravitly.web.auth.Administrator

/**
 * Created with IntelliJ IDEA.
 * User: ginduc
 * Date: 8/16/13
 * Time: 1:01 PM
 * To change this template use File | Settings | File Templates.
 */
object Admin extends Controller with AuthElement with AuthConfigImpl {

  def index = StackAction(AuthorityKey -> Administrator) { implicit request =>
    println("### Inside Admin Index")
    val user = loggedIn
    Ok(admin.index(user))
  }

}
