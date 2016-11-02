package com.github.dlinov.iot.server.db.mongo

import akka.event.LoggingAdapter
import com.github.dlinov.iot.server.models._
import com.github.dlinov.iot.server.util.Implicits.FutureRecovery
import org.bson.conversions.Bson
import org.mongodb.scala.bson.{BsonArray, BsonDocument, BsonNumber, BsonObjectId, BsonString, BsonValue}
import org.mongodb.scala._
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Updates._

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

class MongoConnector(val uri: String, dbName: String)
                    (implicit ec: ExecutionContext, log: LoggingAdapter)
  extends ObservableImplicits {

  lazy val mongoClient: MongoClient = MongoClient(uri)
  lazy val database: MongoDatabase = mongoClient.getDatabase(dbName)
  lazy val sensors = database.getCollection("sensors")
  lazy val users = database.getCollection("users")

  def newId = BsonObjectId().getValue.toHexString

  object uf {
    val idF = "_id"
    val loginF = "login"
    val passwordF = "password"
    val boardsF = "boards"
    val rulesF = "rules"
  }

  object bf {
    val idF = "_id"
    val nameF = "name"
  }

  object rf {
    val idF = "_id"
    val nameF = "name"
    val boardIdF = "boardId"
    val sensorTypeF = "type"
    val minF = "min"
    val maxF = "max"

    val t = "temperature"
    val h = "humidity"
    val p = "pressure"
    val a = "accelerometer"
    val m = "magnetometer"
    val g = "gyroscope"
  }

  implicit class FromDocumentTransformer(document: Document) {
    def toUser = {
      import uf._
      val id = document.get(idF).map(_.asInstanceOf[BsonString].getValue)
      val login = document.getOrElse(loginF, "").asInstanceOf[BsonString].getValue
      val password = document.getOrElse(passwordF, "").asInstanceOf[BsonString].getValue
      val boards = document.get(boardsF)
        .map(_.asInstanceOf[BsonArray].getValues.asScala
          .map(v ⇒ Document(v.asDocument()).toBoard(login)).toVector)
        .getOrElse(Vector.empty[Board])
      val rules = document.get(rulesF)
        .map(_.asInstanceOf[BsonArray].getValues.asScala
          .map(v ⇒ Document(v.asDocument()).toRule).toVector)
        .getOrElse(Vector.empty[Rule])
      User(id = id, login = login, password = password, boards = boards, rules = rules)
    }

    def toBoard(userLogin: String) = {
      import bf._
      val id = document.get(idF).map(_.asInstanceOf[BsonString].getValue)
      val name = document.getOrElse(nameF, "").asInstanceOf[BsonString].getValue
      val topic = Boards.genTopic(userLogin, id.getOrElse(""))
      Board(id = id, name = name, topic = topic)
    }

    def toRule: Rule = {
      import rf._
      val id = document.get(idF).map(_.asInstanceOf[BsonString].getValue)
      val name = document.getOrElse(nameF, "").asInstanceOf[BsonString].getValue
      val boardId = document.getOrElse(boardIdF, "").asInstanceOf[BsonString].getValue
      val min = document.get(minF).map(_.asInstanceOf[BsonNumber].asDouble().getValue)
      val max = document.get(maxF).map(_.asInstanceOf[BsonNumber].asDouble().getValue)
      val sensorType = document.getOrElse(sensorTypeF, "unknown").asInstanceOf[BsonString].getValue match {
        case `t` ⇒ Temperature
        case `h` ⇒ Humidity
        case `p` ⇒ Pressure
        case `a` ⇒ Accelerometer
        case `m` ⇒ Magnetometer
        case `g` ⇒ Gyroscope
        case _ ⇒ Unknown
      }
      Rule(id, name, boardId, sensorType, min, max)
    }
  }

  implicit class UserToDocumentTransformer(user: User) {
    def toDocument = {
      import uf._
      val bs: Seq[BsonValue] = user.boards.map(_.toBsonDocument)
      val rs: Seq[BsonValue] = user.rules.map(_.toBsonDocument)
      Document(
        idF → user.id.getOrElse(newId),
        loginF → user.login,
        passwordF → user.password,
        boardsF → BsonArray(bs),
        rulesF → BsonArray(rs)
      )
    }
  }

  implicit class BoardToDocumentTransformer(board: Board) {
    def toBsonDocument = {
      import bf._
      BsonDocument(
        idF → board.id.getOrElse(newId),
        nameF → board.name
      )
    }

    def toDocument = {
      import bf._
      Document(
        idF → board.id.getOrElse(newId),
        nameF → board.name
      )
    }
  }

  implicit class RuleToDocumentTransformer(rule: Rule) {
    import rf._

    private val typeToName = rule.sensorType match {
      case Temperature ⇒ t
      case Humidity ⇒ h
      case Pressure ⇒ p
      case Accelerometer ⇒ a
      case Magnetometer ⇒ m
      case Gyroscope ⇒ g
      case _ ⇒ "unknown"
    }

    def toBsonDocument = {
      val sensorName = typeToName
      BsonDocument(
        idF → rule.id.getOrElse(newId),
        boardIdF → rule.boardId,
        nameF → rule.name,
        sensorTypeF → sensorName,
        minF → rule.min.getOrElse(0.0),
        maxF → rule.max.getOrElse(0.0)
      )
    }
  }

  def getUserBy(filter: Bson): Future[Option[User]] = {
    users.find(filter)
      .toFuture()
      .recoverWithLog()
      .map(_.headOption.map(_.toUser))
  }

  def getUserByToken(userId: String) = getUserBy(equal(uf.idF, userId))

  def getUserByLogin(login: String) = getUserBy(equal(uf.loginF, login))

  def getUserByLoginAndPassword(login: String, password: String) =
    getUserBy(and(equal(uf.loginF, login), equal(uf.passwordF, password)))

  def getToken(login: String, password: String): Future[Option[String]] =
    getUserByLoginAndPassword(login, password).map(_.flatMap(_.id))

  def createUser(login: String, password: String) = {
    val newUserId = Option(newId)
    for {
      token ← getToken(login, password)
      maybeId ← token.fold(users.insertOne(User(id = newUserId, login = login, password = password).toDocument)
        .toFuture()
        .recoverWithLog()
        .map(_ ⇒ newUserId))(_ ⇒ Future successful Option.empty[String])
    } yield maybeId
  }

  def getUserBoards(userId: String) = getUserByToken(userId).map(_.map(_.boards))

  def addBoardForUser(login: String, boardId: String) = {
    for {
      user ← getUserByLogin(login)
      boards ← user.fold(
        Future successful Option.empty[Vector[Board]]
      )(u ⇒
        if (u.boards.exists(_.id.contains(boardId))) {
          Future successful Some(u.boards)
        } else {
          val newBoard = Board(
            id = Some(boardId),
            name = s"Board #${u.boards.length + 1}",
            topic = Boards.genTopic(u.login, boardId)
          )
          users.findOneAndUpdate(equal(uf.loginF, login), addToSet(uf.boardsF, newBoard.toDocument))
            .toFuture()
            .recoverWithLog()
            .map(_.headOption.map(_.toUser.boards))
        }
      )
    } yield boards
  }

  def getUserRulesByLogin(login: String) = getUserByLogin(login).map(_.map(_.rules))

  def getUserRulesByToken(userId: String) = getUserByToken(userId).map(_.map(_.rules))

  def addUserRule(userId: String, rule: Rule) = {
    for {
      user ← getUserByToken(userId)
      rules ← user.fold(
        Future successful Option.empty[Vector[Rule]]
      )(_ ⇒
        users.findOneAndUpdate(equal(uf.idF, userId), addToSet(uf.rulesF, rule.copy(id = Some(newId)).toBsonDocument))
          .toFuture()
          .recoverWithLog()
          .map(_.headOption.map(_.toUser.rules))
      )
    } yield rules
  }

  def updateUserRule(userId: String, rule: Rule) = {
    for {
      user ← getUserByToken(userId)
      rules ← user.fold(
        Future successful Option.empty[Vector[Rule]]
      )(u ⇒ {
        val updatedRules = u.rules.collect {
          case r if r.id == rule.id ⇒
            r.copy(
              name = rule.name,
              boardId = rule.boardId,
              min = rule.min.orElse(r.min),
              max = rule.max.orElse(r.max)
            )
          case r ⇒ r
        }
        users.findOneAndUpdate(
          equal(uf.idF, userId),
          set(uf.rulesF, updatedRules.map(_.toBsonDocument)))
          .toFuture()
          .recoverWithLog()
          .map(_.headOption.map(_.toUser.rules))
      })
    } yield rules
  }

  def removeUserRule(userId: String, ruleId: String) = {
    for {
      user ← getUserByToken(userId)
      rules ← user.fold(
        Future successful Option.empty[Vector[Rule]]
      )(u ⇒ {
        val newRules = u.rules.filterNot(_.id.contains(ruleId))
        users.findOneAndUpdate(
          equal(uf.idF, userId),
          set(uf.rulesF, BsonArray(newRules.map(_.toBsonDocument))))
          .toFuture()
          .recoverWithLog()
          .map(_.headOption.map(_.toUser.rules))
      })
    } yield rules
  }
}
