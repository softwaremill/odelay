package deferred

import scala.concurrent.duration.Duration
import scala.annotation.implicitNotFound

/** The result of a deferred execution */
trait Deferral {
  /** Cancels the execution of the deferred operation. Once a Deferral
   *  is canceled, if additional attempts to cancel will result in undefined
   *  behavior */
  def cancel(): Unit
}

/** The deferrer of some operation */
@implicitNotFound(
  "Cannot find an implicit deferred.Timer, either define one yourself or import deferred.Defaults._")
trait Timer {
  /** Deferrs the execution of a task until the provided duration */
  def apply[T](after: Duration, todo: => T): Deferral
  /** Deferrs the execution of a task until the provided wait duration then repeats task at the every duration after */
  def apply[T](wait: Duration, every: Duration, todo: => T): Deferral
  /** Stops the timer and releases any retained resources */
  def stop(): Unit
}

object Timer {  
  def apply[T](duration: Duration)(
    todo: => T)(implicit timer: Timer): Deferral =
    timer(duration, todo)
}

/** Defines default configurations for timers */
object Default {
  implicit val timer: Timer = jdk.Default.timer
}
