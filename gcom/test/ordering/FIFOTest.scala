
import org.scalatest._
import scala.util.Random
import gcom.common._
import java.rmi.registry.LocateRegistry
import org.slf4j.LoggerFactory
import gcom.transport.BasicTransport
import gcom.communication.NonReliable
import gcom.ordering.FIFO


class FIFOSpec extends FlatSpec {
   
    "FIFO ordering" should "deliver messages in" in {
    val host     = Util.getLocalHostName
    val port     = 31337
    val name     = Util.getRandomUUID
    val id       = new NodeID(name, host, port)

    /* Registry must be running. */
    val registry = LocateRegistry.getRegistry(port)

    var message : Message  = TestMessage.create("BOGUS");

    val logger        = LoggerFactory.getLogger(id.toString)
    val transport     = BasicTransport.create(id, {msg =>}, logger);
    val communication = NonReliable.create(transport, {msg =>})
    val ordering      = FIFO.create(communication, {msg => message = msg})
    val thread        = new Thread(transport);
    thread.start();
    
    ordering.updateView(List((id,0), (NodeID.fromString("1:1:1"),0),(NodeID.fromString("1:2:1"),0)))

    val msg = "TEST"
    
    ordering.sendToAll(List(id), msg)
    Thread.sleep(1000)
    assert(msg === message.payload)

    transport.receiveMessage(new BlackSpot())
  }
  
}