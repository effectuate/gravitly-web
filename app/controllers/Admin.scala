package controllers

import java.util.UUID
import play.{Play, Logger}
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import views.html._
import jp.t2v.lab.play2.auth.AuthElement
import ly.gravit.web.auth._
import ly.gravit.web.{S3Connectivity, Photo}
import ly.gravit.web.dao.parseapi.PhotoDaoImpl

/**
 * Created with IntelliJ IDEA.
 * User: ginduc
 * Date: 8/16/13
 * Time: 1:01 PM
 * To change this template use File | Settings | File Templates.
 */
object Admin extends Controller
    with AuthElement
    with AuthConfigImpl
    with S3Connectivity {

  lazy val S3_PHOTOS = Play.application.configuration.getString("s3.uploads.bucket")

  def index = StackAction(AuthorityKey -> Administrator) { implicit request =>
    Ok(admin.index(loggedIn))
  }

  def upload = StackAction(AuthorityKey -> Administrator) { implicit request =>
    Ok(admin.upload(uploaderForm))
  }

  val uploaderForm = Form(
    tuple(
      "caption" -> nonEmptyText,
      "width" -> number,
      "height" -> number
    )
  )

  //def postUpload = StackAction(AuthorityKey -> NormalUser) { implicit request =>
  def postUpload = Action { implicit request =>
    uploaderForm.bindFromRequest.fold(
      errors => BadRequest(admin.upload(errors)),
      uploadForm => {
        if(Logger.isDebugEnabled) {
          Logger.debug("### UploadForm: " + uploadForm)
        }

        request.body.asMultipartFormData match {
          case Some(multi) => {
            val filePart = multi.file("image").get
            val byteArray = toByteArray(filePart.ref.file)
            val filename = "%s.%s".format(UUID.randomUUID().toString, fileExtension(filePart.contentType))

            // Upload to S3
            upload(S3_PHOTOS, byteArray, filename, filePart.contentType.get)
            // Save Photo info on Parse
            PhotoDaoImpl.create(Photo(null, uploadForm._1, filename))
          }
          case None => {
            if(Logger.isDebugEnabled) {
              Logger.debug("Can't find file to upload")
            }
          }
        }

        Ok(admin.upload(uploaderForm))
      }
    )
  }
}
