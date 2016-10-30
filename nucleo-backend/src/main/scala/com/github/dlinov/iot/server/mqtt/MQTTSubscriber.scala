package com.github.dlinov.iot.server.mqtt

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorRef, Props}
import com.github.dlinov.iot.server.db.mongo.MongoConnector
import com.github.dlinov.iot.server.json.Parser
import com.github.dlinov.iot.server.services.SensorRulesWatcher
import com.github.dlinov.iot.server.services.SensorRulesWatcher.Messages.SensorData
import net.sigusr.mqtt.api._


class MQTTSubscriber(mqttHost: String, mqttPort: Int, name: String, topics: Vector[String], db: MongoConnector) extends Actor {

  context.actorOf(Manager.props(new InetSocketAddress(mqttHost, mqttPort))) ! Connect(name)

  def receive: Receive = {
    case Connected ⇒
      println(s"Successfully connected to $mqttHost:$mqttPort")
      sender() ! Subscribe(topics zip Vector.fill(topics.length) { AtMostOnce }, 1)
      context become ready(sender())
    case ConnectionFailure(reason) ⇒
      println(s"Connection to $mqttHost:$mqttPort failed [$reason]")
  }

  def ready(mqttManager: ActorRef): Receive = {
    case Subscribed(vQoS, MessageId(1)) ⇒
      println("Successfully subscribed to topics:")
      println(topics.mkString(" ", ",\n ", ""))
    case Message(topic, payload) ⇒
      val message = new String(payload.to[Array], "UTF-8")
      println(s"RECEIVED: [$topic] $message")
      val topicParts = topic.split('/')
      if (topicParts.length >= 3) {
        val (user, board) = topicParts(1) → topicParts(2)
        db.addBoardForUser(user, board)
        val parser = new Parser()
        val sensors = parser.parseMessage(message)
        val rulesWatcher = context.actorOf(Props(classOf[SensorRulesWatcher], db))
        rulesWatcher ! SensorData(user, sensors)
      }
    case Error(eKind) ⇒
      println(s"SUBSCRIBER ERROR: $eKind")
  }

  def disconnecting(): Receive = {
    case Disconnected ⇒
      println("Disconnected from localhost:1883")
  }
}
