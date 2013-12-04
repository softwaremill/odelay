package odelay.netty

class Netty3TimerSpec extends odelay.testing.TimerSpec {
  def newTimer = Default.timer
  def timerName = "Netty3Timer"
}
