package ch.streckeisen.mycv.backend.email

import ch.streckeisen.mycv.backend.account.ApplicantAccountEntity
import groovy.text.markup.MarkupTemplateEngine
import groovy.text.markup.TemplateConfiguration
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

private const val EMAIL_TEMPLATE_BASE_PATH = "ch/streckeisen/mycv/backend/email"
private const val LOGO_URL = "https://localhost:8080/assets/mycvhub_logo.png"

@Service
class EmailTemplateService(
    @Value("\${my-cv.frontend.base-url}")
    private val baseUrl: String
) {
    private val markupTemplateEngine: MarkupTemplateEngine

    init {
        val templateConfiguration = TemplateConfiguration()
        templateConfiguration.isAutoIndent = true
        templateConfiguration.isAutoNewLine = true
        markupTemplateEngine = MarkupTemplateEngine(templateConfiguration)
    }

    fun renderAccountVerification(recipient: ApplicantAccountEntity, verificationToken: String): Result<String> {
        val params = mapOf(
            "username" to recipient.username,
            "verificationUrl" to "${baseUrl}/account/verification?id=${recipient.id}&token=${verificationToken}"
        )

        try {
            val contentTemplate =
                markupTemplateEngine.createTemplateByPath("${EMAIL_TEMPLATE_BASE_PATH}/email_verification.groovy")
            val emailContent = contentTemplate.make(params).toString()
            return renderEmail("MyCVHub: Email Verification", emailContent)
        } catch (ex: Exception) {
            return Result.failure(EmailTemplateException("Failed to render account verification template", ex))
        }
    }

    private fun renderEmail(emailTitle: String, emailContent: String): Result<String> {
        val params = mapOf(
            "emailTitle" to emailTitle,
            "emailContent" to emailContent,
            "logoUrl" to LOGO_URL
        )
        try {
            val emailTemplate =
                markupTemplateEngine.createTemplateByPath("${EMAIL_TEMPLATE_BASE_PATH}/email_template.groovy")
            val emailContent = emailTemplate.make(params).toString()
            return Result.success(emailContent)
        } catch (ex: Exception) {
            return Result.failure(EmailTemplateException("Failed to base template", ex))
        }
    }
}