package controllers

import java.util.{UUID, Date}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.Source
import play.{Play, Logger}
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import views.html._
import jp.t2v.lab.play2.auth.AuthElement
import fly.play.s3._
import fly.play.s3.PUBLIC_READ
import ly.gravit.web.auth._
import ly.gravit.web.{Photo, UploadPhoto}
import ly.gravit.web.helpers._
import ly.gravit.web.dao.parseapi.PhotoDaoImpl

/**
 * Created with IntelliJ IDEA.
 * User: ginduc
 * Date: 8/16/13
 * Time: 1:01 PM
 * To change this template use File | Settings | File Templates.
 */
object Admin extends Controller with AuthElement with AuthConfigImpl  {
  lazy val S3_PHOTOS = Play.application.configuration.getString("s3.uploads.bucket")

  def index = StackAction(AuthorityKey -> Administrator) { implicit request =>
    Ok(admin.index(loggedIn))
  }

  //def upload = StackAction(AuthorityKey -> Administrator) { implicit request =>
  def upload = Action { implicit request =>

    Ok(admin.upload(uploaderForm))
  }

  val uploadForm : Form [UploadPhoto] = Form(
    mapping(
      "imageName" -> text
    )(UploadPhoto.apply)(UploadPhoto.unapply)
  )

  val uploaderForm = Form(
    tuple(
      "caption" -> nonEmptyText,
      "width" -> number,
      "height" -> number
    )
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

  def postUpload = StackAction(AuthorityKey -> NormalUser) { implicit request =>
    uploaderForm.bindFromRequest.fold(
      errors => BadRequest(admin.upload(errors)),
      uploadForm => {
        if(Logger.isDebugEnabled) {
          Logger.debug("### UploadForm: " + uploadForm)
        }

        // Upload to S3
        request.body.asMultipartFormData match {
          case Some(multi) => {
            val filepart = multi.file("image").get

            println("file: " + filepart.ref.file)

            val src = Source.fromFile(filepart.ref.file)(scala.io.Codec.ISO8859)
            val byteArray = src.map(_.toByte).toArray
            src.close

            s3upload(byteArray, UUID.randomUUID().toString+".png", filepart.contentType.get)
          }
          case None => {
            if(Logger.isDebugEnabled) {
              Logger.debug("Can't find file to upload")
            }
          }
        }

        // Save photo info on Parse
        postToParse(Photo(null, uploadForm._1, new Date))

        Ok(admin.upload(uploaderForm))
      }
    )
  }

  private def s3upload(byteArray: Array[Byte], fileName: String, mimeType: String) = {
    val bucket = S3(S3_PHOTOS)
    val result = bucket.add(BucketFile(fileName, mimeType, byteArray, Some(PUBLIC_READ)))

    result.map { unit =>
      if (Logger.isDebugEnabled) {
        Logger.debug("File uploaded to S3")
      }
    }
    .recover {
      case S3Exception(status, code, message, originalXml) => Logger.error("Error: " + message)
    }
  }

  private def postToParse(photo: Photo) = {
    val objectId = PhotoDaoImpl.create(photo)

    if (Logger.isDebugEnabled) {
      Logger.debug("Photo info posted on Parse: " +objectId)
    }
  }
}
