package odelay

private[odelay] object platform {
  implicit val defaultTimer: Timer = jdk.JdkTimer.newTimer
}
