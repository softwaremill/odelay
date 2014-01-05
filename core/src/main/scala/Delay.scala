package odelay

import scala.concurrent.duration.{ Duration, FiniteDuration }

object Delay {  
  def apply[T](delay: FiniteDuration)(
    todo: => T)(implicit timer: Timer): Timeout[T] =
    timer(delay, todo)

  def every[T](every: FiniteDuration)(delay: FiniteDuration = Duration.Zero)(
    todo: => T)(implicit timer: Timer): Timeout[T] =
    timer(delay, every, todo)
}
