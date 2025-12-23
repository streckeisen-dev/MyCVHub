package ch.streckeisen.mycv.backend.locale

import ch.streckeisen.mycv.backend.security.getMyCvPrincipalOrNull
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver
import java.util.Locale

class AccountLocaleResolver : AcceptHeaderLocaleResolver() {
    override fun resolveLocale(request: HttpServletRequest): Locale {
        val principal = SecurityContextHolder.getContext().getMyCvPrincipalOrNull()
        if (principal != null && principal.locale != null) {
            return principal.locale
        }
        return super.resolveLocale(request)
    }
}