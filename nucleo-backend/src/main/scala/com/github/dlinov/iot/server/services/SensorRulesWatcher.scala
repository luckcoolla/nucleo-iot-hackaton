package com.github.dlinov.iot.server.services

import akka.actor.{Actor, Props}
import com.github.dlinov.iot.server.db.mongo.MongoConnector
import com.github.dlinov.iot.server.models.{Rule, Sensor, Temperature}
import com.github.dlinov.iot.server.pushes.PushaT
import com.github.dlinov.iot.server.pushes.PushaT.Messages.TemperatureViolation
import com.github.dlinov.iot.server.services.SensorRulesWatcher.Messages.SensorData

class SensorRulesWatcher(db: MongoConnector) extends Actor {
  import context.dispatcher

  val pushaT = context.actorOf(Props[PushaT])

  def receive = {
    case SensorData(login, sensors) ⇒
      db.getUserRulesByLogin(login).foreach(_.foreach(processRules(login, _, sensors)))
  }

  def processRules(login: String, rules: Vector[Rule], sensors: Vector[Sensor]) = {
    sensors.flatMap(s ⇒ {
      val sensorRules = rules.filter(_.sensorType == s.sensorType)
      sensorRules.collect {
        case r if s.v.exists(value ⇒ r.min.exists(value < _) || r.max.exists(value > _)) ⇒ r → s
        // TODO: 3-values rules (gyroscope and so on)
      }
    }).foreach({
      case (rule, sensor) if sensor.sensorType == Temperature ⇒
        pushaT ! TemperatureViolation(login, sensor.v.getOrElse(-274.0), rule)
      case _ ⇒ ()
    })
  }
}

object SensorRulesWatcher {
  object Messages {
    case class SensorData(userLogin: String, sensors: Vector[Sensor])
  }
}
