package deferred

import scala.concurrent.duration.Duration
import scala.annotation.implicitNotFound

/** The result of a deferred execution */
trait Deferral {
  /** Cancels the execution of the deferred operation */
  def cancel(): Unit
}

/** The deferrer of some operation */
@implicitNotFound(
  "Cannot find an implicit deferred.Timer, either define one yourself or import deferred.Defaults._")
trait Timer {
  /** Deferrs the execution of a task until the provided duration */
  def apply[T](duration: Duration, todo: => T): Deferral
}

object Timer {  
  def apply[T](duration: Duration)(
    todo: => T)(implicit timer: Timer): Deferral =
    timer(duration, todo)
}

/** Defines default configurations for timers */
object Defaults {
  implicit val timer: Timer = jdk.Default.timer
}
