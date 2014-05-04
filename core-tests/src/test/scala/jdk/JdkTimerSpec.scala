package odelay.jdk

class JdkTimerSpec extends odelay.testing.TimerSpec {
  def newTimer = Default.newTimer
  def timerName = "JdkTimer"
}
