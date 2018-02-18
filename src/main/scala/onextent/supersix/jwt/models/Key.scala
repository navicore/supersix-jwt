package onextent.supersix.jwt.models

import java.time.{ZoneOffset, ZonedDateTime}
import java.util.UUID

final case class Key(name: String,
                     email: String,
                     scopes: List[String],
                     verified: Option[ZonedDateTime],
                     approved: Option[ZonedDateTime],
                     created: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC),
                     clientId: UUID = UUID.randomUUID(),
                     clientSecret: UUID = UUID.randomUUID())

object MkKey {

  def apply(req: KeyRequest): Key =
    Key(req.name, req.email, req.scopes, None, None)

}

final case class KeyRequest(name: String, email: String, scopes: List[String])
final case class CreateKey(key: Key)
final case class UpdateKey(key: Key)
final case class DeleteKey(clientId: UUID)
final case class KeyAlreadyExists(clientId: UUID)
final case class KeyNotFound(clientId: UUID)


final case class Jwt(json: String)
final case class JwtRequest(clientId: UUID, clientSecret: UUID)

