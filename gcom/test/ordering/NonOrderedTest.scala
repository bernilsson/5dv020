import java.rmi.registry.LocateRegistry

import org.scalatest.FlatSpec
import org.slf4j.LoggerFactory

import gcom.common._
import gcom.transport._
import gcom.communication._
import gcom.ordering._

/** Requires an rmiregistry running on port 31337. */
class NonOrderedSpec extends FlatSpec {

  "NonOrdered ordering" should "deliver messages instantly" in {
    val host     = Util.getLocalHostName
    val port     = 31337
    val name     = Util.getRandomUUID
    val id       = new NodeID(name, host, port)

    /* Registry must be running. */
    val registry = LocateRegistry.getRegistry(port)

    var message  = "BOGUS";

    val logger        = LoggerFactory.getLogger(id.toString)
    val transport     = BasicTransport.create(id, {msg =>}, logger);
    val communication = NonReliable.create(transport, {msg =>})
    val ordering      = NonOrdered.create(communication, {msg => message = msg.payload})
    val thread        = new Thread(transport);
    thread.start();

    val msg = "TEST"
    ordering.sendToAll(List(id), msg)
    Thread.sleep(1000)
    assert(msg == message)

    transport.receiveMessage(new BlackSpot())
  }
}
