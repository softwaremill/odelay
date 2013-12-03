package deferred.netty

import deferred.{ Timer, Deferral }
import scala.concurrent.duration._
import org.jboss.netty.util.{
  HashedWheelTimer, Timeout,
  Timer => NTimer, TimerTask }

class NettyTimer(underlying: NTimer = new HashedWheelTimer)
  extends Timer {
  def apply[T](after: Duration, op: => T): Deferral =
    new Deferral {
      private val to = underlying.newTimeout(new TimerTask {
          def run(timeout: Timeout) = op
        }, after.length, after.unit)
      def cancel() = if (!to.isCancelled) to.cancel
    }

  def apply[T](after: Duration, period: Duration, op: => T): Deferral = {
    new Deferral {
      private var nextTimeout: Option[Deferral] = None
      private val to = underlying.newTimeout(new TimerTask {
        def run(timeout: Timeout) = try op finally {
          nextTimeout = Some(apply(0.seconds, period, op))
        }
      }, period.length, period.unit)
      def cancel() = if (!to.isCancelled) {
        to.cancel
        nextTimeout.foreach(_.cancel())
      }
    }
  }

  def stop(): Unit = underlying.stop()
}
