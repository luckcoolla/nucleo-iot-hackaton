package com.github.dlinov.iot.server

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import com.github.dlinov.iot.server.api.RootRoute
import com.github.dlinov.iot.server.db.mongo.MongoConnector
import com.github.dlinov.iot.server.mqtt.MQTTSubscriber
import com.github.dlinov.iot.server.util.Implicits._

object Boot extends App with AppSettings {
  implicit val system = ActorSystem("mqtt-test")
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  val host = "0.0.0.0"
  val mongodbUri = Option(System.getenv("MONGODB_URI")).getOrElse("mongodb://localhost/iot")
  val dbName = mongodbUri.split("/").last
  val db = new MongoConnector(mongodbUri, dbName, system.log)

  val iotTopics = Vector("iot/#", "iot-bot/#")
  val mqttSubscriber = system.actorOf(
    Props(classOf[MQTTSubscriber], mqttHost, mqttPort, "subscriber-scala-bot".randomize(), iotTopics, db)
  )
  val route = new RootRoute(db).route

  val bindingFuture = Http().bindAndHandle(route, host, port)
  Http().singleRequest(
    HttpRequest(
      uri = Uri("https://fcm.googleapis.com/fcm/send"),
      method = HttpMethods.POST,
      headers = RawHeader("Authorization", s"key=$fcmServerKey") :: Nil,
      entity = HttpEntity(ContentTypes.`application/json`, """{"to": "/topics/iot_test","data":{"message":"This is test message"}}""")
    )
  ).map(x ⇒ system.log.info(x.entity.toString))

  sys.addShutdownHook {
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ ⇒ system.terminate()) // and shutdown when done
  }
}