package odelay.netty

import odelay.{ Timeout, Timer }
import org.jboss.netty.util.{
  HashedWheelTimer, Timeout => NTimeout, Timer => NTimer, TimerTask }
import scala.concurrent.duration._

class NettyTimer(underlying: NTimer = new HashedWheelTimer)
  extends Timer {
  def apply[T](after: Duration, op: => T): Timeout =
    new Timeout {
      private val to = underlying.newTimeout(new TimerTask {
          def run(timeout: NTimeout) = op
        }, after.length, after.unit)
      def cancel() = if (!to.isCancelled) to.cancel
    }

  def apply[T](delay: Duration, every: Duration, op: => T): Timeout =
    new Timeout {
      @volatile var nextTimeout: Option[Timeout] = None
      val to = underlying.newTimeout(new TimerTask {
        def run(timeout: NTimeout) = try op finally {
          nextTimeout = Some(apply(every, every, op))
        }
      }, delay.length, delay.unit)
      def cancel() = if (!to.isCancelled) {
        to.cancel
        nextTimeout.foreach(_.cancel())
      }
    }

  def stop(): Unit = underlying.stop()
}