package ly.gravit.web.auth

import scala.collection.mutable

/**
 * Created with IntelliJ IDEA.
 * User: ginduc
 * Date: 8/16/13
 * Time: 1:14 PM
 * To change this template use File | Settings | File Templates.
 */
case class Account(id: String, email: String, password: String, name: String, permission: Permission)

object Account {
  val default = Account("1", "ned@flanders.com", "password", "Ned Flanders", Administrator);

  def findById(id: String): Option[Account] = Option(default)
  def findByEmail(email: String): Option[Account] = Option(default)

  def authenticate(email: String, password: String): Option[Account] = {
    findByEmail(email) match {
      case Some(user) => {
         if (user.email.equals(email) && user.password.equals(password)) {
           Option(user)
         } else {
           None
         }
      }
      case None => None
    }
  }
}
