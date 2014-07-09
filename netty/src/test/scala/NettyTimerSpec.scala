package odelay.netty

class NettyTimerSpec extends odelay.testing.TimerSpec {
  def newTimer = NettyTimer.newTimer
  def timerName = "NettyTimer"
}
