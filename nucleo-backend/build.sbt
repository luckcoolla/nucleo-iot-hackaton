enablePlugins(JavaAppPackaging)

Revolver.settings

name := "iot-hackathon"

version := "1.0.0-SNAPSHOT"

scalaVersion := "2.11.8"

scalacOptions := Seq(
  "-language:implicitConversions",
  "-unchecked",
  "-feature",
  "-deprecation",
  "-encoding", "UTF-8",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-language:postfixOps",
  "-Xfatal-warnings",
  "-Xlint:_",
  "-Xfuture",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Ywarn-unused-import"
)

libraryDependencies ++= {
  val akkaV = "2.4.11"
  val logbackV = "1.1.7"
  val mqttV = "0.6.0"
  val mongoV = "1.1.1"

  val logback = "ch.qos.logback" % "logback-classic" % logbackV
  val akka = Seq(
    "com.typesafe.akka"           %% "akka-actor"                         % akkaV,
    "com.typesafe.akka"           %% "akka-slf4j"                         % akkaV,
    "com.typesafe.akka"           %% "akka-testkit"                       % akkaV    % "test",
    "com.typesafe.akka"           %% "akka-http-experimental"             % akkaV,
    "com.typesafe.akka"           %% "akka-http-spray-json-experimental"  % akkaV,
    "com.typesafe.akka"           %% "akka-http-testkit"                  % akkaV    % "test"
  )
  val mqtt = "net.sigusr" %% "scala-mqtt-client" % mqttV
  val mongo = "org.mongodb.scala" %% "mongo-scala-driver" % mongoV

  akka ++ Seq(logback, mqtt, mongo)
}

//resolvers ++= Seq(
//  Resolver.sonatypeRepo("releases"),
//  Resolver.sonatypeRepo("snapshots")
//)
