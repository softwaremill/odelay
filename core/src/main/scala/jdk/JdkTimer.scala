package odelay.jdk

import odelay.{ PromisingDelay, Delay, Timer }
import scala.concurrent.Promise
import scala.concurrent.duration.{ Duration, FiniteDuration }
import java.util.concurrent.{
  RejectedExecutionHandler, ScheduledExecutorService,
  ScheduledThreadPoolExecutor, ThreadFactory }
import java.util.concurrent.atomic.AtomicInteger

/** A Timer implemented in terms of a jdk ScheduledThreadPoolExecutor */
class JdkTimer(
  underlying: ScheduledExecutorService,
  interruptOnCancel: Boolean)
  extends Timer {

  /** customizing constructor */
  def this(
    poolSize: Int = Default.poolSize,
    threads: ThreadFactory = Default.threadFactory,
    handler: Option[RejectedExecutionHandler] = Default.rejectionHandler,
    interruptOnCancel: Boolean = Default.interruptOnCancel) =
    this(handler.map( rejections => new ScheduledThreadPoolExecutor(poolSize, threads, rejections))
                .getOrElse(new ScheduledThreadPoolExecutor(poolSize, threads)),
         interruptOnCancel)

  def apply[T](delay: FiniteDuration, op: => T): Delay[T] =
    new PromisingDelay[T] {
      val jfuture = underlying.schedule(new Runnable {
        def run() = completePromise(op)
      }, delay.length, delay.unit)

      def cancel() = if (!jfuture.isCancelled) {
        jfuture.cancel(interruptOnCancel)
        cancelPromise()
      }
    }

  def apply[T](delay: FiniteDuration, every: FiniteDuration, op: => T): Delay[T] =
    new PromisingDelay[T] {
      val jfuture = underlying.scheduleWithFixedDelay(new Runnable {
        def run = if (promiseIncomplete) op
      }, delay.toUnit(every.unit).toLong, every.length, every.unit)

      def cancel() = if (!jfuture.isCancelled) {
        jfuture.cancel(interruptOnCancel)
        cancelPromise()
      }
    }

  def stop() = if (!underlying.isShutdown) underlying.shutdownNow()
}

/** defaults for jdk timers */
object Default {
  lazy val poolSize = Runtime.getRuntime().availableProcessors()
  lazy val threadFactory: ThreadFactory = new ThreadFactory {
    val grp = new ThreadGroup(
      Thread.currentThread().getThreadGroup(), "odelay")
    val threads = new AtomicInteger(1)
    def newThread(runs: Runnable) =
      new Thread(
        grp, runs,
        "odelay-%s" format threads.getAndIncrement()) {
        setDaemon(true)
      }
  }
  val rejectionHandler: Option[RejectedExecutionHandler] = None
  val interruptOnCancel = true
  /** @return a _new_ Timer. when used clients should be sure to call stop() on all instances for a clean shutdown */
  def timer: Timer = new JdkTimer(
    poolSize, threadFactory, rejectionHandler, interruptOnCancel)
}
