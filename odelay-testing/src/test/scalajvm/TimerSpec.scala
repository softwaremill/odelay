package odelay.testing

import odelay.{Delay, Timer}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funspec.AsyncFunSpec
import scala.concurrent._
import scala.concurrent.duration._
import scala.util.control.NonFatal
import java.util.concurrent.CancellationException
import java.util.concurrent.atomic.AtomicInteger

trait TimerSpec extends AsyncFunSpec with BeforeAndAfterAll {

  implicit def ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  def newTimer: Timer
  def timerName: String
  implicit val timer: Timer = newTimer

  describe(timerName) {
    it("should execute an operation after an initial delay") {
      val start = System.currentTimeMillis
      val fut = Delay(1.seconds) {
        System.currentTimeMillis - start
      }.future
      fut.map(value => assert(value.millis.toSeconds === 1))
    }

    it("should permit cancellation of delayed operations") {
      val cancel = Delay(1.seconds)(sys.error("this should never print"))
      Delay(150.millis) {
        cancel.cancel()
      }.future.map(_ => succeed)
    }

    it("cancellation of delayed operations should result in future failure") {
      val cancel = Delay(1.second)(sys.error("this should never print"))
      val cancelF = Delay(150.millis) {
        cancel.cancel()
      }
      for {
        cf <- cancelF.future
        c <- cancel.future.failed
      } yield {
        assert(c.getClass === classOf[CancellationException])
      }
    }

    it("completion of delayed operations should result in a future success") {
      val future = Delay(1.second)(true).future
      future.map { value => assert(value === true) }
    }

    it("should repeatedly execute an operation on a fixed delay") {
      val start = System.currentTimeMillis
      val delay = Delay.every(150.millis)() {
        val diff = System.currentTimeMillis - start
        print('.')
        diff
      }
      delay.future.failed.foreach { _ => println() }
      Delay(2.seconds) {
        delay.cancel()
      }.future.map(_ => succeed)
    }

    it("cancellation of repeatedly delayed operations should result in future failure") {
      val counter = new AtomicInteger(0)
      val cancel = Delay.every(150.seconds)()(true)
      val cancelFut = cancel.future.recoverWith {
        case e: CancellationException =>
          counter.incrementAndGet()
          Future.successful(true)
        case _ => Future.successful(true)
      }
      val fut = Delay(2.seconds) {
        cancel.cancel()
      }

      cancelFut.map(_ => assert(counter.get() === 1))
    }

  }

  override def afterAll(): Unit = {
    timer.stop()
  }
}
