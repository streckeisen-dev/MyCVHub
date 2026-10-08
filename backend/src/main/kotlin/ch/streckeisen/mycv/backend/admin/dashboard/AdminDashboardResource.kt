package ch.streckeisen.mycv.backend.admin.dashboard

import ch.streckeisen.mycv.backend.security.annotations.RequiresAdminRole
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiresAdminRole
class AdminDashboardResource(
    private val adminDashboardService: AdminDashboardService
) {
    @GetMapping
    fun getDashboard(@RequestParam(defaultValue = "30d") range: String): ResponseEntity<AdminDashboardDto> {
        return ResponseEntity.ok(adminDashboardService.getDashboard(range))
    }
}
