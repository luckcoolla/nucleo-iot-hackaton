package com.github.dlinov.iot.server.models

sealed trait SensorType
case object Temperature extends SensorType
case object Humidity extends SensorType
case object Pressure extends SensorType
case object Accelerometer extends SensorType
case object Magnetometer extends SensorType
case object Gyroscope extends SensorType
case object Unknown extends SensorType
