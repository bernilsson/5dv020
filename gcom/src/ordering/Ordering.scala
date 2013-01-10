package gcom.ordering;

import gcom.common.Message;

abstract class Ordering (callbck : Message => Unit) {
  var callback = callbck

  def setOnReceive(callbck : Message => Unit) = callback = callbck
  def receiveMessage(msg : Message) : Unit
}
