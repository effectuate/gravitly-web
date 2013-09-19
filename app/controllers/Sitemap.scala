package controllers

import play.api.mvc.{Action, Controller}

/**
 * Created with IntelliJ IDEA.
 * User: ginduc
 * Date: 8/16/13
 * Time: 1:51 PM
 * To change this template use File | Settings | File Templates.
 */
object SiteMap extends Controller {
   def index = Action {

     Ok(views.xml.sitemap())
   }
}
