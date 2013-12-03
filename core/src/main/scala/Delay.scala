package odelay

object Delay {  
  def apply[T](delay: Duration)(
    todo: => T)(implicit timer: Timer): Timeout =
    timer(delay, todo)

  def repeatedly[T](delay: Duration)(every: Duration)(
    todo: => T)(implicit timer: Timer): Timeout =
    timer(delay, every, todo)
}
