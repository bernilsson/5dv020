package gcom.nameserver

object NameServer {

  import org.clapper.argot.ArgotParser
  import org.clapper.argot.ArgotUsageException
  import org.clapper.argot.ArgotConverters._

  val parser = new ArgotParser(
    "gcom-nameserver",
    preUsage=Some("gcom-nameserver 0.1")
  )

  val portopt = parser.option[Int](List("p", "port"), "n", "port number")
  lazy val port = portopt.value.getOrElse(31337)

  def main(args: Array[String]) = {
    try {
      parser.parse(args)
      println("Running nameserver on port " + port)
    }
    catch {
      case e : ArgotUsageException => println(e.message);
    }
  }
}
