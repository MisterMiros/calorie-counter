package tech.miroslav.caloriecounter.auth.dto

import jakarta.validation.constraints.NotBlank

data class RegisterRequest(
    @field:NotBlank
    val username: String,
    @field:NotBlank
    val password: String,
    val timezone: String? = null
)

data class LoginRequest(
    @field:NotBlank
    val username: String,
    @field:NotBlank
    val password: String
)

data class AuthResponse(
    val token: String,
    val authUserId: String,
    val appUserId: String,
    val isAdmin: Boolean
)
