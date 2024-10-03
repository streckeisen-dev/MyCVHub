package ch.streckeisen.mycv.backend

import ch.streckeisen.mycv.backend.exceptions.ResultNotFoundException
import ch.streckeisen.mycv.backend.exceptions.ValidationException
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.security.SignatureException
import org.postgresql.util.PSQLException
import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.AccountStatusException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class ControllerAdvice : ResponseEntityExceptionHandler() {
    //private val logger = LoggerFactory.getLogger(ControllerAdvice::class.java)

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
    fun handleAccessDeniedException(ex: AccessDeniedException): ResponseEntity<ErrorDto> {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ErrorDto("You are not authorized to access this resource"))
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
}