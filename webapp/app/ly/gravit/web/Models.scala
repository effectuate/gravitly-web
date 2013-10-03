package ly.gravit.web

import java.util.Date
import play.api.libs.json.{JsObject, JsValue}
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat

case class Photo(id: Option[String], caption: String, filename: String, userId: String, locationId: String,
    categoryId: String, dateCreated: Option[Date], latitude: Option[Double], latitudeRef: Option[String],
    longitude: Option[Double], longitudeRef: Option[String], altitude: Option[Double], width: Option[Int],
    height: Option[Int], isPrivate: Boolean, timestamp: Date, hashTags: Option[Seq[String]]) {

  def parseApiRequest = {
    val reqParams = new StringBuilder(512)

    reqParams.append(""""isPrivate":%s,""".format(this.isPrivate))
    reqParams.append(""""caption":"%s",""".format(this.caption))
    reqParams.append(""""filename":"%s",""".format(this.filename))
    reqParams.append(""""width":%s,""".format(this.width.getOrElse(0)))
    reqParams.append(""""height":%s,""".format(this.height.getOrElse(0)))

    val dt = new DateTime(this.timestamp)
    val fmt = ISODateTimeFormat.dateTime
    reqParams.append(""""timestamp":{"__type": "Date", "iso":"%s"},""".format(fmt.print(dt)))

    (this.latitude, this.latitudeRef, this.longitude, this.longitudeRef) match {
      case (Some(lat), Some(latRef), Some(long), Some(longRef))=> {
        reqParams.append(""""latitude":%s,""".format(lat))
        reqParams.append(""""latitudeRef":"%s",""".format(latRef))
        reqParams.append(""""longitude":%s,""".format(long))
        reqParams.append(""""longitudeRef":"%s",""".format(longRef))
        reqParams.append(""""geoPoint":{"__type":"GeoPoint","latitude":%s, "longitude":%s},""".format(lat, long))
      }
      case _ => /*noop*/
    }

    this.altitude match {
      case Some(alt) => reqParams.append(""""altitude":%s,""".format(alt))
      case None => /**/
    }

    this.hashTags.map { tags =>
      val sb = new StringBuilder(512)
      tags.zipWithIndex foreach { case (tag, idx) =>
        if (idx > 0)
          sb.append(",")

        sb.append("\"%s\"".format(tag))
      }
      reqParams.append("""{"hashTags":{"__op":"AddUnique","objects":[%s]}},""".format(sb.toString))
    }

    reqParams.append(""""user":{"__type":"Pointer","className":"_User","objectId":"%s"},""".format(this.userId))
    reqParams.append(""""category":{"__type":"Pointer","className":"Category","objectId":"%s"},""".format(this.categoryId))
    reqParams.append(""""location":{"__type":"Pointer","className":"Location","objectId":"%s"}""".format(this.locationId))
    reqParams.toString
  }
}

object Photo {
  def fromJson(json: JsValue) = Photo(
    Option((json \ "objectId").as[String]),
    (json \ "caption").as[String],
    (json \ "filename").as[String],
    (json \ "user" \ "objectId").as[String],
    (json \ "location" \ "objectId").as[String],
    (json \ "category" \ "objectId").as[String],
    (json \ "createdAt").asOpt[Date],
    (json \ "latitude").asOpt[Double],
    (json \ "latitudeRef").asOpt[String],
    (json \ "longitude").asOpt[Double],
    (json \ "longitudeRef").asOpt[String],
    (json \ "altitude").asOpt[Double],
    (json \ "width").asOpt[Int],
    (json \ "height").asOpt[Int],
    (json \ "isPrivate").as[Boolean],
    (json \ "timestamp" \ "iso").as[Date],
    (json \ "hashTags").asOpt[List[JsObject]] match {
      case Some(hashTags) => Option(hashTags.map(_.toString))
      case _ => None
    }
  )
}

case class Location(id: String, name: String)
case class Category(id: String, name: String)
