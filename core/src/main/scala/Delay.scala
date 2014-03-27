package odelay

import scala.concurrent.{ Future, Promise }
import scala.concurrent.duration.{ Duration, FiniteDuration }
import java.util.concurrent.CancellationException

object Delay {  
  def apply[T](delay: FiniteDuration)(
    todo: => T)(implicit timer: Timer): Delay[T] =
    timer(delay, todo)

  def every[T](every: FiniteDuration)(delay: FiniteDuration = Duration.Zero)(
    todo: => T)(implicit timer: Timer): Delay[T] =
    timer(delay, every, todo)

  private [odelay] def cancel[T](p: Promise[T]) =
    if (!p.isCompleted) p.failure(new CancellationException)
}

/** The result of a deferred execution */
trait Delay[T] {
  /** @return a Future represent the execution of the Timeouts operation. Timeouts returned
   *          by repeated delays expose a future that will never complete until cancelled */
  def future: Future[T]

  /** Cancels the execution of the deferred operation. Once a Timeout
   *  is canceled, if additional attempts to cancel will result in undefined
   *  behavior */
  def cancel(): Unit
}

/** A base class for Delays which make Promises */
abstract class PromisingDelay[T] extends Delay[T] {
  private val promise = Promise[T]()

  /** Cancels the underlying promise if it's not already completed */
  protected def cancelPromise(): Unit =
    Delay.cancel(promise)

  /** Completes the Promise with a success if the promise is not already completed */
  protected def completePromise(value: => T): Unit =
    if (promiseIncomplete) promise.success(value)

  /** @return true if the promise is not completed */
  protected def promiseIncomplete =
    !promise.isCompleted

  /** @return a Future view of the timeouts Promise */
  def future: Future[T] = promise.future
}
