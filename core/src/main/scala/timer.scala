package odelay

import scala.concurrent.{ Future, Promise }
import scala.concurrent.duration.FiniteDuration
import scala.annotation.implicitNotFound
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

/** The deferrer of some arbitrary operation */
@implicitNotFound(
  "Cannot find an implicit odelay.Timer, either define one yourself or import odelay.Default.timer")
trait Timer {
  /** Delays the execution of an operation until the provided duration */
  def apply[T](delay: FiniteDuration, op: => T): Timeout[T]
  /** Delays the execution of an operation until the provided deplay and then after, repeats the operation at the every duration after.
   *  Timeouts returned by this expose a Future that will never complete until cancelled */
  def apply[T](delay: FiniteDuration, every: FiniteDuration, todo: => T): Timeout[T]
  /** Stops the timer and releases any retained resources. Once a Timer is stoped, it's behavior is undefined. */
  def stop(): Unit
}

/** Defines default configurations for timers */
object Default {
  implicit val timer: Timer = jdk.Default.timer
}
