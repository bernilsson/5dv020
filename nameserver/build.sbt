version := "0.1"

scalaVersion := "2.10.0"

scalacOptions += "-deprecation"

scalaSource in Compile <<= baseDirectory(_ / "src")

scalaSource in Test <<= baseDirectory(_ / "test")

libraryDependencies += "org.scalatest" % "scalatest_2.10.0-RC5" % "1.8-B1"

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.6.6"

libraryDependencies += "ch.qos.logback" % "logback-core" % "1.0.7"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.0.7"

seq(com.github.retronym.SbtOneJar.oneJarSettings: _*)

libraryDependencies += "commons-lang" % "commons-lang" % "2.6"

libraryDependencies += "org.clapper" % "argot_2.10" % "1.0.0"
