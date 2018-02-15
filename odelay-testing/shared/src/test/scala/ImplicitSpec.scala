package odelay.testing

import odelay.{ Delay, Timer }
import org.scalatest.{ BeforeAndAfterAll, FunSpec, Matchers }
import scala.concurrent._
import scala.concurrent.duration._
import scala.util.control.NonFatal
import java.util.concurrent.CancellationException
import java.util.concurrent.atomic.AtomicInteger

class ImplicitSpec extends FunSpec with Matchers with BeforeAndAfterAll {

  implicit def ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  describe("implicit test") {
    it ("should find the implict") {
      import Timer._
      import scala.concurrent._
      val result: Timer = implicitly[Timer]
      result should not be (null)
    }
  }
}
