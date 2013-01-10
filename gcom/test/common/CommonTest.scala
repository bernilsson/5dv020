import org.scalatest.FunSuite

import gcom.common._

class NodeIDSpec extends FunSuite {

  test("NodeIDs can be parsed") {
    val str = "name:host:31337"
    val n = NodeID.fromString(str)
    assert(n.name == "name")
    assert(n.host == "host")
    assert(n.port == 31337)
  }

}
