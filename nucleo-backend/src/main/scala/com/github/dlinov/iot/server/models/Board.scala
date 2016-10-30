package com.github.dlinov.iot.server.models

case class Board(id: Option[String], name: String, topic: String)

object Boards {
  def genTopic(userLogin: String, id: String) = s"iot/$userLogin/$id"
}
