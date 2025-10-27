package tech.miroslav.caloriecounter.security

import org.springframework.security.core.context.SecurityContextHolder

object CurrentUser {
    fun principalOrNull(): JwtPrincipal? =
        SecurityContextHolder.getContext().authentication?.principal as? JwtPrincipal
}
