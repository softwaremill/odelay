package odelay.twitter

class TwitterTimerSpec extends odelay.testing.TimerSpec {
  def newTimer = Default.newTimer
  def timerName = "TwitterTimer"
}
