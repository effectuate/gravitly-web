package controllers

import java.util.UUID
import scala.language.postfixOps
import scala.concurrent.Await
import scala.concurrent.duration._
import play.Logger
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import views.html._
import jp.t2v.lab.play2.auth.AuthElement
import ly.gravit.web.auth._
import ly.gravit.web._
import ly.gravit.web.Photo

/**
 * Created with IntelliJ IDEA.
 * User: ginduc
 * Date: 8/16/13
 * Time: 1:01 PM
 * To change this template use File | Settings | File Templates.
 */
object Admin extends BaseController
    with AuthElement
    with AuthConfigImpl
    with S3Connectivity
    with ParseApiConnectivity {

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
      "height" -> number,
      "location" -> nonEmptyText,
      "category" -> nonEmptyText
    )
  )
  
  def pugslife = Action { implicit request =>
    val ee =request.body.asFormUrlEncoded.get("message")
    println(ee)
    Status(200)
    
    
  }

  def postUpload = StackAction(AuthorityKey -> NormalUser) { implicit request =>
  //def postUpload = Action { implicit request =>
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
            create(Photo(null, uploadForm._1, filename, loggedIn.id, uploadForm._4,uploadForm._5))
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

  private def create(photo: Photo): Option[String] = {
    val reqParams = new StringBuilder(512)

    reqParams.append(""""caption":"%s",""".format(photo.caption))
    reqParams.append(""""filename":"%s",""".format(photo.filename))
    reqParams.append(""""user":{"__type":"Pointer","className":"_User","objectId":"%s"},""".format(photo.userId))
    reqParams.append(""""category":{"__type":"Pointer","className":"Category","objectId":"%s"},""".format(photo.categoryId))
    reqParams.append(""""location":{"__type":"Pointer","className":"Location","objectId":"%s"}""".format(photo.locationId))

    val req = parseApiConnect(CLASS_PHOTO)
      .withHeaders(PARSE_API_HEADER_CONTENT_TYPE -> CONTENT_TYPE_JSON)

    val res =  Await.result(req.post("{%s}".format(reqParams.toString)), WS_TIMEOUT seconds)
    println("### result: " + res.status + " | " +res.json)
    if (res.status == 201) {
      val objectId = (res.json \ "objectId").as[String]

      if (Logger.isDebugEnabled) {
        Logger.debug("Photo Uploaded: " + objectId)
      }
      return Option(objectId)
    }

    None
  }
}
