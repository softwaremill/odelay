package odelay.netty

import odelay.{ Timeout, Timer }
import io.netty.util.{
  HashedWheelTimer, Timeout => NTimeout, Timer => NTimer, TimerTask }
import scala.concurrent.Promise
import scala.concurrent.duration.FiniteDuration
import java.util.concurrent.{ ThreadFactory, TimeUnit }
import java.util.concurrent.atomic.AtomicInteger

import io.netty.util.concurrent.EventExecutorGroup

class NettyGroupTimer(
  grp: EventExecutorGroup,
  interruptOnCancel: Boolean = Default.interruptOnCancel)
  extends Timer {

  def apply[T](delay: FiniteDuration, op: => T): Timeout[T] =
    new Timeout[T] {
      val p = Promise[T]()
      val sf = grp.schedule(new Runnable {
        def run = if (!p.isCompleted) p.success(op)
      }, delay.length, delay.unit)

      def future = p.future

      def cancel() = if (!sf.isCancelled) {
        sf.cancel(interruptOnCancel)
        Timeout.cancel(p)
      }
    }

  def apply[T](
    delay: FiniteDuration, every: FiniteDuration, op: => T): Timeout[T] =
    new Timeout[T] {
      val p = Promise[T]()
      val sf = grp.scheduleWithFixedDelay(new Runnable {
        def run = if (!p.isCompleted) op
      }, delay.toUnit(every.unit).toLong, every.length, every.unit)

      def future = p.future

      def cancel() = if (!sf.isCancelled) {
        sf.cancel(interruptOnCancel)
        Timeout.cancel(p)
      }
    }

  def stop() = if (!grp.isShuttingDown()) grp.shutdownGracefully()
}

class NettyTimer(underlying: NTimer = new HashedWheelTimer)
  extends Timer {

  def apply[T](delay: FiniteDuration, op: => T): Timeout[T] =
    new Timeout[T] {
      val p = Promise[T]()
      val to = underlying.newTimeout(new TimerTask {
        def run(timeout: NTimeout) =
          if (!p.isCompleted) p.success(op)
      }, delay.length, delay.unit)

      def future = p.future

      def cancel() = if (!to.isCancelled) {
        to.cancel()
        Timeout.cancel(p)
      }
    }

  def apply[T](
    delay: FiniteDuration, every: FiniteDuration, op: => T): Timeout[T] =
    new Timeout[T] {
      var nextTimeout: Option[Timeout[T]] = None
      val p = Promise[T]()
      val to = underlying.newTimeout(new TimerTask {
        def run(timeout: NTimeout) = loop()
      }, delay.length, delay.unit)

      def loop() =
        if (!p.isCompleted) {
          op
          nextTimeout = Some(apply(every, every, op))
        }

      def future = p.future

      def cancel() =
        if (!to.isCancelled) {
          synchronized {
            to.cancel()
            nextTimeout.foreach(_.cancel())
            Timeout.cancel(p)
          }
        }
    }

  def stop(): Unit = underlying.stop()
}

object Default {
  val interruptOnCancel = true
  def groupTimer(grp: EventExecutorGroup) =
    new NettyGroupTimer(grp)
  /** @return a new NettyTimer backed by a HashedWheelTimer */
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
