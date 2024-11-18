package ch.streckeisen.mycv.backend.publicapi.country

import ch.streckeisen.mycv.backend.security.annotations.PublicApi
import com.google.i18n.phonenumbers.PhoneNumberUtil
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.Locale

@PublicApi
@RestController
@RequestMapping("/api/public/countries")
class CountryResource {
    @GetMapping
    fun getCountries(): ResponseEntity<List<CountryDto>> {
        return ResponseEntity.ok(
            PhoneNumberUtil.getInstance().supportedRegions.map { countryCode ->
                val locale = Locale.Builder()
                    .setLanguage(LocaleContextHolder.getLocale().language)
                    .setRegion(countryCode)
                    .build()
                CountryDto(locale.getDisplayCountry(locale), countryCode)
            }.sortedBy { it.name }
        )
    }
}

