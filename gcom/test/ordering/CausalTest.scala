package test.ordering
import java.rmi.registry.LocateRegistry
import org.scalatest.FlatSpec
import org.slf4j.LoggerFactory
import gcom.common._
import gcom.transport._
import gcom.communication._
import gcom.ordering._
import scala.util.Random
import gcom.ordering.Causal



/** Requires an rmiregistry running on port 31337. */
class CausalSpec extends FlatSpec {

  "Causal ordering" should "deliver messages in a causal order" in {
    val host     = "localhost" //Util.getLocalHostName
    val port     = 31337

    val name     = Util.getRandomUUID
    
    val id       = new NodeID(name, host, port)

    /* Registry must be running. */
    val registry = LocateRegistry.getRegistry(port)

    var receivedList: List[Message] = List()
    var sentMessages: List[String] = List()
    //The first causal message sent will have a time of 1
    val shuffled = Random.shuffle((1 to 3))
    
    var order = 0;
    
    val logger        = LoggerFactory.getLogger(id.toString)
    val transport     = BasicTransport.create(id, {msg =>}, logger);
    val communication = NonReliable.create(transport, {msg =>})
    val ordering      = Causal.create(communication, {msg => receivedList = receivedList :+ msg }, id)
    val thread        = new Thread(transport);
    thread.start();
    
    ordering.updateView(List((NodeID.fromString("1:1:1"),0),(NodeID.fromString("1:2:1"),0),(id,0)))
    
    for(i <- shuffled){
    	val msg = "" + i
    	sentMessages = sentMessages :+ msg
    	ordering.sendToAll(List(id), msg)
    }
    
    Thread.sleep(1000)
    assert(receivedList.map(_.payload) === sentMessages )

    transport.receiveMessage(new BlackSpot())
  }
}
