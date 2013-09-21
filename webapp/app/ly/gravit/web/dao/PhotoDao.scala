package ly.gravit.web.dao

import ly.gravit.web.Photo

/**
 * Created with IntelliJ IDEA.
 * User: ginduc
 * Date: 8/28/13
 * Time: 5:50 PM
 * To change this template use File | Settings | File Templates.
 */
trait PhotoDao {
  def create(photo: Photo): Option[String]
  def getById(id: String): Option[Photo]
}
