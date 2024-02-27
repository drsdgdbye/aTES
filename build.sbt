import Dependencies.*

import scala.collection.Seq

val sVer = "2.13.11"

lazy val commonSettings = Seq(
    organization := "com.example"
  , scalaVersion := sVer
  , scalacOptions ++= Seq("-Xlint:unused", "-deprecation")
)

lazy val root = (project in file("."))
  .aggregate(
      popugAcc
    , popuTracker
    , popuLedger
    , popuStat
  )
  .settings(
      name := "aTES"
    , publishArtifact := false
    , publish := {}
    , publishLocal := {}
    , test := {}
    , testOnly := {}
  )

lazy val popugAcc =
  project.in(file("popug-acc"))
    .settings(
        version := "0.0.1"
      , commonSettings
      , description := "Auth service"
      , libraryDependencies ++= commonDependencies
    )

lazy val popuTracker =
  project.in(file("popu-tracker"))
    .settings(
        version := "0.0.1"
      , commonSettings
      , description := "Task tracker service"
      , libraryDependencies ++= commonDependencies
    )

lazy val popuLedger =
  project.in(file("popu-ledger"))
    .settings(
        version := "0.0.1"
      , commonSettings
      , description := "Accounting service"
      , libraryDependencies ++= commonDependencies
    )

lazy val popuStat =
  project.in(file("popu-stat"))
    .settings(
        version := "0.0.1"
      , commonSettings
      , description := "Analytics service"
      , libraryDependencies ++= commonDependencies
    )