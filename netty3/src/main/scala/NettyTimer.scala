package odelay.netty

import odelay.{ PromisingTimeout, Timeout, Timer }
import org.jboss.netty.util.{
  HashedWheelTimer, Timeout => NTimeout, Timer => NTimer, TimerTask }
import scala.concurrent.Promise
import scala.concurrent.duration.FiniteDuration
import java.util.concurrent.{ ThreadFactory, TimeUnit }
import java.util.concurrent.atomic.AtomicInteger

class NettyTimer(underlying: NTimer = new HashedWheelTimer)
  extends Timer {
  def apply[T](after: FiniteDuration, op: => T): Timeout[T] =
    new PromisingTimeout[T] {
      private val to = underlying.newTimeout(new TimerTask {
        def run(timeout: NTimeout) = completePromise(op)
      }, after.length, after.unit)

      def cancel() = if (!to.isCancelled) {
        to.cancel()
        cancelPromise()
      }
    }

  def apply[T](
    delay: FiniteDuration, every: FiniteDuration, op: => T): Timeout[T] =
    new PromisingTimeout[T] {
      var nextTimeout: Option[Timeout[T]] = None
      val to = underlying.newTimeout(new TimerTask {
        def run(timeout: NTimeout) = loop()
      }, delay.length, delay.unit)

      def loop() =
        if (promiseIncomplete) {
          op
          nextTimeout = Some(apply(every, every, op))
        }

      def cancel() =
        if (!to.isCancelled) {
          synchronized {
            to.cancel()
            nextTimeout.foreach(_.cancel())
            cancelPromise()
          }
        }
    }

  def stop(): Unit = underlying.stop()
}

object Default {
  def timer: Timer = new NettyTimer(new HashedWheelTimer(new ThreadFactory {
    val grp = new ThreadGroup(
      Thread.currentThread().getThreadGroup(), "odelay")
    val threads = new AtomicInteger(1)
    def newThread(runs: Runnable) =
      new Thread(
        grp, runs,
        "odelay-%s" format threads.getAndIncrement()) {
          setDaemon(true)
        }
  }, 10, TimeUnit.MILLISECONDS))
}
