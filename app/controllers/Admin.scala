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
import ly.gravit.web.helpers.S3Helper

/**
 * Created with IntelliJ IDEA.
 * User: ginduc
 * Date: 8/16/13
 * Time: 1:01 PM
 * To change this template use File | Settings | File Templates.
 */

object Admin extends Controller with AuthElement with AuthConfigImpl {


  private val config = play.api.Play.current.configuration
  private val bucketName = config.getString("s3.uploads.bucket").get
  println("bucketName -> "+ bucketName)
  val bucket = S3(bucketName)



  def index = StackAction(AuthorityKey -> Administrator) { implicit request =>
  //def index = Action { implicit request =>
    Ok(admin.index(loggedIn))
    //Ok(admin.index(null))

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
   private def generateFileName(mineType: Option[String]) = {
      UUID.randomUUID().toString() + generateExtension(mineType)
    }
    private def generateExtension(mineType: Option[String]) = {
        mineType match {
          case Some("image/png") => ".png"
          case Some("image/gif") => ".gif"
          case Some("image/jpeg") => ".jpeg"
          case Some(_) => ".jpeg"
          case None => ".jpeg"
        }
      }

  def submitUpload = Action(parse.multipartFormData) { implicit request =>
    request.body.file("imageName") match {
        case Some(file) => {
          val files  = file.ref.file.getAbsoluteFile()
          val contentType = file.contentType
          //convert file to bit array
          val source = Source.fromFile(files)(scala.io.Codec.ISO8859)
          val byteArray = source.map(_.toByte).toArray
          source.close
          val imageName =   Admin.generateFileName(contentType)
          println(imageName)
          val result = bucket.add(BucketFile(imageName, contentType.get, byteArray, Some(PUBLIC_READ)))

          //comment this is for error handling of s3
          /*result.map {
            case Left(error) => Logger.error("S3 error : " + error)
            None
            case Right(success) =>  {Logger.debug("Saved the file")
              Some(imageName)
            }
          }
        }*/
          Ok("file has been uplaoded")
    }
    }

}
}
