--liquibase formatted sql

--changeset lstreckeisen:14
--validCheckSum: any

ALTER TABLE applicant_account_entity
    ADD COLUMN created_at timestamp,
    ADD COLUMN last_login_at timestamp;

UPDATE applicant_account_entity a
SET created_at = events.first_signup
FROM (
    SELECT applicant_account_id, MIN(timestamp) AS first_signup
    FROM user_activity_event_entity
    WHERE event_type = 'SIGNUP'
    GROUP BY applicant_account_id
) events
WHERE a.id = events.applicant_account_id;

UPDATE applicant_account_entity a
SET last_login_at = events.last_login
FROM (
    SELECT applicant_account_id, MAX(timestamp) AS last_login
    FROM user_activity_event_entity
    WHERE event_type = 'LOGIN'
    GROUP BY applicant_account_id
) events
WHERE a.id = events.applicant_account_id;
