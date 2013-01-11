import java.rmi.registry.LocateRegistry

import org.scalatest.FlatSpec
import org.slf4j.LoggerFactory

import gcom.common._
import gcom.transport._

/** Requires an rmiregistry running on port 31337. */
class TransportSpec extends FlatSpec {

  "Transports" should "be able to exchange messages" in {
    val host     = Util.getLocalHostName
    val port     = 31337
    val t1name   = Util.getRandomUUID
    val t2name   = Util.getRandomUUID
    val t1id     = new NodeID(t1name, host, port)
    val t2id     = new NodeID(t2name, host, port)
    /* Registry must be running. */
    val registry = LocateRegistry.getRegistry(port)

    var message : Message  = TestMessage("BOGUS");

    val t1logger = LoggerFactory.getLogger(t1id.toString)
    val t2logger = LoggerFactory.getLogger(t2id.toString)
    val t1 = BasicTransport.create(t1id, {msg => }, t1logger);
    val t2 = BasicTransport.create(t2id, {msg => message = msg}, t2logger);
    val t1thread = new Thread(t1);
    val t2thread = new Thread(t2);

    t1thread.start();
    t2thread.start();

    val msg = TestMessage("TEST")
    t1.sendMessage(t2id, msg)
    Thread.sleep(1000)
    assert(msg == message)

    t1.receiveMessage(new BlackSpot())
    t2.receiveMessage(new BlackSpot())
  }

}
