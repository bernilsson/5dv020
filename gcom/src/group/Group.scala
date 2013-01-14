package gcom.group;

import org.slf4j.Logger

import gcom.NameServer
import gcom.Group
import gcom.common._
import gcom.communication.Communication
import gcom.ordering.Ordering
import gcom.transport.BasicTransport
import gcom.Communicator

// Dummy group: Stores all data on the name server.

class DummyGroup (grp : Group, lggr : Logger, nsrv : NameServer,
                  ndID : NodeID, ord : Ordering,
                  callbck : String => Unit, viewCllbck : Set[NodeID] => Unit)
      extends Communicator {
  val group        = grp
  val nameserver   = nsrv
  val ordering     = ord
  val nodeID       = ndID
  var callback     = callbck
  var viewCallback = viewCllbck
  val logger       = lggr

  def incCounter() : Int = { nsrv.incCounter() }
  def broadcastMessage(msg : String) : Unit = {
    val nodes = nsrv.listGroupMembers()
    ordering.sendToAll(nodes, msg)
  }
  def listGroupMembers() : Set[NodeID] = { nameserver.listGroupMembers() }
  // Not implemented with dummy group mngmnt.
  def leaveGroup() : Unit = {
    logger.debug("DummyGroup.leaveGroup: not implemented"); }
  def killGroup() : Unit = {
    logger.debug("DummyGroup.killGroup: not implemented"); }

  // Boilerplate.
  def setOnReceive(callbck : String => Unit) = { callback = callbck; }
  def receiveMessage(msg : Message) = {
    msg match {
      case Message(_, _, payload) => callback(payload)
    }
  }
}

object DummyGroup {
  def create(grp : Group, lggr : Logger, nsrv : NameServer, ndID: NodeID,
             comm : Communication, ord : Ordering,
             callbck : String => Unit, viewCallbck : Set[NodeID] => Unit)
              : Communicator = {
    nsrv.getOrSetGroupLeader(grp, ndID)
    val dummy = new DummyGroup(grp, lggr, nsrv, ndID, ord,
                               callbck, viewCallbck)
    comm.setHostCallback(dummy.listGroupMembers)
    ord.setOnReceive(dummy.receiveMessage)
    ord.setOrderCallback(dummy.incCounter)
    nsrv.joinGroup(ndID)
    return dummy
  }
}
