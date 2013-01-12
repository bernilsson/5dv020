package gcom;

import java.rmi.Remote
import java.rmi.RemoteException

import gcom.common.NodeID

/** Client interface for the GCom middleware. */

sealed abstract class MulticastType;
case class UnreliableMulticast() extends MulticastType;
case class ReliableMulticast() extends MulticastType;

sealed abstract class OrderingType;
case class NoOrdering() extends OrderingType;
case class FIFOOrdering() extends OrderingType;
case class CausalOrdering() extends OrderingType;
case class TotalOrdering() extends OrderingType;
case class CausalTotalOrdering() extends OrderingType;

sealed case class Group (name : String
                         , multicast : MulticastType
                         , ordering : OrderingType)

trait NameServer extends Remote {

  @throws(classOf[RemoteException])
  def listGroups() : List[Group]

  @throws(classOf[RemoteException])
  def setGroupLeader(g : Group, l : NodeID) : Unit

  @throws(classOf[RemoteException])
  def removeGroup(g: Group) : Boolean

  @throws(classOf[RemoteException])
  def joinGroup(n : NodeID) : List[NodeID]

  @throws(classOf[RemoteException])
  def incCounter() : Int
}

trait Communicator {
  def broadcastMessage(msg : String) : Unit
  def leaveGroup() : Unit
  def killGroup() : Unit
}
