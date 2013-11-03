package deferred.netty

import deferred.{ Timer, Deferral }
import scala.concurrent.duration.Duration
import org.jboss.netty.util.{
  HashedWheelTimer, Timeout,
  Timer => NTimer, TimerTask }

class NettyTimer(underlying: NTimer = new HashedWheelTimer)
  extends Timer {
  def apply[T](duration: Duration, op: => T): Deferral =
    new Deferral {
      private val (time, unit) = (duration.length, duration.unit)
      private val to = underlying.newTimeout(new TimerTask {
          def run(timeout: Timeout) = op
        }, time, unit)
      def cancel() = if (!to.isCancelled) to.cancel
    }
}
