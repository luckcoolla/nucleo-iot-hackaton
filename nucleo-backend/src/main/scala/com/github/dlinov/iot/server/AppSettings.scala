package com.github.dlinov.iot.server

import com.typesafe.config.ConfigFactory

trait AppSettings {
  private val environment = System.getenv()
  private val config = ConfigFactory.load()

  private def readFromEnvOrSettings[T](envVar: String, configVar: String): String = {
    Option(environment.get(envVar))
      .getOrElse(config.getConfig("app.settings").getString(configVar))
  }

  val port = readFromEnvOrSettings("PORT", "port").toInt

  val mqttHost = readFromEnvOrSettings("MQTT_HOST", "mqtt-host")

  val mqttPort = readFromEnvOrSettings("MQTT_PORT", "mqtt-port").toInt

  val fcmServerKey = readFromEnvOrSettings("FCM_SERVER_KEY", "fcm-server-key")
}
