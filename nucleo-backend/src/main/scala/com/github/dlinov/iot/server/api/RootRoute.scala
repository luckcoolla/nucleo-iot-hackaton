package com.github.dlinov.iot.server.api

import akka.http.scaladsl.server.Directives._
import com.github.dlinov.iot.server.db.mongo.MongoConnector
import com.github.dlinov.iot.server.json.JsonSupport
import com.github.dlinov.iot.server.models.{Board, Rule}

import scala.concurrent.ExecutionContext

class RootRoute(db: MongoConnector)(implicit ec: ExecutionContext) extends JsonSupport {
  import RootRoute._
  val route =
    path(authPrefix) {
      get {
        parameters("login", "password") { (login, password) ⇒
          rejectEmptyResponse {
            complete {
              db.getToken(login, password)
            }
          }
        }
      } ~
      post {
        parameters("login", "password") { (login, password) ⇒
          rejectEmptyResponse {
            complete {
              db.createUser(login, password)
            }
          }
        }
      }
    } ~
    path(boardsPrefix) {
      parameter("token") { token ⇒
        get {
          complete(db.getUserBoards(token))
        } ~
        put {
          entity(as[Board]) { board ⇒
            complete { db.updateBoardName(token, board.id, board.name) }
          }
        }
      }
    } ~
    path(rulesPrefix) {
      parameter("token") { token ⇒
        get {
          complete {
            db.getUserRulesByToken(token)
          }
        } ~
        post {
          entity(as[Rule]) { rule ⇒
            complete {
              db.addUserRule(token, rule)
            }
          }
        } ~
        put {
          entity(as[Rule]) { rule ⇒
            complete {
              db.updateUserRule(token, rule)
            }
          }
        } ~
        delete {
          parameter("ruleId") { ruleId ⇒
            complete {
              db.removeUserRule(token, ruleId)
            }
          }
        }
      }
    } ~
    pathEndOrSingleSlash {
      get {
        complete { "OK" }
      } ~
      post {
        entity(as[String]) { json ⇒
          complete { "POST OK" }
        }
      }
    }
}

object RootRoute {
  val authPrefix = "auth"
  val boardsPrefix = "boards"
  val rulesPrefix = "rules"
}
