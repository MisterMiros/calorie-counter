package tech.miroslav.caloriecounter.user

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface AppUserRepository : JpaRepository<AppUser, UUID> {
    fun findByAuthUserId(authUserId: UUID): AppUser?
}