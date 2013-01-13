package gcom.group;

import gcom.NameServer
import gcom.common._
import gcom.communication.Communication
import gcom.ordering.Ordering
import gcom.transport.BasicTransport
import gcom.Communicator

class DummyGroup (nsrv : NameServer, ndID : NodeID, ldr : NodeID,
                  ord : Ordering, callbck : String => Unit)
      extends Communicator {
  val nameserver = nsrv
  val nodeID     = ndID
  var leader     = ldr
  var callback   = callbck

  def incCounter() : Int = { nsrv.incCounter() }
  def broadcastMessage(msg : String) : Unit = {
    val nodes = nsrv.listGroupMembers()
    ord.sendToAll(nodes, msg)
  }
  def listGroupMembers() : List[NodeID] = { nsrv.listGroupMembers() }
  // Not implemented with dummy group mngmnt.
  def leaveGroup() : Unit = { ; }
  def killGroup() : Unit = { ; }

  def setOnReceive(callbck : String => Unit) = { callback = callbck; }
  def receiveMessage(msg : Message) = {
    msg match {
      case Message(_, _, payload) => callback(payload)
    }
  }
}

object Group {
  def create(nsrv : NameServer, ndID: NodeID, leader : NodeID,
             comm : Communication, ord : Ordering,
             callbck : String => Unit) : Communicator = {
    val dummy = new DummyGroup(nsrv, ndID, leader, ord, callbck)
    comm.setHostCallback(dummy.listGroupMembers)
    ord.setOnReceive(dummy.receiveMessage)
    ord.setOrderCallback(dummy.incCounter)
    return dummy
  }
}
