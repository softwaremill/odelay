package odelay.testing

import odelay.{Delay, Timer}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funspec.AsyncFunSpec
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.control.NonFatal
import java.util.concurrent.CancellationException
import java.util.concurrent.atomic.AtomicInteger
import odelay.js._

class TimerSpec extends AsyncFunSpec with BeforeAndAfterAll {

  // needed so we do not get a scalatest EC error
  implicit override def executionContext =
    scala.concurrent.ExecutionContext.Implicits.global

  val newTimer: Timer = JsTimer.newTimer
  val timerName: String = "jstimer"
  implicit val timer: Timer = newTimer

  describe(timerName) {
    it("should execute an operation after an initial delay") {
      val start = System.currentTimeMillis
      val fut = Delay(1.seconds) {
        System.currentTimeMillis - start
      }.future
      fut.map(value => assert(value.millis.toSeconds.seconds === 1.seconds))
    }

    it("should permit cancellation of delayed operations") {
      val cancel = Delay(1.seconds)(sys.error("this should never print"))
      val fut = Delay(150.millis) {
        cancel.cancel()
      }.future
      fut.map(_ => succeed)
    }

    it("cancellation of delayed operations should result in future failure") {
      val cancel = Delay(1.second)(sys.error("this should never print"))
      Delay(150.millis) {
        cancel.cancel()
      }
      cancel.future.recover {
        case x: CancellationException => succeed
        case _                        => fail()
      }
    }

    it("completion of delayed operations should result in a future success") {
      val future = Delay(1.second)(true).future
      future
        .recover { case NonFatal(_) =>
          sys.error("this should never print")
        }
        .map(value => assert(value === true))
    }

    it("should repeatedly execute an operation on a fixed delay") {
      val start = System.currentTimeMillis
      val delay = Delay.every(150.millis)() {
        val diff = System.currentTimeMillis - start
        print('.')
        diff
      }
      delay.future.failed.foreach { _ => println() }
      val cancelit = Delay(5.seconds) {
        delay.cancel()
      }
      cancelit.future.map(_ => succeed)
    }

    it("cancellation of repeatedly delayed operations should result in future failure") {
      val cancel = Delay.every(150.seconds)()(true)
      val counter = new AtomicInteger(0)
      cancel.future.failed.foreach {
        case NonFatal(e) =>
          assert(e.getClass === classOf[CancellationException])
          counter.incrementAndGet()
        case _ =>
      }

      val canceltrueloop = Delay(2.seconds) {
        cancel.cancel()
      }
      canceltrueloop.future.map(_ => assert(counter.get() === 1))
    }

  }

  override def afterAll(): Unit = {
    timer.stop()
  }
}
