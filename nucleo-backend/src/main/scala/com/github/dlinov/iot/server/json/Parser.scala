package com.github.dlinov.iot.server.json

import com.github.dlinov.iot.server.models.Sensor
import spray.json._

class Parser extends JsonSupport {
  def parseMessage(s: String): Vector[Sensor] = {
    val jsObjects = s.parseJson
    val sensors = jsObjects.convertTo[Vector[Sensor]]
    sensors
  }
}
