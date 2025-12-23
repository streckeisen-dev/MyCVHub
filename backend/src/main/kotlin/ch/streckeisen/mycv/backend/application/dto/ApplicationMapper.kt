package ch.streckeisen.mycv.backend.application.dto

import ch.streckeisen.mycv.backend.application.ApplicationEntity
import ch.streckeisen.mycv.backend.application.ApplicationHistoryEntity
import ch.streckeisen.mycv.backend.application.ApplicationStatus
import ch.streckeisen.mycv.backend.application.ApplicationTransition
import ch.streckeisen.mycv.backend.locale.MessagesService

fun ApplicationEntity.toSearchDto(messagesService: MessagesService): ApplicationSearchDto = ApplicationSearchDto(
    id = id!!,
    jobTitle = jobTitle,
    company = company,
    status = status.toDto(messagesService),
    source = source,
    description = description,
    createdAt = createdAt,
    updatedAt = updatedAt,
    history = history.map { it.toDto(messagesService) }
)

fun ApplicationEntity.toDetailsDto(
    transitions: List<ApplicationTransition>,
    messagesService: MessagesService
): ApplicationDetailsDto = ApplicationDetailsDto(
    id = id!!,
    jobTitle = jobTitle,
    company = company,
    status = status.toDto(messagesService),
    source = source,
    description = description,
    createdAt = createdAt,
    updatedAt = updatedAt,
    history = history.map { it.toDto(messagesService) },
    transitions = transitions.map { it.toDto(messagesService) }
)

fun ApplicationHistoryEntity.toDto(messagesService: MessagesService): ApplicationHistoryDto = ApplicationHistoryDto(
    id = id!!,
    from = source.toDto(messagesService),
    to = target.toDto(messagesService),
    comment = comment,
    timestamp = timestamp
)

fun ApplicationTransition.toDto(messagesService: MessagesService): ApplicationTransitionDto = ApplicationTransitionDto(
    id = id,
    label = messagesService.getMessage(labelKey)
)

fun ApplicationStatus.toDto(messagesService: MessagesService): ApplicationStatusDto = ApplicationStatusDto(
    name,
    messagesService.getMessage(labelKey)
)