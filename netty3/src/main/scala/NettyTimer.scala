package odelay.netty

import odelay.{ Timeout, Timer }
import org.jboss.netty.util.{
  HashedWheelTimer, Timeout => NTimeout, Timer => NTimer, TimerTask }
import scala.concurrent.Promise
import scala.concurrent.duration.FiniteDuration
import java.util.concurrent.ThreadFactory

class NettyTimer(underlying: NTimer = new HashedWheelTimer)
  extends Timer {
  def apply[T](after: FiniteDuration, op: => T): Timeout[T] =
    new Timeout[T] {
      val p = Promise[T]()
      private val to = underlying.newTimeout(new TimerTask {
          def run(timeout: NTimeout) = p.success(op)
        }, after.length, after.unit)
      def future = p.future
      def cancel() = if (!to.isCancelled) {
        to.cancel()
        Timeout.cancel(p)
      }
    }

  def apply[T](delay: FiniteDuration, every: FiniteDuration, op: => T): Timeout[T] =
    new Timeout[T] {
      val p = Promise[T]()
      @volatile var nextTimeout: Option[Timeout[T]] = None
      val to = underlying.newTimeout(new TimerTask {
        def run(timeout: NTimeout) = try op finally {
          nextTimeout = Some(apply(every, every, op))
        }
      }, delay.length, delay.unit)
      def future = p.future
      def cancel() = if (!to.isCancelled) {
        to.cancel()
        nextTimeout.foreach(_.cancel())
        Timeout.cancel(p)
      }
    }

  def stop(): Unit = underlying.stop()
}

object Default {
  def timer: Timer = new NettyTimer(new HashedWheelTimer(new ThreadFactory {
    def newThread(runs: Runnable) =
      new Thread(runs) {
        setDaemon(true)
      }
  }))
}
