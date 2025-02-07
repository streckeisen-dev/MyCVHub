package ch.streckeisen.mycv.backend.email

/**
 * @param emailTitle    The title of the email to display in the header and subject.
 * @param emailContent  The main content/body of the email.
 * @param logoUrl       The URL to the logo
 */
doctype html
html {
    head {
        meta(charset: 'UTF-8')
        title("${emailTitle}")
    }
    body(style: 'font-family: Arial, sans-serif; background-color: #f5f6fa; padding: 0; margin: 0;') {
        div(style: 'max-width: 600px; margin: auto; padding: 20px; background-color: #ffffff; border-radius: 5px; box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);') {
            // Logo and Title Section
            div(style: 'text-align: center; margin-bottom: 20px; background-color: #010409;') {
                img(src: "${logoUrl}", alt: "MyCVHub Logo", style: 'height: 50px; width: auto;')
            }
            
            // Main Content Section
            h1(style: 'color: #333333; font-size: 22px;', "${emailTitle}")
            // Render the emailContent as raw HTML, allowing nested templates or complex structure
            yieldUnescaped emailContent

            // Footer Section
            hr(style: 'border: none; height: 1px; background-color: #eeeeee; margin: 20px 0;')
            footer(style: 'text-align: center; color: #888888; font-size: 12px;') {
                p(style: 'margin: 0;', '© 2024 MyCVHub, Inc. All rights reserved.')
                a(href: 'https://mycvhub.ch/ui/privacy', 'Privacy Policy')
            }
        }
    }
}