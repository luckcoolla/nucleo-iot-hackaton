package com.github.dlinov.iot.server

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.util.Base64
import java.util.zip.{GZIPInputStream, GZIPOutputStream}

import akka.event.LoggingAdapter

import scala.concurrent.{ExecutionContext, Future}
import scala.io.Source
import scala.util.Random

package object util {

  object Implicits {

    implicit class NameRandomizer(name: String) {
      def randomize(n: Int = 8) = s"$name-${Random.alphanumeric.take(n).mkString}"
    }

    implicit class FutureRecovery[T](f: Future[T]) {
      def recoverWithLog(maybeCustomErrorMessage: Option[String] = None)
                        (implicit ec: ExecutionContext, log: LoggingAdapter) =
        f.recoverWith {
          case e: Throwable ⇒
            maybeCustomErrorMessage.fold(log.error(e, e.getMessage))(log.error(e, _))
            Future.failed(e)
        }
    }

    implicit class Base64StringCompressor(s: String) {

      def deflate: String = {
        val arrOutputStream = new ByteArrayOutputStream()
        val zipOutputStream = new GZIPOutputStream(arrOutputStream)
        zipOutputStream.write(s.getBytes)
        zipOutputStream.close()
        val result = Base64.getEncoder.encodeToString(arrOutputStream.toByteArray)
        arrOutputStream.close()
        result
      }

      def inflate: String = {
        val bytes = Base64.getDecoder.decode(s)
        val byteArrayInputStream = new ByteArrayInputStream(bytes)
        val zipInputStream = new GZIPInputStream(byteArrayInputStream)
        val inflated = Source.fromInputStream(zipInputStream).mkString
        zipInputStream.close()
        byteArrayInputStream.close()
        inflated
      }

    }

  }

}
