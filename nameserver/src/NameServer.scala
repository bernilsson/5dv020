package gcom.nameserver

import java.rmi.registry.Registry
import java.rmi.registry.LocateRegistry
import java.rmi.RemoteException
import java.rmi.server.UnicastRemoteObject

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import gcom._
import gcom.common.NodeID

object NameServer extends NameServer {

  val name = "nameserver"
  val logger = LoggerFactory.getLogger(name)

  /* Option parsing. */
  import org.clapper.argot.ArgotParser
  import org.clapper.argot.ArgotUsageException
  import org.clapper.argot.ArgotConverters._

  val parser = new ArgotParser(
    "gcom-nameserver",
    preUsage=Some("gcom-nameserver 0.1")
  )

  /* Option values. */
  val portopt = parser.option[Int](List("p", "port"), "n", "port number")
  lazy val port = portopt.value.getOrElse(31337)

  /* Entry point. */
  def main(args: Array[String]) : Unit = {
    try {
      parser.parse(args)
      logger.debug("Starting nameserver on port " + port.toString)

      if (System.getSecurityManager == null) {
        System.setSecurityManager(new SecurityManager)
      }
      val stub = UnicastRemoteObject.exportObject(this, 0)
      val registry = LocateRegistry.getRegistry(port)

      registry.rebind(name, stub)

      logger.debug("Nameserver running")
      readLine()
    }
    catch {
      case e : ArgotUsageException => println(e.message);
      case e : Throwable => {
        println("Nameserver exception: " + e.toString)
        e.printStackTrace()
      }
    }
  }

  /* The actual operations. */
  import java.util.concurrent.ConcurrentHashMap;
  val groups = new ConcurrentHashMap[Group, NodeID]()

  def listGroups() : List[Group] = {
    import collection.JavaConversions._

    logger.debug("Nameserver.listGroups")
    var ret = List[Group]()
    // TOTHINK: Iterators are weakly consistent. Take a snapshot instead?
    for (key <- groups.keys) {
      ret = key :: ret
    }

    return ret
  }

  def setGroupLeader(g : Group, l : NodeID) : Unit = {
    logger.debug("Nameserver.setGroupLeader")
    groups.put(g, l)
  }

  def getGroupLeader(g : Group) : Option[NodeID] = {
    logger.debug("Nameserver.getGroupLeader")
    if (groups.contains(g)) {
      return Some(groups.get(g))
    }
    else {
      return None
    }
  }

  def removeGroup(g: Group) : Boolean = {
    logger.debug("Nameserver.removeGroup")
    if (groups.contains(g)) {
      groups.remove(g)
      return true
    }
    else {
      return false
    }
  }

  /* Temp operations for testing.*/
  import java.util.concurrent.ConcurrentSkipListSet
  var group = new ConcurrentSkipListSet[NodeID]()

  def joinGroup(n : NodeID) : Unit = {
    logger.debug("Nameserver.joinGroup: " + n.toString)
    group.add(n)
  }

  def listGroupMembers() : List[NodeID] = {
    import collection.JavaConversions._

    logger.debug("Nameserver.listGroupMembers")
    var ret = List[NodeID]()
    // TOTHINK: Iterators are weakly consistent. Take a snapshot instead?
    for (node <- group.iterator()) {
      ret = node :: ret
    }

    return ret
  }

  import java.util.concurrent.atomic.AtomicInteger
  val counter = new AtomicInteger()

  def incCounter() : Int = {
    logger.debug("Nameserver.incCounter")
    return counter.getAndIncrement
  }
}
