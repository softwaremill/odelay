package odelay

import scala.concurrent.{ Future, Promise }
import scala.concurrent.duration.{ Duration, FiniteDuration }
import java.util.concurrent.CancellationException

import scala.util.control.NonFatal

/**
 * Provides an interface for producing Delays. Use
 * requires an implicit [[odelay.Timer]] to be in implicit scope.
 * {{{
 * val delay = odelay.Delay(2.seconds) {
 *   todo
 * }
 * }}}
 */
object Delay {
  /** @return a one-off Delay which may be cancelled */
  def apply[T]
    (delay: FiniteDuration)
    (todo: => T)
    (implicit timer: Timer): Delay[T] =
     timer(delay, todo)

  /** @return a periodic Delay which may be cancelled */
  def every[T]
    (every: FiniteDuration)
    (delay: FiniteDuration = Duration.Zero)
    (todo: => T)
    (implicit timer: Timer): PeriodicDelay[T] =
     timer(delay, every, todo)

  private [odelay] def cancel[T](p: Promise[T]) =
    if (!p.isCompleted) p.failure(new CancellationException)
}

/** A Delay is the default of a deferred operation */
trait Delay[T] {
  /** @return a Future represent the execution of the Delays operation. Delays
   *          to be repeated expose a future that will never complete until cancelled */
  def future: Future[T]

  /** Cancels the execution of the delayed operation. Once a Delay
   *  is canceled, if additional attempts to cancel will result in undefined
   *  behavior */
  def cancel(): Unit
}

trait PeriodicDelay[T] extends Delay[T] {
  def period: FiniteDuration
}

/** If calling cancel on a Delay's implemention has no other effect
 *  than cancelling the underlying promise. Use this as a mix in.
 *  {{{
 *  val timer = new Timer {
 *    def apply(delay: FiniteDuration, op: => T) = new PromisingDelay[T] with SelfCancelation[T] {
 *      schedule(delay, completePromise(op))
 *    }
 *    ...
 *  }
 *  }}}
 */
trait SelfCancelation[T] { self: PromisingDelay[T] =>
  def cancel() = cancelPromise()
}

/**
 * A building block for writing your own [[odelay.Timer]].
 * Call `completePromise(_)` with the value of the result
 * of the operation. Call `cancelPromise()` to cancel it.
 * To query the current state of the promise, use `promiseIncomplete`
 */
abstract class PromisingDelay[T] extends Delay[T] {
  private val promise = Promise[T]()

  /** Cancels the underlying promise if it's not already completed */
  protected def cancelPromise(): Unit =
    Delay.cancel(promise)

  /** if it's not already completed */
  protected def failPromise(why: Throwable): Unit =
    if (promiseIncomplete) promise.failure(why)

  /** Completes the Promise with a success if the promise is not already completed */
  protected def completePromise(value: => T): Unit =
    if (promiseIncomplete) promise.success(value)

  /** @return true if the promise is not completed */
  protected def promiseIncomplete =
    !promise.isCompleted

  /** @return a Future view of the timeouts Promise */
  def future: Future[T] = promise.future
}

abstract class PeriodicPromisingDelay[T](val period: FiniteDuration) extends PromisingDelay[T] with PeriodicDelay[T]