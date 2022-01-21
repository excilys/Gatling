/*
 * Copyright 2011-2022 GatlingCorp (https://gatling.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.gatling.core.util

import java.io._
import java.net.{ URISyntaxException, URL }
import java.nio.charset.Charset
import java.nio.file.{ Files, Path, Paths }
import java.util.concurrent.ConcurrentHashMap

import scala.util.Using

import io.gatling.commons.shared.unstable.util.PathHelper._
import io.gatling.commons.util.Io._
import io.gatling.commons.validation._

import com.typesafe.scalalogging.LazyLogging

object Resource {

  private final case class Location(customDirectory: Option[Path], path: String)

  private object ClasspathResource extends LazyLogging {

    private def urlToFile(url: URL): File =
      try {
        new File(url.toURI)
      } catch {
        case _: URISyntaxException => new File(url.getPath)
      }

    def unapply(location: Location): Option[Validation[Resource]] = {
      val nixPath = location.path.replace('\\', '/')
      val cleanPath = nixPath
        .replace("src/test/resources/", "")
        .replace("src/main/resources/", "")
        .replace("src/gatling/resources/", "")

      if (cleanPath != nixPath) {
        logger.warn(s"""Your resource's path ${location.path} is incorrect.
                       |It should not be relative to your project root on the filesystem.
                       |Instead, it should be relative to your classpath root.
                       |We've clean it up into $cleanPath for you but please fix it.
                       |""".stripMargin)
      }

      Option(getClass.getClassLoader.getResource(cleanPath)).map { url =>
        url.getProtocol match {
          case "file" => ClasspathFileResource(cleanPath, urlToFile(url)).success
          case "jar"  => ClasspathPackagedResource(cleanPath, url).success
          case _      => s"$url is neither a file nor a jar".failure
        }
      }
    }
  }

  private object DirectoryChildResource {
    def unapply(location: Location): Option[Validation[Resource]] =
      location.customDirectory.flatMap { customDirectory =>
        (customDirectory / location.path).ifFile { f =>
          if (f.canRead)
            FilesystemResource(f).success
          else
            s"File $f can't be read".failure
        }
      }
  }

  private object AbsoluteFileResource {
    def unapply(location: Location): Option[Validation[Resource]] =
      Paths.get(location.path).ifFile(FilesystemResource(_).success)
  }

  private[gatling] def resolveResource(customDirectory: Option[Path], path: String): Validation[Resource] =
    Location(customDirectory, path) match {
      case DirectoryChildResource(res) => res
      case ClasspathResource(res)      => res
      case AbsoluteFileResource(res)   => res
      case _                           => s"Resource $path not found".failure
    }
}

sealed trait Resource {
  def name: String
  def file: File
  def inputStream: InputStream = new BufferedInputStream(new FileInputStream(file))
  def string(charset: Charset): String = Using.resource(inputStream) { _.toString(charset) }
  def bytes: Array[Byte] = Files.readAllBytes(file.toPath)
}

final case class ClasspathPackagedResource(path: String, url: URL) extends Resource {
  override val name: String = {
    val urlString = url.toString
    urlString.lastIndexOf(File.separatorChar) match {
      case -1 => urlString
      case i  => urlString.substring(i + 1)
    }
  }

  override lazy val file: File = {
    val tempFile = File.createTempFile("gatling-" + name, null)
    tempFile.deleteOnExit()

    Using.resources(url.openStream(), new BufferedOutputStream(new FileOutputStream(tempFile, false))) { (is, os) =>
      is.copyTo(os)
    }
    tempFile
  }
}

final case class ClasspathFileResource(path: String, file: File) extends Resource {
  override val name: String = file.getName
}

final case class FilesystemResource(file: File) extends Resource {
  override val name: String = file.getName
}

trait ResourceCache {
  private val resourceCache = new ConcurrentHashMap[String, Validation[Resource]]()

  protected def cachedResource(customResourcesDirectory: Option[Path], path: String): Validation[Resource] =
    resourceCache.computeIfAbsent(path, Resource.resolveResource(customResourcesDirectory, _))
}
