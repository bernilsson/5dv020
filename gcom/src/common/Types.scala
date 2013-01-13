package gcom.common

/** The node ID is just a (name, host, port) triple. */
sealed case class NodeID (name : String, host: String, port: Int) {

  override def toString() : String = {
    return name + ":" + host + ":" + port.toString;
  }

  /* Since NodeID is a case class, we get correct implementations of
   * equals() and hashCode() for free. */
}

object NodeID {
  def fromString(s : String) : NodeID = {
    val arr = s.split(":");
    if (arr.length != 3)
      throw new IllegalArgumentException("NodeID.toString: can't parse")

    return new NodeID(arr(0), arr(1), arr(2).toInt)
  }
}

sealed abstract class OrderingData
case class NoOrderingData() extends OrderingData
case class CausalData(clock: Map[NodeID, Int]) extends OrderingData
case class FIFOData(seq: Int) extends OrderingData
case class TotalOrdData(order: Int) extends OrderingData

sealed abstract class ReliabilityData
case class NoReliabilityData() extends ReliabilityData
case class ReliableMsgData(seq: Int) extends ReliabilityData

sealed abstract class AbstractMessage extends Serializable {
  var senders = List[NodeID]();

  def getSenders() : List[NodeID] = senders;
  def addSender(n : NodeID) : Unit = senders = n +: senders;
}

case class Message(reliability : ReliabilityData,
                   ordering : OrderingData,
                   payload : String) extends AbstractMessage
/** On receiving this, die immediately. */
case class BlackSpot() extends AbstractMessage;

/** Used for testing. */
object TestMessage{
  def create(payload: String) = Message(NoReliabilityData(),
                                        NoOrderingData(), payload);
  def create(payload: String, ordering: OrderingData) =
    Message(NoReliabilityData(), ordering, payload);
}
