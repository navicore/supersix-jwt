package onextent.supersix.jwt.models.functions

import java.io.ByteArrayOutputStream
import java.time.{ZoneOffset, ZonedDateTime}
import java.util.Date

import akka.serialization.SerializerWithStringManifest
import com.sksamuel.avro4s._
import com.typesafe.scalalogging.LazyLogging
import onextent.supersix.jwt.models.{Key, KeyRequest}
import org.apache.avro.Schema
import org.apache.avro.Schema.Field

// ejs todo: get rid of all this dupe code. can't use normal generics due to avro4s macros not expanding parameterized types
object AvroSupport extends JsonSupport with LazyLogging {

  implicit object ZondedDateTimeToSchema extends ToSchema[ZonedDateTime] {
    override val schema: Schema = Schema.create(Schema.Type.STRING)
  }
  implicit object ZonedDateTimeToValue extends ToValue[ZonedDateTime] {
    override def apply(value: ZonedDateTime): String =
      get8601(new Date(value.toInstant.toEpochMilli))
  }
  implicit object ZonedDateTimeFromValue extends FromValue[ZonedDateTime] {
    override def apply(value: Any, field: Field): ZonedDateTime =
      java.time.ZonedDateTime
        .ofInstant(parse8601(value.toString).toInstant, ZoneOffset.UTC)
  }

  abstract class AvroSerializer[T] extends SerializerWithStringManifest {

    override def manifest(o: AnyRef): String = o.getClass.getName

  }

  class KeySerializer extends AvroSerializer[Key] {
    override def identifier: Int = 100001
    final val maniFest = classOf[Key].getName
    override def toBinary(o: AnyRef): Array[Byte] = {
      val output = new ByteArrayOutputStream
      val avro = AvroOutputStream.binary[Key](output)
      avro.write(o.asInstanceOf[Key])
      avro.close()
      output.toByteArray
    }
    override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef = {
      implicit val fromRec: FromRecord[Key] =
        FromRecord[Key]
      if (maniFest == manifest) {
        val is = AvroInputStream.binary[Key](bytes)
        val events = is.iterator.toList
        is.close()
        events.head
      } else
        throw new IllegalArgumentException(
          s"Unable to handle manifest $manifest, required $maniFest")
    }
  }
  class KeyRequestSerializer extends AvroSerializer[KeyRequest] {
    override def identifier: Int = 100001
    final val maniFest = classOf[KeyRequest].getName
    override def toBinary(o: AnyRef): Array[Byte] = {
      val output = new ByteArrayOutputStream
      val avro = AvroOutputStream.binary[KeyRequest](output)
      avro.write(o.asInstanceOf[KeyRequest])
      avro.close()
      output.toByteArray
    }
    override def fromBinary(bytes: Array[Byte], manifest: String): AnyRef = {
      implicit val fromRec: FromRecord[KeyRequest] =
        FromRecord[KeyRequest]
      if (maniFest == manifest) {
        val is = AvroInputStream.binary[KeyRequest](bytes)
        val events = is.iterator.toList
        is.close()
        events.head
      } else
        throw new IllegalArgumentException(
          s"Unable to handle manifest $manifest, required $maniFest")
    }
  }

}
