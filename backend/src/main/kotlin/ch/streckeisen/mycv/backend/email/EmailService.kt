package ch.streckeisen.mycv.backend.email

import ch.streckeisen.mycv.backend.account.ApplicantAccountEntity
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.MailException
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger { }

@Service
class EmailService(
    private val mailSender: JavaMailSender,
    private val emailTemplateService: EmailTemplateService,
    @Value("\${spring.mail.username}")
    private val senderEmail: String
) {

    fun sendAccountVerificationEmail(recipient: ApplicantAccountEntity, verificationToken: String): Result<Unit> {
        if (recipient.accountDetails == null) {
            return Result.failure(IllegalStateException("Account has no email address. Cannot send account verification email."))
        }
        logger.debug { "Sending account verification email to ${recipient.accountDetails.email}..." }
        val message = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true)

        val body = emailTemplateService.renderAccountVerification(recipient, verificationToken)
            .getOrElse { return Result.failure(it) }

        try {
            helper.setFrom(senderEmail)
            helper.setTo(recipient.accountDetails.email)
            helper.setSubject("MyCVHub: Verify you email address")
            helper.setText(body, true)
            mailSender.send(message)
            logger.info { "Sent account verification email" }
            return Result.success(Unit)
        } catch (ex: MailException) {
            return Result.failure(ex)
        }
    }
}