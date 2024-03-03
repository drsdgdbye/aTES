package infrastructure

import spray.json.{DeserializationException, JsString, JsValue, JsonFormat, deserializationError}

import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.UUID

package object rest {
  implicit object InstantFormat extends JsonFormat[Instant] {

    private val dtf = DateTimeFormatter.ISO_OFFSET_DATE_TIME

    override def read(jsv: JsValue): Instant = jsv match {
      case JsString(s) if s.endsWith("Z") => Instant.parse(s)

      // Note that Zalando JSON guidelines do NOT allow non-UTC offsets in stored data.
      case JsString(s) =>
        Instant.from(dtf.parse(s))
      case _ =>
        deserializationError(s"Unknown Instant (ISO 8601 with UTC time zone expected): $jsv")
    }

    override def write(obj: Instant): JsValue = JsString(obj.toString)
  }

  implicit object IdJsonFormat extends JsonFormat[UUID] {
    def write(id: UUID): JsString = JsString(id.toString)

    def read(value: JsValue): UUID = {
      value match {
        case JsString(uuid) => UUID.fromString(uuid)
        case _ => throw DeserializationException("Expected hexadecimal UUID string")
      }
    }
  }
}
