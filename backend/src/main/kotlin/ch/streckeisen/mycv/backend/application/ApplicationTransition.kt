package ch.streckeisen.mycv.backend.application

import ch.streckeisen.mycv.backend.locale.MYCV_KEY_PREFIX

enum class ApplicationTransition(
    val id: Int,
    val from: List<ApplicationStatus>,
    val to: ApplicationStatus,
    val labelKey: String
) {
    SENT_APPLICATION(
        id = 1,
        from = listOf(ApplicationStatus.UNSENT),
        to = ApplicationStatus.WAITING_FOR_FIRST_RESPONSE,
        labelKey = "$MYCV_KEY_PREFIX.application.transition.sent"
    ),
    REJECTED(
        id = 2,
        from = listOf(
            ApplicationStatus.WAITING_FOR_FIRST_RESPONSE,
            ApplicationStatus.WAITING_FOR_INTERVIEW_FEEDBACK
        ),
        to = ApplicationStatus.REJECTED,
        labelKey = "$MYCV_KEY_PREFIX.application.transition.rejected"
    ),
    SCHEDULED_INTERVIEW(
        id = 3,
        from = listOf(
            ApplicationStatus.WAITING_FOR_FIRST_RESPONSE,
            ApplicationStatus.WAITING_FOR_INTERVIEW_FEEDBACK
        ),
        to = ApplicationStatus.INTERVIEW_SCHEDULED,
        labelKey = "$MYCV_KEY_PREFIX.application.transition.interviewScheduled"
    ),
    INTERVIEW_ENDED(
        id = 4,
        from = listOf(ApplicationStatus.INTERVIEW_SCHEDULED),
        to = ApplicationStatus.WAITING_FOR_INTERVIEW_FEEDBACK,
        labelKey = "$MYCV_KEY_PREFIX.application.transition.interviewEnded"
    ),
    OFFER_RECEIVED(
        id = 5,
        from = listOf(ApplicationStatus.WAITING_FOR_INTERVIEW_FEEDBACK),
        to = ApplicationStatus.OFFER_RECEIVED,
        labelKey = "$MYCV_KEY_PREFIX.application.transition.offerReceived"
    ),
    OFFER_ACCEPTED(
        id = 6,
        from = listOf(ApplicationStatus.OFFER_RECEIVED),
        to = ApplicationStatus.HIRED,
        labelKey = "$MYCV_KEY_PREFIX.application.transition.offerAccepted"
    ),
    OFFER_DECLINED(
        id = 7,
        from = listOf(ApplicationStatus.OFFER_RECEIVED),
        to = ApplicationStatus.OFFER_DECLINED,
        labelKey = "$MYCV_KEY_PREFIX.application.transition.offerDeclined"
    ),
    WITHDRAWN(
        id = 8,
        from = listOf(
            ApplicationStatus.UNSENT,
            ApplicationStatus.WAITING_FOR_FIRST_RESPONSE,
            ApplicationStatus.INTERVIEW_SCHEDULED,
            ApplicationStatus.WAITING_FOR_INTERVIEW_FEEDBACK
        ),
        to = ApplicationStatus.WITHDRAWN,
        labelKey = "$MYCV_KEY_PREFIX.application.transition.withdrawn"
    )
}