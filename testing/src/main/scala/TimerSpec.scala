package odelay.testing

import odelay.{ Delay, Timer }
import org.scalatest.{ BeforeAndAfterAll, FunSpec }
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.control.NonFatal
import java.util.concurrent.CancellationException
import java.util.concurrent.atomic.AtomicInteger

trait TimerSpec extends FunSpec with BeforeAndAfterAll {
  
  def newTimer: Timer
  def timerName: String
  implicit val timer = newTimer

  describe (timerName) {
    it ("should execute an operation after an initial delay") {
      val start = System.currentTimeMillis
      val fut = Delay(1.seconds) {
        System.currentTimeMillis - start
      }.future
      val value = Await.result(fut, 2.seconds)
      value.millis.toSeconds === 1.seconds
    }

    it ("should permit cancellation of delayed operations") {
      val cancel = Delay(1.seconds)(sys.error("this should never print"))
      Await.result(Delay(150.millis) {
        cancel.cancel()
      }.future, 200.millis)
    }

    it ("cancellation of delayed operations should result in future failure") {
      val cancel = Delay(1.second)(sys.error("this should never print"))
      Delay(150.millis) {
        cancel.cancel()
      }
      cancel.future.onFailure {
        case NonFatal(e) => assert(e.getClass === classOf[CancellationException])
      }
    }

    it ("completion of delayed operations should result in a future success") {
      val future = Delay(1.second)(true).future
      future.onFailure {
        case NonFatal(_) => sys.error("this should never print")
      }
      future.onSuccess {
        case value => assert(value === true)
      }
    }

    it ("should repeatedly execute an operation on a fixed delay") {
      val start = System.currentTimeMillis
      val delay = Delay.every(150.millis)() {
        val diff = System.currentTimeMillis - start
        print('.')
        diff
      }
      delay.future.onFailure { case NonFatal(_) => println() }
      Await.ready(Delay(2.seconds) {
        delay.cancel()
      }.future, 3.seconds)
    }

    it ("cancellation of repeatedly delayed operations should result in future failure") {
      val cancel = Delay.every(150.seconds)()(true)
      val counter = new AtomicInteger(0)
      cancel.future.onFailure {
        case NonFatal(e) =>
          assert(e.getClass === classOf[CancellationException])
          counter.incrementAndGet()
      }
      Await.ready(Delay(2.seconds) {
        cancel.cancel()
      }.future, 3.seconds)
      Await.ready(cancel.future, 3.seconds)
      Thread.sleep(100)
      assert(counter.get() === 1)
    }
  }

  override def afterAll() {
    timer.stop()
  }
}
