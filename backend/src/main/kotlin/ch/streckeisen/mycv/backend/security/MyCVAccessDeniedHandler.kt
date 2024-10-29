package ch.streckeisen.mycv.backend.security

import ch.streckeisen.mycv.backend.locale.MYCV_KEY_PREFIX
import ch.streckeisen.mycv.backend.locale.MessagesService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler

private const val ACCESS_DENIED_ERROR_KEY = "${MYCV_KEY_PREFIX}.exceptions.accessDenied"

class MyCVAccessDeniedHandler(
    private val messagesService: MessagesService
) : AccessDeniedHandler {
    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException
    ) {
        response.sendError(HttpServletResponse.SC_FORBIDDEN, messagesService.getMessage(ACCESS_DENIED_ERROR_KEY))
    }
}