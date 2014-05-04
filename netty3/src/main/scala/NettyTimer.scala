package odelay.netty

import odelay.{ Delay, PromisingDelay, Timer }
import odelay.jdk.{ Default => JdkDefault }
import org.jboss.netty.util.{
  HashedWheelTimer, Timeout, Timer => NTimer, TimerTask }
import scala.concurrent.Promise
import scala.concurrent.duration.FiniteDuration
import java.util.concurrent.{ ThreadFactory, TimeUnit }
import java.util.concurrent.atomic.AtomicInteger

class NettyTimer(underlying: NTimer = new HashedWheelTimer)
  extends Timer {
  def apply[T](after: FiniteDuration, op: => T): Delay[T] =
    new PromisingDelay[T] {
      private val to = underlying.newTimeout(new TimerTask {
        def run(timeout: Timeout) = completePromise(op)
      }, after.length, after.unit)

      def cancel() = if (!to.isCancelled) {
        to.cancel()
        cancelPromise()
      }
    }

  def apply[T](
    delay: FiniteDuration, every: FiniteDuration, op: => T): Delay[T] =
    new PromisingDelay[T] {
      var nextDelay: Option[Delay[T]] = None
      val to = underlying.newTimeout(new TimerTask {
        def run(timeout: Timeout) = loop()
      }, delay.length, delay.unit)

      def loop() =
        if (promiseIncomplete) {
          op
          nextDelay = Some(apply(every, every, op))
        }

      def cancel() =
        if (!to.isCancelled) {
          synchronized {
            to.cancel()
            nextDelay.foreach(_.cancel())
            cancelPromise()
          }
        }
    }

  def stop(): Unit = underlying.stop()
}

object Default {
  def newTimer: Timer = new NettyTimer(
    new HashedWheelTimer(
      JdkDefault.threadFactory,
      10, TimeUnit.MILLISECONDS))
}
