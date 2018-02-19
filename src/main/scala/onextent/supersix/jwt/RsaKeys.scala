package onextent.supersix.jwt

object RsaKeys {

  case class KeyInfo(publicKey: String, privateKey: String, keyId: String)

  def apply(): KeyInfo = {

    KeyInfo("", "", "")
  }
}
