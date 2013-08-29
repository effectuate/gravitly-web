package ly.gravit.web

import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.Source
import java.io.File
import fly.play.s3.{S3Exception, PUBLIC_READ, BucketFile, S3}
import play.Logger

/**
 * Created with IntelliJ IDEA.
 * User: ginduc
 * Date: 8/29/13
 * Time: 2:25 PM
 * To change this template use File | Settings | File Templates.
 */
trait S3Connectivity {
  def toByteArray(file: File) = {
    val src = Source.fromFile(file)(scala.io.Codec.ISO8859)
    val byteArray = src.map(_.toByte).toArray
    src.close

    byteArray
  }

  def fileExtension(mimeType: Option[String]) = mimeType match {
    case Some("image/png") => "png"
    case Some("image/gif") => "gif"
    case Some("image/jpeg") => "jpeg"
    case _ => "jpg"
  }

  def upload(bucketName: String, byteArray: Array[Byte], fileName: String, mimeType: String) = {
    val bucket = S3(bucketName)
    val result = bucket.add(BucketFile(fileName, mimeType, byteArray, Some(PUBLIC_READ)))

    result.map { unit =>
      if (Logger.isDebugEnabled) {
        Logger.debug("File uploaded to S3")
      }
    }
    .recover {
      case S3Exception(status, code, message, originalXml) => Logger.error("Error: " + message)
    }
  }
}
