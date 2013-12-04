package odelay.netty

class NettyTimerSpec extends odelay.testing.TimerSpec {
  def newTimer = Default.timer
  def timerName = "NettyTimer"
}
