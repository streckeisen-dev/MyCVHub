package ch.streckeisen.mycv.backend

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TestResource {

    @GetMapping("/api/test")
    fun test(): ResponseEntity<TestResponse> {
        return ResponseEntity.ok(TestResponse("api test successful"))
    }
}

data class TestResponse(val message: String)