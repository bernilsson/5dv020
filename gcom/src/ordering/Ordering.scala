package gcom.ordering;

import gcom.common.Message
import gcom.communication.Communication
import gcom.common.NodeID
import gcom.common.MessageOrdering

abstract class Ordering (c: Communication, callbck : Message => Unit) {
  var callback = callbck
  val communicator = c;
  def setOnReceive(callbck : Message => Unit) = callback = callbck
  def createMessage(message: Message) : Message;
  def receiveMessage(msg : Message) : Unit
  def sendToAll(dst: List[NodeID],payload: String) : Unit = {
    communicator.sendToAll(dst, payload,createOrdering())
  }
  protected def createOrdering() : MessageOrdering
}
