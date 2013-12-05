package odelay.testing

import odelay.{ Delay, Timer }
import org.scalatest.{ BeforeAndAfterAll, FunSpec }
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.control.NonFatal
import java.util.concurrent.CancellationException

trait TimerSpec extends FunSpec with BeforeAndAfterAll {
  
  def newTimer: Timer
  def timerName: String
  implicit val timer = newTimer

  describe (timerName) {
    it ("should execute an operation after an initial delay") {
      val start = System.currentTimeMillis
      val fut = Delay(1.seconds) {
        val diff = System.currentTimeMillis - start
        println(s"delay once diff $diff")
        diff
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

    it ("canellation of deplayed operations should result in future failure") {
      val cancel = Delay(1.second)(sys.error("this should never print"))
      Delay(150.millis) {
        cancel.cancel()
      }
      cancel.future.onFailure {
        case NonFatal(e) => assert(e.getClass === classOf[CancellationException])
      }
    }


    it ("should repeatedly execute an operation on a fixed delay") {
      val start = System.currentTimeMillis
      val timeout = Delay.repeatedly(150.millis)() {
        val diff = System.currentTimeMillis - start
        println(s"delay repeatedly diff $diff")
        diff
      }
      Await.result(Delay(2.seconds) {
        timeout.cancel()
      }.future, 3.seconds)
    }
  }

  override def afterAll() {
    timer.stop
  }
}
