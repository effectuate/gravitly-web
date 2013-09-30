package ly.gravit.web

import java.util.Date
import play.api.libs.json.JsValue

case class Photo(id: Option[String], caption: String, filename: String, userId: String, locationId: String,
    categoryId: String, dateCreated: Option[Date], latitude: Option[Double], latitudeRef: Option[String],
    longitude: Option[Double], longitudeRef: Option[String], altitude: Option[Double], width: Option[Int],
    height: Option[Int], isPrivate: Boolean) {

  def parseApiRequest = {
    val reqParams = new StringBuilder(512)

    reqParams.append(""""isPrivate":%s,""".format(this.isPrivate))
    reqParams.append(""""caption":"%s",""".format(this.caption))
    reqParams.append(""""filename":"%s",""".format(this.filename))
    reqParams.append(""""width":%s,""".format(this.width.getOrElse(0)))
    reqParams.append(""""height":%s,""".format(this.height.getOrElse(0)))

    this.latitude match {
      case Some(lat) => reqParams.append(""""latitude":%s,""".format(lat))
      case None => /**/
    }
    this.latitudeRef match {
      case Some(latRef) => reqParams.append(""""latitudeRef":"%s",""".format(latRef))
      case None => /**/
    }
    this.longitude match {
      case Some(long) => reqParams.append(""""longitude":%s,""".format(long))
      case None => /**/
    }
    this.longitudeRef match {
      case Some(longRef) => reqParams.append(""""longitudeRef":"%s",""".format(longRef))
      case None => /**/
    }
    this.altitude match {
      case Some(alt) => reqParams.append(""""altitude":%s,""".format(alt))
      case None => /**/
    }

    (this.latitude, this.longitude) match {
      case (Some(lat), Some(long)) => {
        reqParams.append(""""geoPoint":{"__type":"GeoPoint","latitude":%s,"longitude":%s},""".format(lat, long))
      }
      case _ => /* noop */
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
    (json \ "caption").as[String], (json \ "filename").as[String],
    (json \ "user" \ "objectId").as[String], (json \ "location" \ "objectId").as[String],
    (json \ "category" \ "objectId").as[String], (json \ "createdAt").asOpt[Date],
    (json \ "latitude").asOpt[Double],
    (json \ "latitudeRef").asOpt[String],
    (json \ "longitude").asOpt[Double],
    (json \ "longitudeRef").asOpt[String],
    (json \ "altitude").asOpt[Double],
    (json \ "width").asOpt[Int],
    (json \ "height").asOpt[Int],
    (json \ "isPrivate").as[Boolean]
  )
}

case class Location(id: String, name: String)
case class Category(id: String, name: String)
