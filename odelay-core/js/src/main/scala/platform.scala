package odelay

private[odelay] object platform {
  implicit val defaultTimer: Timer = odelay.js.JsTimer.newTimer 
}
