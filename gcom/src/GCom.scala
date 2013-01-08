package gcom;

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

case class Group (name : String
                 , multicast : MulticastType
                 , ordering : OrderingType)

trait NameServer {
  def listGroups() : List[Group]
  def joinGroup(g : Group, onReceive : String => Unit) : Communicator
  def killGroup(g: Group) : Boolean
}

trait Communicator {
  def broadcastMessage(msg : String)
  def leaveGroup()
}
