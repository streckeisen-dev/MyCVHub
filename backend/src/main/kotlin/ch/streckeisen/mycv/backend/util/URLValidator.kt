package ch.streckeisen.mycv.backend.util

import java.net.MalformedURLException
import java.net.URI
import java.net.URISyntaxException

fun isUrlValid(url: String): Boolean {
    try {
        URI(url).toURL()
    } catch (ex: Exception) {
        when (ex) {
            is URISyntaxException, is MalformedURLException, is IllegalArgumentException -> {
                return false
            }

            else -> throw ex
        }
    }
    return true
}