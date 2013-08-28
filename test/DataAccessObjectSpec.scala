package test

import java.util.UUID
import play.api.test.WithApplication
import ly.gravit.web.dao.parseapi.AccountDaoImpl
import org.specs2.mutable.Specification

/**
 * Created with IntelliJ IDEA.
 * User: ginduc
 * Date: 8/19/13
 * Time: 11:39 AM
 * To change this template use File | Settings | File Templates.
 */
class DataAccessObjectSpec extends Specification {
  "AccountDao" should {
    var testId: String = null
    val testEmail = UUID.randomUUID().toString + "@test.com"

    "return a valid Account for successful authentication" in new WithApplication {
      val account = AccountDaoImpl.authenticate("admin@gravit.ly","password")
      account must_!= null
      account getOrElse failure("A valid Account is required for valid credentials")

      testId = account.get.id
      testId must_!= null
    }

    "return null for failed authentication" in new WithApplication {
      val account = AccountDaoImpl.authenticate("fakey@mcfaker.com","my_fake_password")
      account getOrElse success
    }

    "return a valid Account for getById() with valid id" in new WithApplication {
      val account = AccountDaoImpl.getById(testId)
      account must_!= null
      account getOrElse failure("A valid Account is required for valid ids")
    }

    "return null for getById() with bad id" in new WithApplication {
      val account = AccountDaoImpl.getById(UUID.randomUUID().toString)
      account getOrElse success
    }
  }
}
