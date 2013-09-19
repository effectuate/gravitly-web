package ly.gravit.web

import java.util.Date

case class Photo(id: String, caption: String, filename: String, userId: String, locationId: String,
    categoryId: String, dateCreated: Option[Date])
case class Location(id: String, name: String)
case class Category(id: String, name: String)
