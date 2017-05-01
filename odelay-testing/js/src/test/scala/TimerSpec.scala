package odelay.testing

import odelay.{ Delay, Timer }
import org.scalatest.{ BeforeAndAfterAll, AsyncFunSpec, Matchers }
import scala.concurrent._
import scala.concurrent.duration._
import scala.util.control.NonFatal
import java.util.concurrent.CancellationException
import java.util.concurrent.atomic.AtomicInteger

class TimerSpecJS extends TimerSpec {
  def newTimer = new odelay.js.JsTimer()
  def timerName: String = "jstimer"
}
