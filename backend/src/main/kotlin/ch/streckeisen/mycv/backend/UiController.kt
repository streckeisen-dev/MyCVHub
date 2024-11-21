package ch.streckeisen.mycv.backend

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@Profile("!local")
class UiController {
    @RequestMapping("/ui/**")
    fun forwardUiRequests(): String {
        return "forward:/index.html"
    }
}