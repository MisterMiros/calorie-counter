package tech.miroslav.caloriecounter.auth

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import tech.miroslav.caloriecounter.auth.dto.AuthResponse
import tech.miroslav.caloriecounter.auth.dto.LoginRequest
import tech.miroslav.caloriecounter.auth.dto.RegisterRequest

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    fun register(@RequestBody @Valid req: RegisterRequest): ResponseEntity<Void> {
        authService.register(req)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @PostMapping("/login")
    @Operation(summary = "Login and receive a JWT token")
    fun login(@RequestBody @Valid req: LoginRequest): ResponseEntity<AuthResponse> {
        val resp = authService.login(req)
        return ResponseEntity.ok(resp)
    }
}
