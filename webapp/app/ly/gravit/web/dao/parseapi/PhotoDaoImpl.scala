package ly.gravit.web.dao.parseapi

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps
import scala.concurrent.{Future, Await}
import scala.concurrent.duration._
import play.api.Play
import play.api.Play.current
import ly.gravit.web.dao.PhotoDao
import ly.gravit.web.{ParseApi, Photo}
import play.Logger
import java.util.Date

/**
 * Created with IntelliJ IDEA.
 * User: ginduc
 * Date: 8/28/13
 * Time: 5:50 PM
 * To change this template use File | Settings | File Templates.
 */
object PhotoDaoImpl extends PhotoDao {
  private lazy val PARSE_PHOTO  = "Photo"
  private lazy val WS_TIMEOUT = Play.application.configuration.getInt("gravitly.ws.timeout").getOrElse(30)

  override def create(photo: Photo): Option[String] = {
    val req = ParseApi.create(PARSE_PHOTO, Map(
      "caption" -> photo.caption,
      "filename" -> photo.filename,
      "user" -> photo.userId,
      "location" -> photo.locationId
    ))

    val res =  Await.result(req, WS_TIMEOUT seconds)
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

  override def getById(id: String): Option[Photo] = {
    var opt: Option[Photo] = None
    val req = ParseApi.get(PARSE_PHOTO, id)
    val res = Await.result(req, WS_TIMEOUT seconds)

    if(Logger.isDebugEnabled) {
      Logger.debug("GET /photos/%s [%s]".format(id, res.status))
    }

    if (res.status == 200) {
      val json = res.json
      opt = Option(Photo(
        Option((json \ "objectId").as[String]),
        (json \ "caption").as[String],
        (json \ "filename").as[String],
        (json \ "user").as[String],
        (json \ "location").as[String],
        (json \ "category").as[String],
        Option((json \ "createdAt").as[Date]),
        None,None,None,None,None,None,None
      ))
    }
    opt
  }
}
