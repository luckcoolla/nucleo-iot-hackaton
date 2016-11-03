package com.github.dlinov.iot.server.models

case class User(id: Option[String] = None,
                login: String,
                password: String,
                boards: Seq[Board] = Seq.empty,
                rules: Seq[Rule] = Seq.empty)
