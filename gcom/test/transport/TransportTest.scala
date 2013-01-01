import org.scalatest.FlatSpec

import gcom.transport._

class TransportSpec extends FlatSpec {

  "1" should "not be equal to 2" in {
    assert(1 != 2)
  }

  "Transport.someMethod" should "return 23" in {
    val t = new Transport
    assert(t.someMethod() == 23)
  }

}
