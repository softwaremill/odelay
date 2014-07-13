package odelay.jdk

class JdkTimerSpec extends odelay.testing.TimerSpec {
  def newTimer = JdkTimer.newTimer
  def timerName = "JdkTimer"
}
