package controllers

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps
import scala.concurrent.{Future, Await}
import scala.concurrent.duration._
import play.api.Play.current
import play.Logger
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import views.html._
import jp.t2v.lab.play2.auth.AuthElement
import ly.gravit.web.auth._
import ly.gravit.web._
import ly.gravit.web.Photo
import java.io.File
import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.{Tag, Directory, Metadata}
import play.api.Play
import play.api.libs.ws.WS
import play.api.libs.json.{JsArray, JsObject}

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
      Async{
        Details.getLocations.map{
          loc =>
            Async{
              Details.getCategories.map{
                cat =>
                Ok(admin.upload(uploaderForm,loc.toList, cat.toList))
              }

        }
      }
    }
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
      errors =>
        Async{
        Details.getLocations.map{ loc =>
          Async{
            Details.getCategories.map{ cat =>
              BadRequest(admin.upload(errors,loc.toList,cat.toList))
            }
          }

        }
       },
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
            //upload(S3_PHOTOS, byteArray, filename, filePart.contentType.get)

            // Save Photo info on Parse
            val exif = exifData(filePart.ref.file)
            //create(Photo(null, uploadForm._1, filename, loggedIn.id, uploadForm._4,uploadForm._5))
          }
          case None => {
            if(Logger.isDebugEnabled) {
              Logger.debug("Can't find file to upload")
            }
          }
        }
        Async{
          Details.getLocations.map{loc =>
            Async{
              Details.getCategories.map{ cat =>
                Ok(admin.upload(uploaderForm,loc.toList, cat.toList))
              }
            }

          }
        }

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

  /*
    #### EXIF TAG: [Jpeg] Compression Type - Baseline
    #### EXIF TAG: [Jpeg] Data Precision - 8 bits
    #### EXIF TAG: [Jpeg] Image Height - 308 pixels
    #### EXIF TAG: [Jpeg] Image Width - 545 pixels
    #### EXIF TAG: [Jpeg] Number of Components - 3
    #### EXIF TAG: [Jpeg] Component 1 - Y component: Quantization table 0, Sampling factors 2 horiz/2 vert
    #### EXIF TAG: [Jpeg] Component 2 - Cb component: Quantization table 1, Sampling factors 1 horiz/1 vert
    #### EXIF TAG: [Jpeg] Component 3 - Cr component: Quantization table 1, Sampling factors 1 horiz/1 vert
    #### EXIF TAG: [Jfif] Version - 1.2
    #### EXIF TAG: [Jfif] Resolution Units - none
    #### EXIF TAG: [Jfif] X Resolution - 100 dots
    #### EXIF TAG: [Jfif] Y Resolution - 100 dots
    #### EXIF TAG: [Adobe Jpeg] DCT Encode Version - 1
    #### EXIF TAG: [Adobe Jpeg] Flags 0 - 192
    #### EXIF TAG: [Adobe Jpeg] Flags 1 - 0
    #### EXIF TAG: [Adobe Jpeg] Color Transform - YCbCr
   */
  private def exifData(imageFile: File) = {
    val metadata: Metadata = ImageMetadataReader.readMetadata(imageFile)
    val iter: java.util.Iterator[Directory] = metadata.getDirectories.iterator

    while(iter.hasNext) {
      val directory = iter.next
      val tagIter = directory.getTags.iterator

      while (tagIter.hasNext) {
        val tag = tagIter.next

        println("#### EXIF TAG: " + tag)
      }
    }
  }



  val sierraAtTahoe = "CoQBdAAAANahspttjHfS875axpTChB9K17fFVW3beJ6l_4kTulu_eRbwAH1GzyGYL8KetHXcW-v1w66rLY3sUgd5Jpp0HrGTXoO7b7ad2zJCac8WjVJOAnUI9vuaZcaMu1fwyiGOfqzdnWL0kAb7A2rl0g7IZhShJfjSnyuy7q3FNoa3DWGvEhAzU1Ysyhf0HL_TD6Bd-PEJGhTjedH1ZjfDrBAOM4FI_sRw2xBGDg"

  def meta(category: String) = Action {
      category match {
        case "rph0ovXefp" => {
          Async {
            // wwo
            val wwoUrl = Play.application.configuration.getString("meta.api.wwo.url").get
            val wwoKey = Play.application.configuration.getString("meta.api.wwo.key").get
            val wwoDs = Play.application.configuration.getString("meta.api.wwo.dataset").get.split(",")
            val req = "%s%s?%s&format=json&key=%s".format(wwoUrl, "/marine.ashx", "q=45%2C-2", wwoKey)

            /*pullData(req, wwoDs).map { m =>
              Ok(m.toString)
            }*/

            getGooglePlace(sierraAtTahoe).map { gp=>
              Ok(gp.toString)
            }
          }
        }

        case _ => BadRequest("Unsupported category.")
      }
  }

  private def pullData(url: String, dataSet: Array[String]): Future[Map[String, Any]] = {
    if (Logger.isDebugEnabled) {
      Logger.debug("3rdParty: "  +url)
    }

    Future {
      var map = Map[String, Any]()
      val req = WS.url(url).get
      val res = Await.result(req, 20 seconds)

      val json = res.json
      val weatherJson = (json \ "data" \ "weather").as[List[JsObject]].head
      val hourly = (weatherJson \ "hourly").as[List[JsObject]].head

      dataSet.map{ key =>
        println("key: " + key)
        map += (key -> (hourly \ key).as[String])
      }

      map
    }
  }


  private def getGooglePlace(reference: String): Future[Map[String, Any]] = {
    Future {
      val gpUrl = Play.application.configuration.getString("meta.api.goglplaces.url").get
      val gpKey = Play.application.configuration.getString("meta.api.goglplaces.key").get
      val url = "%s?reference=%s&sensor=true&key=%s".format(gpUrl,reference,gpKey)

      if (Logger.isDebugEnabled) {
        Logger.debug("3rdParty: "  +url)
      }
      var map = Map[String, Any]()
      val req = WS.url(url).get
      val res = Await.result(req, 20 seconds)
      val json = res.json
      val results = (json \ "result")

      (results \ "address_components").as[List[JsObject]].map { ac =>
        (ac \ "types").as[JsArray].value.map { t =>
          t.as[String] match {
            case "country" => map += ("country" -> (ac \ "short_name").as[String])
            case "locality" => map += ("locality" -> (ac \ "short_name").as[String])
            case _ => /* noop */
          }
        }
      }
      map += ("name" -> (results \ "name").as[String])
      map += ("utc_offset" -> (results \ "utc_offset").as[Int])

      map
    }
  }
}
