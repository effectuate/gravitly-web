package com.gravitly.web.auth

/**
 * Created with IntelliJ IDEA.
 * User: ginduc
 * Date: 8/16/13
 * Time: 1:14 PM
 * To change this template use File | Settings | File Templates.
 */
case class Account(id: Int, email: String, password: String, name: String, permission: Permission)

object Account {

  def findById(id: String): Option[Account] = {
    Option(Account(1, "ned@flanders.com", "password", "Ned Flanders", Administrator))
  }
}
