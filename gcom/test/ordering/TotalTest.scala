package test.ordering
import java.rmi.registry.LocateRegistry
import org.scalatest.FlatSpec
import org.slf4j.LoggerFactory
import gcom.common._
import gcom.transport._
import gcom.communication._
import gcom.ordering._
import scala.util.Random



/** Requires an rmiregistry running on port 31337. */
class TotalSpec extends FlatSpec {

  "Total ordering" should "deliver messages in a total order" in {
    val host     = "localhost" //Util.getLocalHostName
    val port     = 31337

    val name     = Util.getRandomUUID

    val id       = new NodeID(name, host, port)

    /* Registry must be running. */
    val registry = LocateRegistry.getRegistry(port)

    var receivedList: List[Message] = List()
    var sentMessages: List[String] = List()
    val shuffled = Random.shuffle((0 to 10))

    var order = 0;

    val logger        = LoggerFactory.getLogger(id.toString)
    val transport     = BasicTransport.create(id, {msg =>}, logger);
    val communication = NonReliable.create(transport, {msg =>})
    val ordering      = Total.create(communication, {msg => receivedList = receivedList :+ msg },
                                                    {() => order+=1; order-1})
    val thread        = new Thread(transport);
    thread.start();


    for(i <- shuffled){
        val msg = ""+ i
        sentMessages = sentMessages :+ msg
        ordering.sendToAll(List(id), msg)
    }
    Thread.sleep(1000)
    assert(receivedList.map(_.payload) === sentMessages)

    transport.receiveMessage(new BlackSpot())
  }
}
