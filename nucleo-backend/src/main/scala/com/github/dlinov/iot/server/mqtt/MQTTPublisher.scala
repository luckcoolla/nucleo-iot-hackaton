package com.github.dlinov.iot.server.mqtt

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorRef}
import net.sigusr.mqtt.api._

import scala.concurrent.duration._
import scala.util.Random

class MQTTPublisher(mqttHost: String,
                    mqttPort: Int,
                    name: String,
                    topic: String) extends Actor {

  import context.dispatcher

  context.actorOf(Manager.props(new InetSocketAddress(mqttHost, mqttPort))) ! Connect(name)

  val toPublish = Vector(
    """[ { "sensor_name":"Temperature in the bedroom", "sensor_type":"temperature", "values":[  ], "value":42.0 }, { "sensor_name":"Humidity in the bedroom", "sensor_type":"humidity", "values":[  ], "value":42.0 }, { "sensor_name":"Pressure in the bedroom", "sensor_type":"pressure", "values":[  ], "value":42.0 }, { "sensor_name":"Accelerometer in the bedroom", "sensor_type":"accelerometer", "values":[ { "type":"x", "value":0.0 }, { "type":"y", "value":1.0 }, { "type":"z", "value":-1.0 } ], "value":0.0 }, { "sensor_name":"Gyroscope in the bedroom", "sensor_type":"gyroscope", "values":[ { "type":"x", "value":0.0 }, { "type":"y", "value":1.0 }, { "type":"z", "value":-1.0 } ], "value":0.0 }, { "sensor_name":"Magnetometer in the bedroom", "sensor_type":"magnetometer", "values":[ { "type":"x", "value":0.0 }, { "type":"y", "value":1.0 }, { "type":"z", "value":-1.0 } ], "value":0.0 } ]"""
//    """{"temperature": 20.0, "humidity": 50.0}""",
//    """{"temperature": 21.0, "humidity": 60.0}""",
//    """{"temperature": 22.0, "humidity": 70.0}""",
//    """{"temperature": 35.0, "humidity": 100.0}"""
  )
  val length = toPublish.length

  def scheduleRandomMessage(): Unit = {
    val message = toPublish(Random.nextInt(length))
    context.system.scheduler.scheduleOnce(10.seconds, self, message)
    ()
  }

  def receive: Receive = {
    case Connected ⇒
      println(s"Successfully connected to $mqttHost:$mqttPort")
      println(s"Ready to publish to topic [ $topic ]")
      scheduleRandomMessage()
      context become ready(sender())
    case ConnectionFailure(reason) ⇒
      println(s"Connection to $mqttHost:$mqttPort failed [$reason]")
  }

  def ready(mqttManager: ActorRef): Receive = {
    case m: String ⇒
      println(s"Publishing [ $m ]")
      mqttManager ! Publish(topic, m.getBytes("UTF-8").to[Vector])
      scheduleRandomMessage()
  }
}
