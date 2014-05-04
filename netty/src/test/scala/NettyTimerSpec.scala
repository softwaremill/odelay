package odelay.netty

class NettyTimerSpec extends odelay.testing.TimerSpec {
  def newTimer = Default.newTimer
  def timerName = "NettyTimer"
}
