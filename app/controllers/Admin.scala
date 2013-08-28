package controllers

import play.api.mvc._
import views.html._
import jp.t2v.lab.play2.auth.AuthElement
import ly.gravit.web.auth._
import play.api.data._
import play.api.data.Forms._
import ly.gravit.web.UploadPhoto
import org.reflections.vfs.Vfs.File
import fly.play.s3._
import fly.play.s3.PUBLIC_READ
import scala.io.Source
import java.util.UUID
import ly.gravit.web.helpers._

/**
 * Created with IntelliJ IDEA.
 * User: ginduc
 * Date: 8/16/13
 * Time: 1:01 PM
 * To change this template use File | Settings | File Templates.
 */

object Admin extends Controller with AuthElement with AuthConfigImpl  {

  def index = StackAction(AuthorityKey -> Administrator) { implicit request =>
    Ok(admin.index(loggedIn))
  }

  //def upload = StackAction(AuthorityKey -> Administrator) { implicit request =>
  def upload = Action { implicit request =>
    Ok(admin.upload(uploadForm))
  }

  val uploadForm : Form [UploadPhoto] = Form(
    mapping(
      "imageName" -> text
    )(UploadPhoto.apply)(UploadPhoto.unapply)
  )

  def submitUpload = Action(parse.multipartFormData) { implicit request =>
    request.body.file("imageName") match {
        case Some(file) => {
          val files  = file.ref.file.getAbsoluteFile()
          val contentType = file.contentType
            S3Helper.S3Uploader (files, contentType)
          Ok("file has been uplaoded")
     }
     case None => Ok("File Error")
    }

}
}
