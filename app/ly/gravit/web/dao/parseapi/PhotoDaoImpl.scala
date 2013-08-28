package ly.gravit.web.dao.parseapi

import scala.concurrent.Await
import scala.concurrent.duration._
import play.api.Play
import play.api.Play.current
import ly.gravit.web.dao.PhotoDao
import ly.gravit.web.{ParseApi, Photo}

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
      "caption" -> photo.caption
    ))
    val res =  Await.result(req, WS_TIMEOUT seconds)

    if (res.status == 201) {
      return Option((res.json \ "objectId").as[String])
    }

    None
  }
}
