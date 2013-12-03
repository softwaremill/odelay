package odelay.twitter

import odelay.{ Timeout, Timer }
import com.twitter.util.{ Duration, Timer => TwttrTimer, TimerTask }
import scala.concurrent.duration.{ Duration => StdDuration }

case class TwitterTimer(underlying: TwttrTimer) extends Timer {
  def apply[T](delay: StdDuration, op: => T): Timeout =
    new Timeout {
      val tto = underlying.schedule(
        Duration.fromTimeUnit(delay.length, delay.unit))(op)
      def cancel() = tto.cancel()
    }
  def apply[T](delay: StdDuration, every: StdDuration, op: => T): Timeout =
    new Timeout {
      val tto = underlying.schedule(
        Duration.fromTimeUnit(delay.length, delay.unit).fromNow,
        Duration.fromTimeUnit(every.length, every.unit))(op)
      def cancel() = tto.cancel()
    }
  def stop(): Unit = underlying.stop()
}
