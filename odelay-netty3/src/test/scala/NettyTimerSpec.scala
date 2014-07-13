package odelay.netty

class Netty3TimerSpec extends odelay.testing.TimerSpec {
  def newTimer = NettyTimer.newTimer
  def timerName = "Netty3Timer"
}
