package gcom.client

import gcom._
import gcom.common.NodeID

object Client {

  /* Option parsing. */
  import org.clapper.argot.ArgotParser
  import org.clapper.argot.ArgotConverters._
  import org.clapper.argot.ArgotUsageException

  val parser = new ArgotParser(
    "gcom-client",
    preUsage=Some("gcom-client 0.1")
  )

  val portopt = parser.option[Int](List("p", "port"), "n", "port number")

  val commopt = parser.option[MulticastType](
    List("c", "communication"),
    "[reliable|unreliable]", "communication type") { (s, opt) =>
    s match {
      case "reliable" => ReliableMulticast()
      case "unreliable" => UnreliableMulticast()
      case _ => parser.usage("bad value for the -c option")
    }
  }

  val ordopt = parser.option[OrderingType](
    List("o", "ordering"),
    "[unordered|fifo|causal|total|causaltotal]", "ordering type") { (s, opt) =>
    s match {
      case "unordered" => NoOrdering()
      case "fifo" => FIFOOrdering()
      case "causal" => CausalOrdering()
      case "total" => TotalOrdering()
      case "causaltotal" => CausalTotalOrdering()
      case _ => parser.usage("bad value for the -o option")
    }
  }

  val nameserveropt = parser.parameter[NodeID](
    "nameserver", "name:host:port", true) { (s, opt) => NodeID.fromString(s) }

  sealed abstract class CommandType;
  case class CommandList() extends CommandType;
  case class CommandJoin(groupName : String) extends CommandType;
  case class CommandKill(groupName : String) extends CommandType;

  val commandopt = parser.multiParameter[String](
    "command", "[list|kill|join] [GROUP]", true)

  /* Values of the parsed options. */
  /* gcom-client -p PORT -c RELIABILITY -o ORDERING NAMESERVER COMMAND */
  lazy val port = portopt.value.getOrElse(31337)
  lazy val comm = commopt.value.getOrElse(UnreliableMulticast())
  lazy val ordering = ordopt.value.getOrElse(NoOrdering())
  lazy val nameserver = nameserveropt.value.getOrElse(
    NodeID.fromString("nameserver:localhost:31337"))
  lazy val command = commandopt.value match {
    case "list" :: Nil => CommandList()
    case "kill" :: groupName :: Nil => CommandKill(groupName)
    case "join" :: groupName :: Nil => CommandJoin(groupName)
    case _ => parser.usage("no command or incorrect command")
  }

  def main(args: Array[String]) = {
    try {
      parser.parse(args)
      println("Port: " + port.toString)
      println("Multicast type: " + comm.toString)
      println("Ordering type: " + ordering.toString)
      println("Nameserver: " + nameserver.toString)
      println("Command: " + command.toString)
    }
    catch {
      case e: ArgotUsageException => println(e.message)
    }
  }
}
