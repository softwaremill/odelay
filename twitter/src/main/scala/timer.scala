package deferred.twitter

import java.util.concurrent.TimeUnit
import com.twitter.util.{ Duration, Timer, TimerTask }
import scala.concurrent.duration.{ Duration => StdDuration }

case class TwitterTimer(underlying: Timer) extends deferred.Timer {
  def apply[T](duration: StdDuration, op: => Future[T]): deferred.Deferral =
    new deferred.Deferral {
      private val (time, unit) = (duration.length, duration.unit)
      val tto = underlying.schedule(Duration.fromTimeUnit(time, unit))(op)
      def cancel() = tto.cancel()
    }
  def apply[T](wait: StdDuration, period: StdDuration, todo: => T): Deferral =
    new deferred.Deferral {
      val tto = underlying.schedule(
        Duration.fromTimeUnit(wait.length, wait.unit).fromNow,
        Duration.fromTimeUnit(period.length, period.unit))(op)
      def cancel() = too.cancel()
    }
  def stop(): Unit = underlying.stop()
}
