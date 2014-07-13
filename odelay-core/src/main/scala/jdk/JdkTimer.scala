package odelay.jdk

import java.util.concurrent.{
  Future => JFuture,
  RejectedExecutionHandler, ScheduledExecutorService,
  ScheduledThreadPoolExecutor, ThreadFactory }
import java.util.concurrent.atomic.AtomicInteger
import odelay.{ PromisingDelay, Delay, Timer }
import scala.concurrent.Promise
import scala.concurrent.duration.FiniteDuration
import scala.util.control.NonFatal

/** A Timer implemented in terms of a jdk ScheduledThreadPoolExecutor */
class JdkTimer(
  underlying: ScheduledExecutorService,
  interruptOnCancel: Boolean)
  extends Timer {

  /** customizing constructor */
  def this(
    poolSize: Int                             = JdkTimer.poolSize,
    threads: ThreadFactory                    = JdkTimer.threadFactory,
    handler: Option[RejectedExecutionHandler] = JdkTimer.rejectionHandler,
    interruptOnCancel: Boolean                = JdkTimer.interruptOnCancel) =
    this(handler.map( rejections => new ScheduledThreadPoolExecutor(poolSize, threads, rejections))
                .getOrElse(new ScheduledThreadPoolExecutor(poolSize, threads)),
         interruptOnCancel)

  def apply[T](delay: FiniteDuration, op: => T): Delay[T] =
    new PromisingDelay[T] {
      val jfuture: Option[JFuture[_]] = try {
        Some(underlying.schedule(new Runnable {
          def run() = completePromise(op)
        }, delay.length, delay.unit))
      } catch {
        case NonFatal(e) =>
          failPromise(e)
          None
      }

      def cancel() = jfuture.filterNot(_.isCancelled).foreach { f =>
        f.cancel(interruptOnCancel)
        cancelPromise()
      }
    }

  def apply[T](delay: FiniteDuration, every: FiniteDuration, op: => T): Delay[T] =
    new PromisingDelay[T] {
      val jfuture: Option[JFuture[_]] = try {
        Some(underlying.scheduleWithFixedDelay(new Runnable {
          def run = if (promiseIncomplete) op
        }, delay.toUnit(every.unit).toLong, every.length, every.unit))
      } catch {
        case NonFatal(e) =>
          failPromise(e)
          None
      }

      def cancel() = jfuture.filterNot(_.isCancelled).foreach { f =>
        f.cancel(interruptOnCancel)
        cancelPromise()
      }
    }

  def stop() = if (!underlying.isShutdown) underlying.shutdownNow()
}

/** defaults for jdk timers */
object JdkTimer {
  lazy val poolSize = Runtime.getRuntime().availableProcessors()
  /** @return a new ThreadFactory with that produces new threads named odelay-{threadNum} */
  def threadFactory: ThreadFactory = new ThreadFactory {
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
  def newTimer: Timer = new JdkTimer(
    poolSize, threadFactory, rejectionHandler, interruptOnCancel)
}
