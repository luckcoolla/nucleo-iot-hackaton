package com.github.dlinov.iot.server.pushes

import akka.actor.Actor
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import com.github.dlinov.iot.server.AppSettings
import com.github.dlinov.iot.server.models.Rule
import com.github.dlinov.iot.server.pushes.PushaT.Messages.TemperatureViolation

class PushaT extends Actor with AppSettings {
  import context.system
  import context.dispatcher
  implicit val materializer = ActorMaterializer()(context)

  def receive = {
    case TemperatureViolation(userName, degrees, rule) ⇒
      val title = s"The temperature is $degrees degrees!"
      val message = s"The rule allows ${rule.min.map(x ⇒ s"$x <= ").getOrElse("")}x${rule.max.map(x⇒ s" <= $x").getOrElse("")}"
      pushUser(userName, title, message)
      ()
  }

  def pushUser(userName: String, title: String, message: String) = {
    val json =
      s"""
         |{
         |  "to": "/topics/iot_$userName",
         |  "priority": "high",
         |  "notification": {
         |    "body": "$title",
         |    "title": "$message",
         |    "icon": "myicon"
         |  },
         |  "data": {
         |    "message":"$message"
         |  }
         |}""".stripMargin
    Http().singleRequest(
      HttpRequest(
        uri = Uri("https://fcm.googleapis.com/fcm/send"),
        method = HttpMethods.POST,
        headers = RawHeader("Authorization", s"key=$fcmServerKey") :: Nil,
        entity = HttpEntity(ContentTypes.`application/json`, json)
      )
    ).map(x ⇒ system.log.info(s"Received response from push request: ${x.toString}"))
  }
}

object PushaT {
  object Messages {
    case class TemperatureViolation(userName: String, current: Double, rule: Rule)
  }
}
