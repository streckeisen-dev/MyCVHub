package ch.streckeisen.mycv.backend.application

import ch.streckeisen.mycv.backend.locale.MYCV_KEY_PREFIX

enum class ApplicationStatus(
    val labelKey: String
) {
    UNSENT("$MYCV_KEY_PREFIX.application.status.unsent"),
    WAITING_FOR_FIRST_RESPONSE("$MYCV_KEY_PREFIX.application.status.waitingForFirstResponse"),
    REJECTED("$MYCV_KEY_PREFIX.application.status.rejected"),
    INTERVIEW_SCHEDULED("$MYCV_KEY_PREFIX.application.status.interviewScheduled"),
    WAITING_FOR_INTERVIEW_FEEDBACK("$MYCV_KEY_PREFIX.application.status.waitingForInterviewFeedback"),
    OFFER_RECEIVED("$MYCV_KEY_PREFIX.application.status.offerReceived"),
    OFFER_DECLINED("$MYCV_KEY_PREFIX.application.status.offerDeclined"),
    HIRED("$MYCV_KEY_PREFIX.application.status.hired"),
    WITHDRAWN("$MYCV_KEY_PREFIX.application.status.withdrawn")
}