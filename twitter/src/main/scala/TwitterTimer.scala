package odelay.twitter

import odelay.{ Timeout, Timer }
import com.twitter.util.{ Duration, JavaTimer, Timer => TwttrTimer, TimerTask }
import scala.concurrent.Promise
import scala.concurrent.duration.FiniteDuration

case class TwitterTimer(underlying: TwttrTimer) extends Timer {

  def apply[T](delay: FiniteDuration, op: => T): Timeout[T] =
    new Timeout[T] {
      val p = Promise[T]()
      val tto = underlying.schedule(
        duration(delay))(p.success(op))
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
        duration(delay).fromNow,
        duration(every))(op)
      def future = p.future
      def cancel() {
        tto.cancel()
        Timeout.cancel(p)
      }
    }

  def stop(): Unit = underlying.stop()

  private def duration(fd: FiniteDuration) =
    Duration.fromNanoseconds(fd.toNanos)
}

object Default {
  /** Default twitter timer backed by a com.twitter.util.JavaTimer */
  def timer: Timer = new TwitterTimer(new JavaTimer(true))
}
