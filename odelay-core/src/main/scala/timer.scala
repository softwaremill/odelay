package odelay

import scala.concurrent.duration.FiniteDuration
import scala.annotation.implicitNotFound

/** The deferrer of some arbitrary operation */
@implicitNotFound(
  "Cannot find an implicit odelay.Timer, either define one yourself or import odelay.Default.timer")
trait Timer {
  /** Delays the execution of an operation until the provided duration */
  def apply[T](delay: FiniteDuration, op: => T): Delay[T]
  /** Delays the execution of an operation until the provided deplay and then after, repeats the operation at the every duration after.
   *  Timeouts returned by this expose a Future that will never complete until cancelled */
  def apply[T](delay: FiniteDuration, every: FiniteDuration, todo: => T): PeriodicDelay[T]
  /** Stops the timer and releases any retained resources. Once a Timer is stoped, it's behavior is undefined. */
  def stop(): Unit
}

/** Defines default configurations for timers */
object Timer {
  implicit val default: Timer = jdk.JdkTimer.newTimer
}
