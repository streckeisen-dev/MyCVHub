package ch.streckeisen.mycv.backend

import ch.streckeisen.mycv.backend.security.annotations.PublicApi
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@Profile("!local")
class UiController {
    @RequestMapping("/ui/**")
    @PublicApi
    fun forwardUiRequests(): String {
        return "forward:/index.html"
    }
}