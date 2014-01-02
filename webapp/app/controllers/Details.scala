package controllers

import ly.gravit.web._
import play.api.libs.json.{JsValue, JsObject}
import jp.t2v.lab.play2.auth.AuthElement
import collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import ly.gravit.web.Location
import play.api.libs.json.JsObject

/**
 * Created with IntelliJ IDEA.
 * User: Clarence
 * Date: 8/31/13
 * Time: 10:22 PM
 * To change this template use File | Settings | File Templates.
 */
object Details extends BaseController
    with AuthElement
    with AuthConfigImpl
    with S3Connectivity
    with ParseApiConnectivity{

  def getLocations = {
    val query = parseApiConnect(CLASS_LOCATION)
      .withQueryString("include" -> "location")
    query.get.map { res =>
      val json = res.json
      val location =  ListBuffer[Location]()
      (json \ "results").as [Seq[JsObject]].map({loc =>
        location +=  convertToLocation(loc)
        // println("loc ----> "+location)
      })
      location
    }

  }
  def convertToLocation(j : JsValue) : Location = {
    new Location(
      (j \ "objectId").as[String],
      (j \ "name").as[String]
    )
  }

  def getCategories = {
    val query = parseApiConnect(CLASS_CATEGORY)
      .withQueryString("include" -> "category")
    query.get.map { res =>
      val json = res.json
      val category =  ListBuffer[Category]()
      (json \ "results").as [Seq[JsObject]].map({loc =>
        category +=  convertToCategory(loc)
      })
      category
    }

  }
  def convertToCategory(j : JsValue) : Category = {
    new Category(
      (j \ "objectId").as[String],
      (j \ "name").as[String]
    )
  }
}
