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

/** The message ADT. Lists all possible types of messages. */
sealed abstract class Message {
  var senders = List[NodeID]();

  def getSenders() : List[NodeID] = senders;
  def addSender(n : NodeID) : Unit = senders = n +: senders;
}
/** Used for testing. */
case class TestMessage(content : String) extends Message;
/** On receiving this, die immediately. */
case class BlackSpot() extends Message;






case class FIFOMessage(seq: Int,msg: Message) extends Message
