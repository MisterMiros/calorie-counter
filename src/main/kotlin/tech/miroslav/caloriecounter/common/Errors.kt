package tech.miroslav.caloriecounter.common

import org.springframework.http.HttpStatus

// Simple exception hierarchy for standardized API errors
open class ApiException(val status: HttpStatus, message: String) : RuntimeException(message)
class BadRequestException(message: String) : ApiException(HttpStatus.BAD_REQUEST, message)
class UnauthorizedException(message: String) : ApiException(HttpStatus.UNAUTHORIZED, message)
class ForbiddenException(message: String) : ApiException(HttpStatus.FORBIDDEN, message)
class NotFoundException(message: String) : ApiException(HttpStatus.NOT_FOUND, message)
class ConflictException(message: String) : ApiException(HttpStatus.CONFLICT, message)

// Error response envelope
data class ApiError(
    val status: Int,
    val error: String,
    val message: String?,
    val path: String? = null,
    val timestamp: java.time.OffsetDateTime = java.time.OffsetDateTime.now()
)
