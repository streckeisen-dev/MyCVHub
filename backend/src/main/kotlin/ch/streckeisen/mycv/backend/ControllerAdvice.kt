package ch.streckeisen.mycv.backend

import ch.streckeisen.mycv.backend.cv.profile.picture.ProfilePictureStorageException
import ch.streckeisen.mycv.backend.exceptions.LocalizedException
import ch.streckeisen.mycv.backend.exceptions.ValidationException
import ch.streckeisen.mycv.backend.locale.MYCV_KEY_PREFIX
import ch.streckeisen.mycv.backend.locale.MessagesService
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
import tools.jackson.core.JacksonException

private const val EXCEPTION_ERROR_KEY_PREFIX = "${MYCV_KEY_PREFIX}.exceptions"
private const val DATA_ACCESS_ERROR_KEY = "${EXCEPTION_ERROR_KEY_PREFIX}.db"
private const val EXPIRED_TOKEN_ERROR_KEY = "${EXCEPTION_ERROR_KEY_PREFIX}.expiredJwt"
private const val BAD_CREDENTIALS_ERROR_KEY = "${EXCEPTION_ERROR_KEY_PREFIX}.badCredentials"
private const val LOCKED_ACCOUNT_ERROR_KEY = "${EXCEPTION_ERROR_KEY_PREFIX}.lockedAccount"
private const val ACCESS_DENIED_ERROR_KEY = "${EXCEPTION_ERROR_KEY_PREFIX}.accessDenied"
private const val INVALID_JWT_SIGNATURE_ERROR_KEY = "${EXCEPTION_ERROR_KEY_PREFIX}.invalidJwtSignature"
private const val PSQL_ERROR_KEY = "${EXCEPTION_ERROR_KEY_PREFIX}.psql"
private const val REQUEST_FORMAT_ERROR_KEY = "${EXCEPTION_ERROR_KEY_PREFIX}.requestFormat"
private const val PROFILE_PICTURE_ERROR_KEY = "${EXCEPTION_ERROR_KEY_PREFIX}.profilePictureStorage"
private const val UNAUTHORIZED_ERROR_KEY = "${EXCEPTION_ERROR_KEY_PREFIX}.unauthorized"
private const val GENERIC_ERROR_KEY = "${EXCEPTION_ERROR_KEY_PREFIX}.generic"

@ControllerAdvice
class ControllerAdvice(
    private val messagesService: MessagesService
) : ResponseEntityExceptionHandler() {
    @ExceptionHandler
    fun handleValidationError(ex: ValidationException): ResponseEntity<ErrorDto> {
        return ResponseEntity.badRequest().body(ErrorDto(ex.message!!, ex.errors))
    }

    @ExceptionHandler
    fun handleDatabaseError(ex: DataAccessException): ResponseEntity<ErrorDto> {
        logger.error(ex.message, ex)
        return ResponseEntity.badRequest()
            .body(ErrorDto(messagesService.getMessage(DATA_ACCESS_ERROR_KEY)))
    }

    @ExceptionHandler
    fun handleExpiredJwtException(ex: ExpiredJwtException): ResponseEntity<ErrorDto> {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ErrorDto(messagesService.getMessage(EXPIRED_TOKEN_ERROR_KEY)))
    }

    @ExceptionHandler
    fun handleBadCredentialsException(ex: BadCredentialsException): ResponseEntity<ErrorDto> {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ErrorDto(messagesService.getMessage(BAD_CREDENTIALS_ERROR_KEY)))
    }

    @ExceptionHandler
    fun handleAccountStatusException(ex: AccountStatusException): ResponseEntity<ErrorDto> {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ErrorDto(messagesService.getMessage(LOCKED_ACCOUNT_ERROR_KEY)))
    }

    @ExceptionHandler
    fun handleAuthenticationException(ex: AuthenticationException): ResponseEntity<ErrorDto> {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ErrorDto(messagesService.getMessage(UNAUTHORIZED_ERROR_KEY)))
    }

    @ExceptionHandler
    fun handleAccessDeniedException(ex: AccessDeniedException): ResponseEntity<ErrorDto> {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ErrorDto(messagesService.getMessage(ACCESS_DENIED_ERROR_KEY)))
    }

    @ExceptionHandler
    fun handleSignatureException(ex: SignatureException): ResponseEntity<ErrorDto> {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ErrorDto(messagesService.getMessage(INVALID_JWT_SIGNATURE_ERROR_KEY)))
    }

    @ExceptionHandler
    fun handlePsqlException(ex: PSQLException): ResponseEntity<ErrorDto> {
        logger.error(ex.message, ex)
        return ResponseEntity.internalServerError()
            .body(ErrorDto(messagesService.getMessage(PSQL_ERROR_KEY)))
    }

    @ExceptionHandler
    fun handleJacksonException(ex: JacksonException): ResponseEntity<ErrorDto> {
        logger.error(ex.message, ex)
        return ResponseEntity.badRequest().body(ErrorDto(messagesService.getMessage(REQUEST_FORMAT_ERROR_KEY)))
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
    fun handleProfilePictureStorageException(ex: ProfilePictureStorageException): ResponseEntity<ErrorDto> {
        return ResponseEntity.internalServerError()
            .body(ErrorDto(messagesService.getMessage(PROFILE_PICTURE_ERROR_KEY)))
    }

    @ExceptionHandler
    fun handleLocalizedException(ex: LocalizedException): ResponseEntity<ErrorDto> {
        return ResponseEntity.badRequest().body(ErrorDto(messagesService.getMessage(ex.messageKey, *ex.args)))
    }

    @ExceptionHandler
    fun handleException(ex: Exception): ResponseEntity<ErrorDto> {
        logger.error(ex.message, ex)
        return ResponseEntity.internalServerError().body(ErrorDto(messagesService.getMessage(GENERIC_ERROR_KEY)))
    }
}