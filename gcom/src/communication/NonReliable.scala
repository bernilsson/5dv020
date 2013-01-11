package gcom.communication;

import gcom.common._;
import gcom.transport.Transport;

class NonReliable(t : Transport, callbck : Message => Unit)
                 extends Communication(t, callbck) {
  def sendToAll(dsts : List[NodeID], payload: String, ordering: MessageOrdering) : List[NodeID] = {
    var retList = List[NodeID]()
    dsts.foreach { dst =>
      val mid = transport.sendMessage(dst, Message(Unreliable(),ordering,payload))
      retList = mid.map({ id => id :: retList}).getOrElse(retList)
    }
    return retList
  }

  def receiveMessage(msg : Message) = callback(msg)
}

object NonReliable {
  def create(t : Transport, callbck : Message => Unit) : NonReliable = {
    val comm = new NonReliable(t, callbck)
    t.setOnReceive(comm.receiveMessage)
    return comm
  }
}
