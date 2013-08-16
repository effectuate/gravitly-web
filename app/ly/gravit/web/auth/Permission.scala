package ly.gravit.web.auth

/**
 * Created with IntelliJ IDEA.
 * User: ginduc
 * Date: 8/16/13
 * Time: 1:11 PM
 * To change this template use File | Settings | File Templates.
 */
sealed trait Permission
case object Administrator extends Permission
case object NormalUser extends Permission

object Permission {

  def valueOf(value: String): Permission = value match {
    case "Administrator" => Administrator
    case "NormalUser"    => NormalUser
    case _ => throw new IllegalArgumentException()
  }

}
