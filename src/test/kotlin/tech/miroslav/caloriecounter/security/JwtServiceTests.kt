package tech.miroslav.caloriecounter.security

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

class JwtServiceTests {

    @Test
    fun `issue and verify token round-trip`() {
        val service = JwtService(secret = "test-secret", issuer = "test-issuer")
        val authId = UUID.randomUUID()
        val appId = UUID.randomUUID()
        val token = service.issueToken(
            username = "alice",
            authUserId = authId,
            appUserId = appId,
            isAdmin = true
        )
        val claims = service.verify(token)
        assertThat(claims.username).isEqualTo("alice")
        assertThat(claims.authUserId).isEqualTo(authId)
        assertThat(claims.appUserId).isEqualTo(appId)
        assertThat(claims.isAdmin).isTrue()
        assertThat(claims.issuedAt).isNotNull()
    }
}
