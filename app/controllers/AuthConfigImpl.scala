package controllers

import ly.gravit.web.auth._
import play.api.mvc._
import play.api.mvc.Results._
import play.api.Play._
import scala.reflect.{ClassTag, classTag}
import jp.t2v.lab.play2.auth.AuthConfig

/**
 * Created with IntelliJ IDEA.
 * User: ginduc
 * Date: 8/16/13
 * Time: 12:59 PM
 * To change this template use File | Settings | File Templates.
 */

/*
  https://github.com/t2v/play2-auth#usage
 */
trait AuthConfigImpl extends AuthConfig {

  /**
   * A type that is used to identify a user.
   * `String`, `Int`, `Long` and so on.
   */
  type Id = String

  /**
   * A type that represents a user in your application.
   * `User`, `Account` and so on.
   */
  type User = Account

  /**
   * A type that is defined by every action for authorization.
   * This sample uses the following trait:
   *
   * sealed trait Permission
   * case object Administrator extends Permission
   * case object NormalUser extends Permission
   */
  type Authority = Permission

  /**
   * A `ClassManifest` is used to retrieve an id from the Cache API.
   * Use something like this:
   */
  val idTag: ClassTag[Id] = classTag[Id]

  /**
   * The session timeout in seconds
   */
  val sessionTimeoutInSeconds: Int = 3600

  /**
   * A function that returns a `User` object from an `Id`.
   * You can alter the procedure to suit your application.
   */
  def resolveUser(id: Id): Option[User] = Account.findById(id)

  /**
   * Where to redirect the user after a successful login.
   */
  def loginSucceeded(request: RequestHeader): Result = {
    val uri = request.session.get("access_uri").getOrElse(routes.Admin.index.url.toString)
    Redirect(uri).withSession(request.session - "access_uri")
  }

  /**
   * Where to redirect the user after logging out
   */
  def logoutSucceeded(request: RequestHeader): Result = Redirect(routes.Application.index)

  /**
   * If the user is not logged in and tries to access a protected resource then redirect them as follows:
   */
  def authenticationFailed(request: RequestHeader): Result =
    Redirect(routes.Application.login).withSession("access_uri" -> request.uri)

  /**
   * If authorization failed (usually incorrect password) redirect the user as follows:
   */
  def authorizationFailed(request: RequestHeader): Result = Forbidden("no permission")

  /**
   * A function that determines what `Authority` a user has.
   * You should alter this procedure to suit your application.
   */
  def authorize(user: User, authority: Authority): Boolean =
    (user.permission, authority) match {
      case (Administrator, _) => true
      case (NormalUser, NormalUser) => true
      case _ => false
    }

  /**
   * Whether use the secure option or not use it in the cookie.
   * However default is false, I strongly recommend using true in a production.
   */
  override lazy val cookieSecureOption: Boolean = play.api.Play.current.configuration.getBoolean("auth.cookie.secure").getOrElse(true)

}
