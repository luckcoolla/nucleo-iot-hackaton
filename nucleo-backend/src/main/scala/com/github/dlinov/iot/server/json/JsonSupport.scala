package com.github.dlinov.iot.server.json

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.github.dlinov.iot.server.models._
import spray.json._

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

  implicit val sensorTypeFormat = new RootJsonFormat[SensorType] {
    val t = "temperature"
    val h = "humidity"
    val p = "pressure"
    val a = "accelerometer"
    val m = "magnetometer"
    val g = "gyroscope"

    def read(json: JsValue) = json match {
      case JsString(`t`) ⇒ Temperature
      case JsString(`h`) ⇒ Humidity
      case JsString(`p`) ⇒ Pressure
      case JsString(`a`) ⇒ Accelerometer
      case JsString(`m`) ⇒ Magnetometer
      case JsString(`g`) ⇒ Gyroscope
      case _ ⇒ Unknown
    }

    def write(obj: SensorType) = {
      JsString(obj match {
        case Temperature ⇒ t
        case Humidity ⇒ h
        case Pressure ⇒ p
        case Accelerometer ⇒ a
        case Magnetometer ⇒ m
        case Gyroscope ⇒ g
        case _ ⇒ "unknown"
      })
    }
  }

//  implicit val sensorValueFormat = jsonFormat1(SensorValue)

  implicit val sensorNamedValueFormat = jsonFormat2(SensorNamedValue)

  implicit val sensorFormat = jsonFormat6(Sensor)

  implicit val boardFormat = jsonFormat3(Board)

  implicit val ruleFormat = jsonFormat6(Rule)

  implicit val userFormat = jsonFormat5(User)
}

