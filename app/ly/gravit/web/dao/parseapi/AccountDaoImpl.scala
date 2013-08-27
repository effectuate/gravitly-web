package ly.gravit.web.dao.parseapi

import scala.concurrent.ExecutionContext.Implicits.global
import ly.gravit.web.dao.AccountDao
import ly.gravit.web.auth.Account
import ly.gravit.web.ParseApi

/**
 * Created with IntelliJ IDEA.
 * User: ginduc
 * Date: 8/19/13
 * Time: 11:38 AM
 * To change this template use File | Settings | File Templates.
 */
object AccountDaoImpl extends AccountDao {

  override def authenticate(email: String, password: String) = {
    None
  }

  override def getById(id: String) = {
    None
  }
  // case class Account(id: String, email: String, password: String, name: String, permission: Permission)
  override def getByEmail(email: String): Option[Account] = {
      ParseApi.find("_User", Map("email" -> "admin@gravitly.com")).map { res =>
        if (res.status == 200) {
          Option(Account(
            (res.json \ "objectId").as[String],
            (res.json \ "email").as[String],
            null,
            (res.json \ "username").as[String],
            null
          ))
        } else {
          None
        }
      }
    None
  }

  override def create(account: Account): Option[String] = {
    None
  }
}
