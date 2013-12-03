package odelay

import scala.concurrent.duration.Duration
import scala.annotation.implicitNotFound

/** The result of a deferred execution */
trait Timeout {
  /** Cancels the execution of the deferred operation. Once a Timeout
   *  is canceled, if additional attempts to cancel will result in undefined
   *  behavior */
  def cancel(): Unit
}

/** The deferrer of some operation */
@implicitNotFound(
  "Cannot find an implicit odelay.Timer, either define one yourself or import odelay.Default._")
trait Timer {
  /** Delays the execution of an operation until the provided duration */
  def apply[T](delay: Duration, op: => T): Timeout
  /** Delays the execution of an operation until the provided deplay and then after, repeats the operation at the every duration after */
  def apply[T](delay: Duration, every: Duration, todo: => T): Timeout
  /** Stops the timer and releases any retained resources */
  def stop(): Unit
}

/** Defines default configurations for timers */
object Default {
  implicit val timer: Timer = jdk.Default.timer
}
