package ly.gravit.web.dao

import ly.gravit.web.auth.Account

/**
 * Created with IntelliJ IDEA.
 * User: ginduc
 * Date: 8/19/13
 * Time: 11:35 AM
 * To change this template use File | Settings | File Templates.
 */
trait AccountDao {
  def authenticate(email: String, password: String): Option[Account]
  def getById(id: String): Option[Account]
  def create(account: Account): Option[String]
  def getByEmail(email: String): Option[Account];
}
