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
  import scala.collection.concurrent.TrieMap;
  val groups = TrieMap[Group, NodeID]()

  def listGroups() : Set[Group] = {
    logger.debug("Nameserver.listGroups")
    return groups.keySet.toSet
  }

  def setGroupLeader(g : Group, l : NodeID) : Unit = {
    logger.debug("Nameserver.setGroupLeader")
    groups.put(g, l)
  }

  def getGroupLeader(g : Group) : Option[NodeID] = {
    logger.debug("Nameserver.getGroupLeader")
    groups.get(g)
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
  var group = TrieMap[NodeID, Unit]()

  def joinGroup(n : NodeID) : Unit = {
    logger.debug("Nameserver.joinGroup: " + n.toString)
    group.put(n, ())
  }

  def listGroupMembers() : Set[NodeID] = {
    logger.debug("Nameserver.listGroupMembers")
    return group.keySet.toSet
  }

  import java.util.concurrent.atomic.AtomicInteger
  val counter = new AtomicInteger()

  def incCounter() : Int = {
    logger.debug("Nameserver.incCounter")
    return counter.getAndIncrement
  }
}
