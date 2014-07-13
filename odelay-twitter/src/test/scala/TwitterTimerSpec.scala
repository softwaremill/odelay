package odelay.twitter

class TwitterTimerSpec extends odelay.testing.TimerSpec {
  def newTimer = TwitterTimer.newTimer
  def timerName = "TwitterTimer"
}
