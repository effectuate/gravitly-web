package ly.gravit.web

import java.util.Date

case class Photo(id: Option[String], caption: String, filename: String, userId: String, locationId: String, categoryId: String,
    dateCreated: Option[Date], latitude: Option[Double], longitude: Option[Double], altitude: Option[Double],
    width: Option[Int], height: Option[Int])
case class Location(id: String, name: String)
case class Category(id: String, name: String)