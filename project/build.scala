import com.github.retronym.SbtOneJar
import sbt._
import Keys._

object GComBuild extends Build {
    def standardSettings = Seq(
      exportJars := true
    ) ++ Defaults.defaultSettings

    lazy val root = Project(id = "lab2",
                            base = file("."),
                            aggregate = Seq(gcom, registry, nameserver, client))

    lazy val gcom = Project(id = "gcom",
                            base = file("gcom"),
                            settings = standardSettings)

    lazy val registry = Project(id = "registry",
                                base = file("registry"),
                                dependencies = Seq(gcom),
                                settings = standardSettings ++
                                  SbtOneJar.oneJarSettings)

    lazy val nameserver = Project(id = "nameserver",
                                  base = file("nameserver"),
                                  dependencies = Seq(gcom),
                                  settings = standardSettings ++
                                    SbtOneJar.oneJarSettings)

    lazy val client = Project(id = "client",
                              base = file("client"),
                              dependencies = Seq(gcom),
                              settings = standardSettings ++
                                SbtOneJar.oneJarSettings)
}
