version := "0.1"

scalaVersion := "2.10.0"

scalacOptions += "-deprecation"

libraryDependencies += "org.clapper" % "argot_2.10" % "1.0.0"

scalaSource in Compile <<= baseDirectory(_ / "src")

scalaSource in Test <<= baseDirectory(_ / "test")

seq(com.github.retronym.SbtOneJar.oneJarSettings: _*)
