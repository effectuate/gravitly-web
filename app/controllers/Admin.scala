package controllers

import play.api.mvc._
import views.html._
import jp.t2v.lab.play2.auth.AuthElement
import ly.gravit.web.auth._
import play.api.data._
import play.api.data.Forms._
import ly.gravit.web.Photo

/**
 * Created with IntelliJ IDEA.
 * User: ginduc
 * Date: 8/16/13
 * Time: 1:01 PM
 * To change this template use File | Settings | File Templates.
 */

object Admin extends Controller with AuthElement with AuthConfigImpl {

  //def index = Action(AuthorityKey -> Administrator) { implicit request =>
  def index = Action { implicit request =>
    //Ok(admin.index(loggedIn))
    Ok(admin.index(null))

  }

  //def upload = StackAction(AuthorityKey -> Administrator) { implicit request =>
  def upload = Action { implicit request =>
    Ok(admin.upload(uploadForm))
  }

  val uploadForm : Form [Photo] = Form(
  mapping(
    "photoId" -> text,
    "photoCaption" -> text
  )(Photo.apply)(Photo.unapply)

  )
  def submitUpload = Action { implicit request =>
    uploadForm.bindFromRequest.fold(
      formWithErrors => {
       BadRequest
        Ok("not ok")
      },
      value => {
        val params = request.body.asFormUrlEncoded.get
        val photoId = request.body.asFormUrlEncoded.get("photoId").head;
        val photoCaption = request.body.asFormUrlEncoded.get("photoCaption").head;
        println("id --> "+photoId)
        println("caption --> "+photoCaption)
        println("params --> "+params)
        Redirect("/admin/upload") //TODO change the redirect page
      }
    )

  }
}
