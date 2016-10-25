package com.github.dlinov.iot.server.models

case class Sensor(id: Option[String] = None,
                  boardId: Option[String] = None,
                  name: Option[String] = Some(""),
                  sensorType: SensorType,
                  vs: Option[Vector[SensorNamedValue]] = None,
                  v: Option[Double] = None)
