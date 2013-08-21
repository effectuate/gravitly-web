
import java.util.UUID
import ly.gravit.web.auth.Account
import ly.gravit.web.dao.parseapi.AccountDaoImpl
import org.specs2.mutable.Specification
import play.api.test.WithApplication

/**
 * Created with IntelliJ IDEA.
 * User: ginduc
 * Date: 8/19/13
 * Time: 11:39 AM
 * To change this template use File | Settings | File Templates.
 */
class DataAccessObjectSpec extends Specification {
  "AccountDao" should {
    "return valid Account for successful authentication" in new WithApplication {
      val account = AccountDaoImpl.authenticate("fakey@mcfaker.com","my_fake_password")
      account getOrElse failure("A valid Account is required for valid credentials")
    }
    "return null for failed authentication" in new WithApplication {
      val account = AccountDaoImpl.authenticate("fakey@mcfaker.com","my_fake_password")
      account getOrElse success
    }

    "return valid Account for getById() with valid id" in new WithApplication {
      val account = AccountDaoImpl.authenticate("fakey@mcfaker.com","my_fake_password")
      account getOrElse failure("A valid Account is required for valid ids")
    }
    "return null for getById() with bad id" in new WithApplication {
      val account = AccountDaoImpl.getById(UUID.randomUUID().toString)
      account getOrElse success
    }

    "return valid Account for create() with valid fields" in new WithApplication {
      val account = AccountDaoImpl.create(null)
      account getOrElse failure("An Account needs to be created for valid fields")
    }
  }
}
