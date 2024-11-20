package ch.streckeisen.mycv.backend.email

/**
 * @param username        recipient username
 * @param verificationUrl URL of the verification link
 */

div {
    p("Hello ${username},")
    p("Thank you for signing up for MyCVHub! Please verify your email address by clicking the link below:")
    a(href: "${verificationUrl}", style: 'color: #4A90E2; text-decoration: none; font-weight: bold;', "Verify Email Address")
    p("Best regards,")
    p("The MyCVHub Team")
}