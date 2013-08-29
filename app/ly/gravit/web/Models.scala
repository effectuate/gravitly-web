package ly.gravit.web

case class Photo(id: String, caption: String, filename: String, userId: String, locationId: String, categoryId: String)
case class Location(id: String, name: String)
case class Category(id: String, name: String)
