package odelay

import scala.concurrent.duration.Duration

object Delay {  
  def apply[T](delay: Duration)(
    todo: => T)(implicit timer: Timer): Timeout[T] =
    timer(delay, todo)

  def repeatedly[T](delay: Duration)(every: Duration)(
    todo: => T)(implicit timer: Timer): Timeout[T] =
    timer(delay, every, todo)
}
