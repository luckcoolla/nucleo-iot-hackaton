package com.github.dlinov.iot.server.models

case class Rule(id: Option[String] = None,
                name: String,
                boardId: String,
                sensorType: SensorType,
                min: Option[Double],
                max: Option[Double]
               )
