package odelay.netty

class Netty3TimerSpec extends odelay.testing.TimerSpec {
  def newTimer = Default.newTimer
  def timerName = "Netty3Timer"
}
