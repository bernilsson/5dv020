package gcom.ordering;

import gcom.common.Message
import gcom.communication.Communication
import gcom.common.NodeID

abstract class Ordering (c: Communication, callbck : Message => Unit) {
  var callback = callbck
  val communicator = c;
  def setOnReceive(callbck : Message => Unit) = callback = callbck
  def receiveMessage(msg : Message) : Unit
  def sendToAll(dst: List[NodeID],msg: Message) : Unit = {
    communicator.sendToAll(dst, createMessage(msg))
  }
  protected def createMessage(msg : Message) : Message
}
