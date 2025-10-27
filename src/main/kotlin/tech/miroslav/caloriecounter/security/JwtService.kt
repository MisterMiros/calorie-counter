package tech.miroslav.caloriecounter.security

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*

/**
 * Issues and verifies JWT tokens. No expiration per specification (iat only).
 */
@Component
class JwtService(
    @Value("\${app.security.jwt.secret}") private val secret: String,
    @Value("\${app.security.jwt.issuer}") private val issuer: String
) {
    private val algorithm: Algorithm by lazy { Algorithm.HMAC256(secret) }
    private val verifier: JWTVerifier by lazy { JWT.require(algorithm).withIssuer(issuer).build() }

    fun issueToken(
        username: String,
        authUserId: UUID,
        appUserId: UUID,
        isAdmin: Boolean
    ): String {
        val now = Instant.now()
        return JWT.create()
            .withIssuer(issuer)
            .withIssuedAt(Date.from(now))
            .withSubject(username)
            .withClaim("auth_user_id", authUserId.toString())
            .withClaim("app_user_id", appUserId.toString())
            .withClaim("is_admin", isAdmin)
            .sign(algorithm)
    }

    fun verify(token: String): JwtUserClaims {
        try {
            val decoded = verifier.verify(token)
            val username = decoded.subject
            val authUserId = UUID.fromString(decoded.getClaim("auth_user_id").asString())
            val appUserId = UUID.fromString(decoded.getClaim("app_user_id").asString())
            val isAdmin = decoded.getClaim("is_admin").asBoolean() ?: false
            val issuedAt = decoded.issuedAt?.toInstant()
            return JwtUserClaims(username, authUserId, appUserId, isAdmin, issuedAt)
        } catch (ex: JWTVerificationException) {
            throw ex
        }
    }
}

data class JwtUserClaims(
    val username: String,
    val authUserId: UUID,
    val appUserId: UUID,
    val isAdmin: Boolean,
    val issuedAt: Instant?
)
