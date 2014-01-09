package odelay.jdk

import odelay.{ Timeout, Timer }
import scala.concurrent.Promise
import scala.concurrent.duration.{ Duration, FiniteDuration }
import java.util.concurrent.{
  RejectedExecutionHandler, ScheduledExecutorService,
  ScheduledThreadPoolExecutor, ThreadFactory }

/** A Timer implemented in terms of a jdk ScheduledThreadPoolExecutor */
class JdkTimer(
  underlying: ScheduledExecutorService,
  interruptOnCancel: Boolean)
  extends Timer {

  /** customizing constructor */
  def this(poolSize: Int = Default.poolSize,
           threads: ThreadFactory = Default.threadFactory,
           handler: Option[RejectedExecutionHandler] = Default.rejectionHandler,
           interruptOnCancel: Boolean = Default.interruptOnCancel) =
    this(handler.map( rejections => new ScheduledThreadPoolExecutor(poolSize, threads, rejections))
                .getOrElse(new ScheduledThreadPoolExecutor(poolSize, threads)),
         interruptOnCancel)

  def apply[T](delay: FiniteDuration, op: => T): Timeout[T] =
    new Timeout[T] {
      val p = Promise[T]()
      val jfuture = underlying.schedule(new Runnable {
        def run() = if (!p.isCompleted) p.success(op)
      }, delay.length, delay.unit)
      def future = p.future
      def cancel() = if (!jfuture.isCancelled) {
        jfuture.cancel(interruptOnCancel)
        Timeout.cancel(p)
      }
    }

  def apply[T](delay: FiniteDuration, every: FiniteDuration, op: => T): Timeout[T] =
    new Timeout[T] {
      val p = Promise[T]()
      val jfuture = underlying.scheduleWithFixedDelay(new Runnable {
        def run = if (p.isCompleted) op
      }, delay.toUnit(every.unit).toLong, every.length, every.unit)
      def future = p.future
      def cancel() = if (!jfuture.isCancelled) {
        jfuture.cancel(interruptOnCancel)
        Timeout.cancel(p)
      }
    }

  def stop() = if (!underlying.isShutdown) underlying.shutdownNow()
}


/** defaults for jdk timers */
object Default {
  lazy val poolSize = Runtime.getRuntime().availableProcessors()
  lazy val threadFactory: ThreadFactory = new ThreadFactory {
    def newThread(runs: Runnable) =
      new Thread(runs) {
        setDaemon(true)
      }
  }
  val rejectionHandler: Option[RejectedExecutionHandler] = None
  val interruptOnCancel = true
  /** @return a _new_ Timer. when used clients should be sure to call stop() on all instances for a clean shutdown */
  def timer: Timer = new JdkTimer(
    poolSize, threadFactory, rejectionHandler, interruptOnCancel)
}
