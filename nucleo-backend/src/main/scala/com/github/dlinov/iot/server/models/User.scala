package com.github.dlinov.iot.server.models

case class User(id: Option[String] = None,
                login: String,
                password: String,
                boards: Vector[Board] = Vector.empty,
                rules: Vector[Rule] = Vector.empty)
