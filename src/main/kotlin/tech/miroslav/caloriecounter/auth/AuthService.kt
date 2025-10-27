package tech.miroslav.caloriecounter.auth

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tech.miroslav.caloriecounter.auth.dto.AuthResponse
import tech.miroslav.caloriecounter.auth.dto.LoginRequest
import tech.miroslav.caloriecounter.auth.dto.RegisterRequest
import tech.miroslav.caloriecounter.common.BadRequestException
import tech.miroslav.caloriecounter.common.ConflictException
import tech.miroslav.caloriecounter.common.UnauthorizedException
import tech.miroslav.caloriecounter.security.JwtService
import tech.miroslav.caloriecounter.user.AppUser
import tech.miroslav.caloriecounter.user.AppUserRepository
import java.time.ZoneId

@Service
class AuthService(
    private val authUserRepository: AuthUserRepository,
    private val appUserRepository: AppUserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService
) {

    @Transactional
    fun register(req: RegisterRequest) {
        val username = req.username.trim()
        if (authUserRepository.existsByUsername(username)) {
            throw ConflictException("Username already exists")
        }
        // Validate timezone if provided
        val tz = req.timezone?.trim()?.ifBlank { null }
        if (tz != null) {
            try { ZoneId.of(tz) } catch (ex: Exception) { throw BadRequestException("Invalid timezone: $tz") }
        }
        val auth = AuthUser(
            username = username,
            passwordHash = passwordEncoder.encode(req.password),
            enabled = true,
            isAdmin = false,
            isSystem = false
        )
        val savedAuth = authUserRepository.save(auth)
        val appUser = AppUser(
            authUserId = savedAuth.id,
            activityLevel = "SEDENTARY",
            timezone = tz ?: "UTC"
        )
        appUserRepository.save(appUser)
    }

    @Transactional(readOnly = true)
    fun login(req: LoginRequest): AuthResponse {
        val user = authUserRepository.findByUsername(req.username.trim())
            ?: throw UnauthorizedException("Invalid credentials")
        if (!user.enabled || user.isSystem) throw UnauthorizedException("Invalid credentials")
        if (!passwordEncoder.matches(req.password, user.passwordHash)) {
            throw UnauthorizedException("Invalid credentials")
        }
        val app = appUserRepository.findByAuthUserId(user.id)
            ?: throw UnauthorizedException("Invalid credentials")
        val token = jwtService.issueToken(user.username, user.id, app.id, user.isAdmin)
        return AuthResponse(
            token = token,
            authUserId = user.id.toString(),
            appUserId = app.id.toString(),
            isAdmin = user.isAdmin
        )
    }
}
