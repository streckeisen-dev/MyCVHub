package ch.streckeisen.mycv.backend.locale

import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.web.servlet.LocaleResolver
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver
import java.nio.charset.StandardCharsets
import java.util.Locale

@Configuration
class LocaleConfiguration {
    @Bean
    fun localeResolver(): LocaleResolver {
        val resolver = AcceptHeaderLocaleResolver()
        resolver.setDefaultLocale(Locale.ENGLISH)
        return resolver
    }

    @Bean
    fun messageSource(): MessageSource {
        val src = ResourceBundleMessageSource()
        src.setBasename("messages")
        src.setDefaultEncoding(StandardCharsets.UTF_8.name())
        return src
    }
}