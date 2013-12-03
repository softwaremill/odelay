package odelay.jdk

import odelay.{ Timeout, Timer }
import scala.concurrent.duration.{ Duration }
import java.util.concurrent.{
  Future, RejectedExecutionHandler, ScheduledExecutorService,
  ScheduledFuture, ScheduledThreadPoolExecutor, ThreadFactory }

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

  def apply[T](delay: Duration, op: => T): Timeout =
    new Timeout {
      val future = underlying.schedule(new Runnable {
        def run = op
      }, delay.length, delay.unit)
      def cancel() = if (!future.isCancelled) future.cancel(interruptOnCancel)
    }

  def apply[T](delay: Duration, every: Duration, op: => T): Timeout =
    new Timeout {
      val future = underlying.scheduleWithFixedDelay(new Runnable {
        def run = op
      }, delay.toUnit(every.unit).toLong, every.length, every.unit)
      def cancel() = if (!future.isCancelled) future.cancel(interruptOnCancel)
    }

  def stop() = if (!underlying.isShutdown) underlying.shutdownNow()
}


/** defaults for jdk timers */
object Default {
  lazy val poolSize = Runtime.getRuntime().availableProcessors()
  lazy val threadFactory: ThreadFactory = new ThreadFactory {
    def newThread(runnable: Runnable) = {
      new Thread(runnable) {
        setDaemon(true)
      }
    }
  }
  val rejectionHandler: Option[RejectedExecutionHandler] = None
  val interruptOnCancel = true
  /** @return a _new_ Timer. when used clients should be sure to call stop() on all instances for a clean shutdown */
  def timer: Timer = new JdkTimer(
    poolSize, threadFactory, rejectionHandler, interruptOnCancel)
}