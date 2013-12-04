package odelay.jdk

class JdkTimerSpec extends odelay.testing.TimerSpec {
  def newTimer = Default.timer
  def timerName = "JdkTimer"
}
