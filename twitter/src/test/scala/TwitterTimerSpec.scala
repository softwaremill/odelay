package odelay.twitter

class TwitterTimerSpec extends odelay.testing.TimerSpec {
  def newTimer = Default.timer
  def timerName = "TwitterTimer"
}
