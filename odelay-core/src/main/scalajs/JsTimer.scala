package odelay
package js

import scalajs.js.timers._
import scala.concurrent.duration.FiniteDuration

class JsTimer() extends Timer {
  /** Delays the execution of an operation until the provided duration */
  def apply[T](delay: FiniteDuration, op: => T): Delay[T] = {
    new PromisingDelay[T] {
      val clearable: SetTimeoutHandle = setTimeout(delay){
        completePromise(op)
      }

      def cancel(): Unit = { clearTimeout(clearable); cancelPromise() }
    }
  }

  /** Delays the execution of an operation until the provided deplay and then after, repeats the operation at the every duration after.
   *  Timeouts returned by this expose a Future that will never complete until cancelled */
  def apply[T](delay: FiniteDuration, every: FiniteDuration, todo: => T): PeriodicDelay[T] = {
    new PeriodicPromisingDelay[T](every) {
      var clearable: SetIntervalHandle = null
      val initclearable: SetTimeoutHandle = setTimeout(delay){
        clearTimeout(initclearable);
        clearable = setInterval(every){
          if(promiseIncomplete) todo
        }
      }

      def cancel(): Unit = {
        if(clearable != null) clearInterval(clearable)
        cancelPromise()
      }
    }
  }

  /** Stops the timer and releases any retained resources. Once a Timer is stoped, it's behavior is undefined. */
  def stop(): Unit = ()
}


object JsTimer {
  def newTimer: Timer = new JsTimer()
}
