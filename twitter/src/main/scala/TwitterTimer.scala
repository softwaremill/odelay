package odelay.twitter

import odelay.{ Timeout, Timer }
import com.twitter.util.{ Duration, Timer => TwttrTimer, TimerTask }
import scala.concurrent.Promise
import scala.concurrent.duration.FiniteDuration

case class TwitterTimer(underlying: TwttrTimer) extends Timer {

  def apply[T](delay: FiniteDuration, op: => T): Timeout[T] =
    new Timeout[T] {
      val p = Promise[T]()
      val tto = underlying.schedule(
        Duration.fromTimeUnit(delay.length, delay.unit))(p.success(op))
      def future = p.future
      def cancel() {
        tto.cancel()
        Timeout.cancel(p)
      }
    }

  def apply[T](delay: FiniteDuration, every: FiniteDuration, op: => T): Timeout[T] =
    new Timeout[T] {
      val p = Promise[T]()
      val tto = underlying.schedule(
        Duration.fromTimeUnit(delay.length, delay.unit).fromNow,
        Duration.fromTimeUnit(every.length, every.unit))(op)
      def future = p.future
      def cancel() {
        tto.cancel()
        Timeout.cancel(p)
      }
    }

  def stop(): Unit = underlying.stop()
}
