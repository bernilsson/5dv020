package gcom.ordering;

import gcom.common.Message;
import gcom.communication.Communication;
import gcom.transport.Transport;

class NonOrdered(callbck : Message => Unit) extends Ordering(callbck) {
  def receiveMessage(msg : Message) {
    callback(msg)
  }
}

object NonOrdered {
  def create(t : Communication, callbck : Message => Unit) : NonOrdered = {
    val ord = new NonOrdered(callbck)
    t.setOnReceive(ord.receiveMessage)
    return ord
  }
}
