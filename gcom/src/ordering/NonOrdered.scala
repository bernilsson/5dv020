package gcom.ordering;

import gcom.common.Message;
import gcom.communication.Communication;
import gcom.transport.Transport;

class NonOrdered(communicator : Communication, callbck : Message => Unit) extends Ordering(communicator, callbck) {
  def receiveMessage(msg : Message) {
    callback(msg)
  }
  def createMessage(m: Message) = m
}

object NonOrdered {
  def create(t : Communication, callbck : Message => Unit) : NonOrdered = {
    val ord = new NonOrdered(t, callbck)
    t.setOnReceive(ord.receiveMessage)
    return ord
  }
}
