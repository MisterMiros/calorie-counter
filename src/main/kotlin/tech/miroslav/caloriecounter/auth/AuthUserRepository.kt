package tech.miroslav.caloriecounter.auth

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface AuthUserRepository : JpaRepository<AuthUser, UUID> {
    fun findByUsername(username: String): AuthUser?
    fun existsByUsername(username: String): Boolean
}