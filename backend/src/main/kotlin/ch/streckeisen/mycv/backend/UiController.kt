package ch.streckeisen.mycv.backend

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping

@Controller
class UiController {
    @RequestMapping("/ui/**")
    fun forwardUiRequests(): String {
        return "forward:/index.html"
    }
}