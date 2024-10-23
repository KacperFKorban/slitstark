val scala3 = "3.3.3"

val commonSettings = Seq(
  organization := "io.github.kacperfkorban",
  description := "Scala 3 Mirrors for value classes",
  homepage := Some(url("https://github.com/KacperFKorban/slitstark")),
  licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
  developers := List(
    Developer(
      "KacperFKorban",
      "Kacper Korban",
      "kacper.f.korban@gmail.com",
      url("https://twitter.com/KacperFKorban")
    )
  ),
  scalaVersion := scala3,
  scalacOptions ++= Seq(
    // "-Xcheck-macros",
    // "-Ycheck:inlining",
    "-explain",
    "-deprecation",
    "-unchecked",
    "-feature"
  ),
  libraryDependencies ++= Seq(
    "org.scalameta" %%% "munit" % "1.0.0" % Test
  )
)

lazy val root = project
  .in(file("."))
  .settings(commonSettings)
  .settings(
    name := "slitstark-root",
    publish / skip := true
  )
  .aggregate(slitstark.projectRefs : _*)

lazy val slitstark = projectMatrix
  .in(file("slitstark"))
  .settings(commonSettings)
  .settings(
    name := "slitstark",
  )
  .jvmPlatform(scalaVersions = List(scala3))
  .jsPlatform(scalaVersions = List(scala3))
  .nativePlatform(scalaVersions = List(scala3))
