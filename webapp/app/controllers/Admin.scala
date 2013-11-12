package controllers

import java.util.{Date, UUID}
import java.io.File
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps
import scala.concurrent.{Future, Await}
import scala.concurrent.duration._
import play.api.Play.current
import play.Logger
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.Play
import play.api.libs.ws.WS
import play.api.libs.json._
import views.html._
import ly.gravit.web.auth._
import ly.gravit.web._
import play.api.libs.json.Json._
import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.{Tag, Directory, Metadata}
import jp.t2v.lab.play2.auth.AuthElement
import play.api.libs.json.JsArray
import scala.Some
import ly.gravit.web.Photo
import play.api.libs.json.JsObject
import com.drew.metadata.exif.{ExifIFD0Directory, GpsDirectory, ExifSubIFDDirectory}
import com.drew.lang.GeoLocation
import play.api.libs.ws.WS.WSRequestHolder

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
      "location" -> nonEmptyText,
      "category" -> nonEmptyText,
      "isPrivate" -> boolean,
      "hashTags" -> list(nonEmptyText),
      "locationName" -> optional(text)
    )
  )

  //def postUpload = StackAction(AuthorityKey -> NormalUser) { implicit request =>
  def postUpload = Action { implicit request =>
    uploaderForm.bindFromRequest.fold(
      errors => {
        if(Logger.isDebugEnabled) {
          Logger.debug("### Upload Errors: " + errors)
        }
        Async{
          Details.getLocations.map{ loc =>
            Async{
              Details.getCategories.map{ cat =>
                BadRequest(admin.upload(errors,loc.toList,cat.toList))
              }
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
            upload(S3_PHOTOS, byteArray, filename, filePart.contentType.get)

            // Save Photo info on Parse
            val exif = exifData(filePart.ref.file)
            create(Photo(null, uploadForm._1, filename, "LsmI34VlUu"/*loggedIn.id*/, uploadForm._2,uploadForm._3, None,
              nonEmptyDouble(exif.get("latitude")),
              Option(exif.getOrElse("latitudeRef", null).asInstanceOf[String]),
              nonEmptyDouble(exif.get("longitude")),
              Option(exif.getOrElse("longitudeRef", null).asInstanceOf[String]),
              nonEmptyDouble(exif.get("altitude")),
              Option(exif.getOrElse("width", 0).asInstanceOf[Int]),
              Option(exif.getOrElse("height", 0).asInstanceOf[Int]),
              Option(uploadForm._4),
              exif.get("timestamp") match {
                case Some(ts) => Option(exif.get("timestamp").get.asInstanceOf[Date])
                case None => None
              },
              Option(uploadForm._5),
              uploadForm._6
            ))
          }
          case None => {
            if(Logger.isDebugEnabled) {
              Logger.debug("Can't find file to upload")
            }
          }
        }
        Redirect(routes.Admin.upload())
      }
    )
  }

  private def nonEmptyDouble(value: Option[Any]): Option[Double] = value match {
    case Some(v) => {
      if (v.isInstanceOf[Number]) {
        val d = v.asInstanceOf[Number].doubleValue()

        if (d > 0) {
          return Some(d)
        }
      }
      None
    }
    case _ => None
  }

  private def create(photo: Photo): Option[String] = {
    val req = parseApiConnect(CLASS_PHOTO)
      .withHeaders(PARSE_API_HEADER_CONTENT_TYPE -> CONTENT_TYPE_JSON)

    println("Parse Post: {%s}".format(photo.parseApiRequest))

    val res =  Await.result(req.post("{%s}".format(photo.parseApiRequest)), WS_TIMEOUT seconds)
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

  private def exifData(imageFile: File) = {
    var exifData = Map[String, Any]()
    val metadata: Metadata = ImageMetadataReader.readMetadata(imageFile)

    val ifd = metadata.getDirectory(classOf[ExifIFD0Directory])
    if (ifd != null && ifd.getDate(ExifIFD0Directory.TAG_DATETIME) != null) {
      exifData += ("timestamp" -> ifd.getDate(ExifIFD0Directory.TAG_DATETIME))
    }

    val subIfd = metadata.getDirectory(classOf[ExifSubIFDDirectory])
    if (subIfd != null) {
      exifData += ("width" -> subIfd.getInt(ExifSubIFDDirectory.TAG_EXIF_IMAGE_WIDTH))
      exifData += ("height" -> subIfd.getInt(ExifSubIFDDirectory.TAG_EXIF_IMAGE_HEIGHT))
    }

    val geo = metadata.getDirectory(classOf[GpsDirectory])
    if (geo != null) {
      val geoLocation: GeoLocation = geo.getGeoLocation()
      if (geoLocation != null) {
        exifData += ("latitude" -> geoLocation.getLatitude)
        exifData += ("latitudeRef" -> geo.getString(GpsDirectory.TAG_GPS_LATITUDE_REF))
        exifData += ("longitude" -> geoLocation.getLongitude)
        exifData += ("longitudeRef" -> geo.getString(GpsDirectory.TAG_GPS_LONGITUDE_REF))
        exifData += ("altitude" -> geo.getDouble(GpsDirectory.TAG_GPS_ALTITUDE))
      }
    }
    exifData
  }

  //val sierraAtTahoe = "CoQBdAAAANahspttjHfS875axpTChB9K17fFVW3beJ6l_4kTulu_eRbwAH1GzyGYL8KetHXcW-v1w66rLY3sUgd5Jpp0HrGTXoO7b7ad2zJCac8WjVJOAnUI9vuaZcaMu1fwyiGOfqzdnWL0kAb7A2rl0g7IZhShJfjSnyuy7q3FNoa3DWGvEhAzU1Ysyhf0HL_TD6Bd-PEJGhTjedH1ZjfDrBAOM4FI_sRw2xBGDg"
  //val pillarPoint = "CoQBcgAAAKzJpbZ_I_TTCg1ZybQQL-nZDzGhMLDEAq6WlGYKC_cwcAyH046g9zsmJJP2hdUnaHnYCJmAicX8jaJcGFwjda_fI2m8cWNbQXfuT6GFNW4-eVNL2tf6Ai95EhXszyI6rpKvsS_LyjeEqODAF_d458XN_0Ccu8fgvzPodl9e351eEhCK6Ig6sxyq4tMFdE8q62vaGhSsEnVc23t82zzg_dQPTiKy2Rzbfw"
  //val portillo = "411008"

  val categoryMap = Map(
    "PVVqIA0NRI" -> "All/Custom",
    "3YYJZFAp8B" -> "Flight",
    "u1A6tJR4B0" -> "General Weather",
    "bZWt3gbTxS" -> "River",
    "w75f8pnvDJ" -> "Snow",
    "rph0ovXefp" -> "Surf",
    "P0QlFETikE" -> "Trail",
    "uoabsxZmSB" -> "Wind"
  )

  def meta(category: String, gPlaceRef: String, ll: String) = Action {
    categoryMap(category) match {
      case "Surf" => {
        Async {
          for {
            gp <- getGooglePlace(gPlaceRef)
            wwo <- getWwo(ll)
            wwoMarine <- getWwoMarine(ll)
          } yield {
            val meta = Json.obj(
              "name" -> gp("name").as[String],
              "utc_offset" -> gp("utc_offset").as[Int],
              "country" -> gp("country").as[String],
              "locality" -> gp("locality").as[String],
              "temp_C" -> wwo("temp_C").as[String],
              "temp_F" -> wwo("temp_F").as[String],
              "cloudcover" -> wwoMarine("cloudcover").as[String],
              "humidity" -> wwoMarine("humidity").as[String],
              "precipMM" -> wwoMarine("precipMM").as[String],
              "pressure" -> wwoMarine("pressure").as[String],
              "sigHeight_m" -> wwoMarine("sigHeight_m").as[String],
              "swellDir" -> wwoMarine("swellDir").as[String],
              "swellHeight_m" -> wwoMarine("swellHeight_m").as[String],
              "swellPeriod_secs" -> wwoMarine("swellPeriod_secs").as[String],
              "visibility" -> wwoMarine("visibility").as[String],
              "weatherCode" -> wwoMarine("weatherCode").as[String],
              "winddir16Point" -> wwoMarine("winddir16Point").as[String],
              "winddirDegree" -> wwoMarine("winddirDegree").as[String],
              "windspeedKmph" -> wwoMarine("windspeedKmph").as[String],
              "windspeedMiles" -> wwoMarine("windspeedMiles").as[String]
            )
            Ok(Json.obj(categoryMap(category) -> meta))
          }
        }
      }
      case "General Weather" | "Trail" | "Wind" => {
        Async {
          for {
            gp <- getGooglePlace(gPlaceRef)
            wwo <- getWwo(ll)
          } yield {
            val meta = Json.obj(
              "name" -> gp("name").as[String],
              "utc_offset" -> gp("utc_offset").as[Int],
              "country" -> gp("country").as[String],
              "locality" -> gp("locality").as[String],
              "temp_C" -> wwo("temp_C").as[String],
              "temp_F" -> wwo("temp_F").as[String],
              "waterTemp_C" -> wwo("waterTemp_C").as[String],
              "waterTemp_F" -> wwo("waterTemp_F").as[String],
              "cloudcover" -> wwo("cloudcover").as[String],
              "humidity" -> wwo("humidity").as[String],
              "precipMM" -> wwo("precipMM").as[String],
              "pressure" -> wwo("pressure").as[String],
              "pressureMiles" -> wwo("pressureMiles").as[String],
              "visibility" -> wwo("visibility").as[String],
              "visibilityMiles" -> wwo("visibilityMiles").as[String],
              "weatherCode" -> wwo("weatherCode").as[String],
              "weatherDesc" -> wwo("weatherDesc").as[String],
              "winddir16Point" -> wwo("winddir16Point").as[String],
              "winddirDegree" -> wwo("winddirDegree").as[String],
              "windspeedKmph" -> wwo("windspeedKmph").as[String],
              "windspeedMeterSec" -> wwo("windspeedMeterSec").as[String],
              "windspeedKnots" -> wwo("windspeedKnots").as[String],
              "precipMM" -> wwo("precipMM").as[String],
              "precipInches" -> wwo("precipInches").as[String],
              "windspeedMiles" -> wwo("windspeedMiles").as[String]
            )
            Ok(Json.obj(categoryMap(category) -> meta))
          }
        }
      }
      case "Snow" => {
        Async{
          for {
            gp <- getGooglePlace(gPlaceRef)
            //sc <- getSnowCountry(portillo)
            wwo <- getWwo(ll)
          } yield {
            val meta = Json.obj(
              //"resortName" -> sc("resortName").as[String],
              "name" -> gp("name").as[String],
              "utc_offset" -> gp("utc_offset").as[Int],
              "country" -> gp("country").as[String],
              "locality" -> gp("locality").as[String],
              "temp_C" -> wwo("temp_C").as[String],
              "temp_F" -> wwo("temp_F").as[String]
            )
            Ok(Json.obj(categoryMap(category) -> meta))
          }
        }
      }
      case "Flight" => Ok
      case "River" => {
        Async{
          for {
            gp <- getGooglePlace(gPlaceRef)
            wwo <- getWwo(ll)
          } yield {
            val meta = Json.obj(
              //"resortName" -> sc("resortName").as[String],
              "name" -> gp("name").as[String],
              "utc_offset" -> gp("utc_offset").as[Int],
              "country" -> gp("country").as[String],
              "locality" -> gp("locality").as[String],
              "temp_C" -> wwo("temp_C").as[String],
              "temp_F" -> wwo("temp_F").as[String]
            )
            Ok(Json.obj(categoryMap(category) -> meta))
          }
        }
      }
      case "All/Custom" => Ok
      case _ => BadRequest("Unsupported Category.")
    }
  }

  lazy private val snowCountryUrl = Play.application.configuration.getString("meta.api.snowCountry.url").get
  lazy private val snowCountryKey = Play.application.configuration.getString("meta.api.snowCountry.key").get
  /*
    http://localhost:19001/meta/w75f8pnvDJ/38.999976,-120.080566
   */
  private def getSnowCountry(id : String) : Future[Map[String, JsValue]] = {
    Future {

      val url =  "%s?apiKey=%s&ids=%s&output=json".format(snowCountryUrl, snowCountryKey, id)

      if (Logger.isDebugEnabled) {
        Logger.debug("3rdParty: "  +url)
      }
      var map = Map[String, JsValue]()
      val req = WS.url(url).get
      val res = Await.result(req, 20 seconds)
      val json = res.json
      println("sno json: " + json)

      (json \ "items").as[List[JsObject]].map { i =>
        map += ("resortName" -> (i \ "resortName"))
      }

      map
    }
  }

  lazy private val wwoUrl = Play.application.configuration.getString("meta.api.wwo.url").get
  lazy private val wwoKey = Play.application.configuration.getString("meta.api.wwo.key").get

  private def getWwo(ll: String): Future[Map[String, JsValue]] = {
    Future {
      val wwoDs = Play.application.configuration.getString("meta.api.wwo.dataset").get.split(",")
      val url = "%s%s?q=%s&format=json&key=%s".format(wwoUrl, "/weather.ashx", ll, wwoKey)

      if (Logger.isDebugEnabled) {
        Logger.debug("3rdParty: "  +url)
      }

      var map = Map[String, JsValue]()
      val req = WS.url(url).get
      val res = Await.result(req, 20 seconds)

      val json = res.json
      //println("weather json: " + json)
      val weatherJson = (json \ "data" \ "current_condition").as[List[JsObject]].head

      wwoDs.map{ key =>
        if (key.equals("weatherDesc")) {
          map += (key -> (((weatherJson \ key).as[List[JsObject]].head) \ "value"))
        } else {
          map += (key -> (weatherJson \ key))
        }
      }

      map
    }
  }

  private def getWwoMarine(ll: String): Future[Map[String, JsValue]] = {
    Future {
      val wwoDs = Play.application.configuration.getString("meta.api.wwo.marine.dataset").get.split(",")
      val url = "%s%s?q=%s&format=json&key=%s".format(wwoUrl, "/marine.ashx", ll, wwoKey)

      if (Logger.isDebugEnabled) {
        Logger.debug("3rdParty: "  +url)
      }

      var map = Map[String, JsValue]()
      val req = WS.url(url).get
      val res = Await.result(req, 20 seconds)

      val json = res.json
      val weatherJson = (json \ "data" \ "weather").as[List[JsObject]].head
      val hourly = (weatherJson \ "hourly").as[List[JsObject]].head

      wwoDs.map{ key =>
        //println("map key : " + key)
        map += (key -> (hourly \ key))
      }

      map
    }
  }

  private def getGooglePlace(reference: String): Future[Map[String, JsValue]] = {
    Future {
      val gpUrl = Play.application.configuration.getString("meta.api.goglplaces.url").get
      val gpKey = Play.application.configuration.getString("meta.api.goglplaces.key").get
      val url = "%s?reference=%s&sensor=true&key=%s".format(gpUrl,reference,gpKey)

      if (Logger.isDebugEnabled) {
        Logger.debug("3rdParty: "  +url)
      }
      var map = Map[String, JsValue]()
      val req = WS.url(url).get
      val res = Await.result(req, 20 seconds)
      val json = res.json
      val results = (json \ "result")

      (results \ "address_components").as[List[JsObject]].map { ac =>
        (ac \ "types").as[JsArray].value.map { t =>
          t.as[String] match {
            case "country" => map += ("country" -> (ac \ "short_name"))
            case "locality" => map += ("locality" -> (ac \ "short_name"))
            case _ => /* noop */
          }
        }
      }
      map += ("name" -> (results \ "name"))
      map += ("utc_offset" -> (results \ "utc_offset"))

      map
    }
  }

  def environmentalData(category: String, ll: String) = Action {
    categoryMap(category) match {
      case "Surf" => {
        Async {
          for {
            wwo <- getWwo(ll)
            wwoMarine <- getWwoMarine(ll)
          } yield {
            val meta = Json.obj(
              "temp_C" -> wwo("temp_C").as[String],
              "temp_F" -> wwo("temp_F").as[String],
              //"cloudcover" -> wwoMarine("cloudcover").as[String],
              //"humidity" -> wwoMarine("humidity").as[String],
              //"precipMM" -> wwoMarine("precipMM").as[String],
              "pressure" -> wwoMarine("pressure").as[String],
              "sigHeight_m" -> wwoMarine("sigHeight_m").as[String],
              "swellDir" -> wwoMarine("swellDir").as[String],
              "swellHeight_m" -> wwoMarine("swellHeight_m").as[String],
              "swellPeriod_secs" -> wwoMarine("swellPeriod_secs").as[String],
              "visibility" -> wwoMarine("visibility").as[String],
              //"weatherCode" -> wwoMarine("weatherCode").as[String],
              "winddir16Point" -> wwoMarine("winddir16Point").as[String],
              //"winddirDegree" -> wwoMarine("winddirDegree").as[String],
              "windspeedKmph" -> wwoMarine("windspeedKmph").as[String],
              "waterTemp_C" -> wwoMarine("waterTemp_C").as[String],
              "waterTemp_F" -> wwoMarine("waterTemp_F").as[String],
              "windspeedMiles" -> wwoMarine("windspeedMiles").as[String]
            )
            Ok(Json.obj(categoryMap(category) -> meta))
          }
        }
      }
      case "General Weather" | "Trail" | "Wind" => {
        Async {
          for {
            wwo <- getWwo(ll)
          } yield {
            val meta = Json.obj(
              "temp_C" -> wwo("temp_C").as[String],
              "temp_F" -> wwo("temp_F").as[String],
              "cloudcover" -> wwo("cloudcover").as[String],
              "humidity" -> wwo("humidity").as[String],
              "precipMM" -> wwo("precipMM").as[String],
              //"precipInches" -> wwo("precipInches").as[String],
              "pressure" -> wwo("pressure").as[String],
              //"pressureInches" -> wwo("pressureInches").as[String],
              "visibility" -> wwo("visibility").as[String],
              //"visibilityMiles" -> wwo("visibilityMiles").as[String],
              "weatherCode" -> wwo("weatherCode").as[String],
              "weatherDesc" -> wwo("weatherDesc").as[String],
              "winddir16Point" -> wwo("winddir16Point").as[String],
              "winddirDegree" -> wwo("winddirDegree").as[String],
              "windspeedKmph" -> wwo("windspeedKmph").as[String],
              "weatherDesc" -> wwo("weatherDesc").as[String],
              //"windspeedMeterSec" -> wwo("windspeedMeterSec").as[String],
              //"windspeedKnots" -> wwo("windspeedKnots").as[String],
              "windspeedMiles" -> wwo("windspeedMiles").as[String]
            )
            Ok(Json.obj(categoryMap(category) -> meta))
          }
        }
      }
      case "Snow" => {
        Async{
          for {
            //sc <- getSnowCountry(portillo)
            wwo <- getWwo(ll)
          } yield {
            val meta = Json.obj(
              "temp_C" -> wwo("temp_C").as[String],
              "temp_F" -> wwo("temp_F").as[String]
            )
            Ok(Json.obj(categoryMap(category) -> meta))
          }
        }
      }
      case "Flight" => Ok
      case "River" => {
        Async{
          for {
            wwo <- getWwo(ll)
          } yield {
            val meta = Json.obj(
              "temp_C" -> wwo("temp_C").as[String],
              "temp_F" -> wwo("temp_F").as[String]
            )
            Ok(Json.obj(categoryMap(category) -> meta))
          }
        }
      }
      case "All/Custom" => Ok
      case _ => BadRequest("Unsupported Category.")
    }
  }
}
