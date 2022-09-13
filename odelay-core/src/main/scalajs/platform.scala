package odelay

private[odelay] object platform {
  implicit val defaultTimer: Timer = js.JsTimer.newTimer
}
