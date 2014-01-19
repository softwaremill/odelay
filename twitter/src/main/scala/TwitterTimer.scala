package odelay.twitter

import odelay.{ PromisingTimeout, Timeout, Timer }
import com.twitter.util.{ Duration, JavaTimer, Timer => TwttrTimer }
import scala.concurrent.Promise
import scala.concurrent.duration.FiniteDuration

case class TwitterTimer(underlying: TwttrTimer)
  extends Timer {

  def apply[T](delay: FiniteDuration, op: => T): Timeout[T] =
    new PromisingTimeout[T] {
      val tto = underlying.schedule(
        duration(delay).fromNow)(completePromise(op))
      def cancel() {
        tto.cancel()
        cancelPromise()
      }
    }

  def apply[T](
    delay: FiniteDuration, every: FiniteDuration, op: => T): Timeout[T] =
    new PromisingTimeout[T] {
      val tto = underlying.schedule(
        duration(delay).fromNow,
        duration(every))(op)
      def cancel() {
        tto.cancel()
        cancelPromise()
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
