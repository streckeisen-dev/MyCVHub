package ch.streckeisen.mycv.backend

import ch.streckeisen.mycv.backend.exceptions.ResultNotFoundException
import ch.streckeisen.mycv.backend.exceptions.ValidationException
import com.fasterxml.jackson.core.JacksonException
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.security.SignatureException
import org.postgresql.util.PSQLException
import org.springframework.dao.DataAccessException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.AccountStatusException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.AuthenticationException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class ControllerAdvice : ResponseEntityExceptionHandler() {
    @ExceptionHandler
    fun handleValidationError(ex: ValidationException): ResponseEntity<ErrorDto> {
        return ResponseEntity.badRequest().body(ErrorDto(ex.message!!, ex.errors))
    }

    @ExceptionHandler
    fun handleDatabaseError(ex: DataAccessException): ResponseEntity<ErrorDto> {
        logger.error(ex.message, ex)
        return ResponseEntity.badRequest()
            .body(ErrorDto("A database error occurred. Please contact the administrator"))
    }

    @ExceptionHandler
    fun handleExpiredJwtException(ex: ExpiredJwtException): ResponseEntity<ErrorDto> {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ErrorDto("Expired JWT token"))
    }

    @ExceptionHandler
    fun handleBadCredentialsException(ex: BadCredentialsException): ResponseEntity<ErrorDto> {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ErrorDto("Username and/or password are incorrect"))
    }

    @ExceptionHandler
    fun handleAccountStatusException(ex: AccountStatusException): ResponseEntity<ErrorDto> {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ErrorDto("Account is locked"))
    }

    @ExceptionHandler
    fun handleSignatureException(ex: SignatureException): ResponseEntity<ErrorDto> {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ErrorDto("Invalid JWT signature"))
    }

    @ExceptionHandler
    fun handleResultNotFoundException(ex: ResultNotFoundException): ResponseEntity<ErrorDto> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ErrorDto(ex.message!!))
    }

    @ExceptionHandler
    fun handlePsqlException(ex: PSQLException): ResponseEntity<ErrorDto> {
        logger.error(ex.message, ex)
        return ResponseEntity.internalServerError()
            .body(ErrorDto("Error during data processing"))
    }

    @ExceptionHandler
    fun handleJacksonException(ex: JacksonException): ResponseEntity<ErrorDto> {
        logger.error(ex.message, ex)
        return ResponseEntity.badRequest().body(ErrorDto(ex.message!!))
    }

    @ExceptionHandler
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ResponseEntity<ErrorDto> {
        return ResponseEntity.badRequest().body(ErrorDto(ex.message!!))
    }

    @ExceptionHandler
    fun handleIllegalStateException(ex: IllegalStateException): ResponseEntity<ErrorDto> {
        return ResponseEntity.internalServerError().body(ErrorDto(ex.message!!))
    }

    @ExceptionHandler
    fun handleException(ex: Exception): ResponseEntity<ErrorDto> {
        logger.error(ex.message, ex)
        return ResponseEntity.internalServerError().body(ErrorDto("An unknown error occurred"))
    }
}