package com.github.dlinov.iot.server

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.util.Base64
import java.util.zip.{GZIPInputStream, GZIPOutputStream}

import scala.io.Source
import scala.util.Random

package object util {

  object Implicits {

    implicit class NameRandomizer(name: String) {
      def randomize(n: Int = 8) = s"$name-${Random.alphanumeric.take(n).mkString}"
    }

    implicit class Base64StringCompressor(s: String) {

      def deflate: String = {
        val arrOutputStream = new ByteArrayOutputStream()
        val zipOutputStream = new GZIPOutputStream(arrOutputStream)
        zipOutputStream.write(s.getBytes)
        zipOutputStream.close()
        Base64.getEncoder.encodeToString(arrOutputStream.toByteArray)
      }

      def inflate: String = {
        val bytes = Base64.getDecoder.decode(s)
        val zipInputStream = new GZIPInputStream(new ByteArrayInputStream(bytes))
        val inflated = Source.fromInputStream(zipInputStream).mkString
        zipInputStream.close()
        inflated
      }

    }

  }

}
