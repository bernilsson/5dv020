import sbt._
import Keys._

object GComBuild extends Build {
    lazy val root = Project(id = "lab2",
                            base = file(".")) aggregate(gcom, nameserver, client)

    lazy val gcom = Project(id = "gcom",
                            base = file("gcom"))

    lazy val nameserver = Project(id = "nameserver",
                                  base = file("nameserver")) dependsOn(gcom)

    lazy val client = Project(id = "client",
                              base = file("client")) dependsOn(gcom)
}
