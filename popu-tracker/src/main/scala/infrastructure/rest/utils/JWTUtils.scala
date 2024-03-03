package infrastructure.rest.utils

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.typesafe.config.Config

import java.security.KeyFactory
import java.security.interfaces.{RSAPrivateKey, RSAPublicKey}
import java.security.spec.{PKCS8EncodedKeySpec, X509EncodedKeySpec}
import java.util.Base64
import scala.util.Try

/**
 * Утилиты для верификации JWT
 */
class JWTUtils(jwtConfig: Config) {
  /** Алгоритм подписания и верификации */
  private lazy val algorithm: Algorithm = {
    val factory = KeyFactory.getInstance("RSA")
    val publicKeySpec = new X509EncodedKeySpec(Base64.getDecoder.decode(jwtConfig.getString("public-key").getBytes))
    val privateKeySpec = new PKCS8EncodedKeySpec(Base64.getDecoder.decode(jwtConfig.getString("private-key").getBytes))

    Algorithm.RSA256(factory.generatePublic(publicKeySpec).asInstanceOf[RSAPublicKey], factory.generatePrivate(privateKeySpec).asInstanceOf[RSAPrivateKey])
  }

  /**
   * Верификация JWT
   *
   * @param token JWT
   * @return Валидный ли токен?
   */
  def verifyJWT(token: String): Try[DecodedJWT] = {
    val verifier = JWT.require(algorithm).build()
    Try(verifier.verify(token))
  }

  /**
   * Создание подписанного JWT
   *
   * @param claims Полезная нагрузка токена
   * @return JWT
   */
  def createSignedJWT(claims: (String, String)*): String = {
    val jwtBuilder = JWT.create()
    claims.foreach { case (name, value) =>
      jwtBuilder.withClaim(name, value)
    }
    jwtBuilder.sign(algorithm)
  }
}
