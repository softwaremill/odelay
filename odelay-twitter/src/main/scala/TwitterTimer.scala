package odelay.twitter

import com.twitter.util.{Duration, JavaTimer, Timer => TwttrTimer}
import odelay.{Delay, PeriodicDelay, PeriodicPromisingDelay, PromisingDelay, Timer}
import scala.concurrent.Promise
import scala.concurrent.duration.FiniteDuration
import scala.util.control.NonFatal

case class TwitterTimer(underlying: TwttrTimer) extends Timer {

  def apply[T](delay: FiniteDuration, op: => T): Delay[T] =
    new PromisingDelay[T] {
      val tto =
        try {
          Some(underlying.schedule(duration(delay).fromNow)(completePromise(op)))
        } catch {
          case NonFatal(e) =>
            failPromise(e)
            None
        }

      def cancel() = tto.foreach { f =>
        f.cancel()
        cancelPromise()
      }
    }

  def apply[T](delay: FiniteDuration, every: FiniteDuration, op: => T): PeriodicDelay[T] =
    new PeriodicPromisingDelay[T](every) {
      val tto =
        try {
          Some(underlying.schedule(duration(delay).fromNow, duration(every))(op))
        } catch {
          case NonFatal(e) =>
            failPromise(e)
            None
        }

      def cancel() = tto.foreach { f =>
        f.cancel()
        cancelPromise()
      }
    }

  def stop(): Unit = underlying.stop()

  private def duration(fd: FiniteDuration) =
    Duration.fromNanoseconds(fd.toNanos)
}

object TwitterTimer {

  /** Default twitter timer backed by a com.twitter.util.JavaTimer */
  def newTimer: Timer = new TwitterTimer(new JavaTimer(true))
}
