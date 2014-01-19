package odelay

import scala.concurrent.{ Future, Promise }
import java.util.concurrent.CancellationException

private [odelay] object Timeout {
  private [odelay] def cancel[T](p: Promise[T]) =
    if (!p.isCompleted) p.failure(new CancellationException)
}

/** The result of a deferred execution */
trait Timeout[T] {
  /** @return a Future represent the execution of the Timeouts operation. Timeouts returned
   *          by repeated delays expose a future that will never complete until cancelled */
  def future: Future[T]

  /** Cancels the execution of the deferred operation. Once a Timeout
   *  is canceled, if additional attempts to cancel will result in undefined
   *  behavior */
  def cancel(): Unit
}

/** A base class for Timeouts which make Promises */
abstract class PromisingTimeout[T] extends Timeout[T] {
  private val promise = Promise[T]()

  /** Cancels the underlying promise if it's not already completed */
  protected def cancelPromise(): Unit =
    Timeout.cancel(promise)

  /** Completes the Promise with a success if the promise is not already completed */
  protected def completePromise(value: => T): Unit =
    if (promiseIncomplete) promise.success(value)

  /** @return true if the promise is not completed */
  protected def promiseIncomplete =
    !promise.isCompleted

  /** @return a Future view of the timeouts Promise */
  def future: Future[T] = promise.future
}
