--liquibase formatted sql

--changeset lstreckeisen:1
--validCheckSum: any

CREATE TABLE privacy_settings (
    id bigserial NOT NULL,
    is_profile_public BOOLEAN NOT NULL,
    is_email_public BOOLEAN NOT NULL,
    is_phone_public BOOLEAN NOT NULL,
    is_birthday_public BOOLEAN NOT NULL,
    is_address_public BOOLEAN NOT NULL
);

ALTER TABLE privacy_settings ADD CONSTRAINT pk_privacy_settings PRIMARY KEY (id);

ALTER TABLE applicant ADD COLUMN privacy_settings_id bigint;
ALTER TABLE applicant ADD CONSTRAINT unique_applicant_privacy_settings UNIQUE (privacy_settings_id);
ALTER TABLE applicant ADD CONSTRAINT fk_applicant_privacy_settings FOREIGN KEY (privacy_settings_id) REFERENCES privacy_settings(id);

INSERT INTO privacy_settings (is_profile_public, is_email_public, is_phone_public, is_birthday_public, is_address_public)
    SELECT a.has_public_profile, false, false, false, false
    FROM applicant a;

WITH created_privacy_settings AS (
    SELECT a.id AS applicant_id, p.id AS privacy_id
    FROM applicant a
    JOIN privacy_settings p ON a.has_public_profile = p.is_profile_public
)
UPDATE applicant
    SET privacy_settings_id = created_privacy_settings.privacy_id
FROM created_privacy_settings
WHERE applicant.id = created_privacy_settings.applicant_id;

ALTER TABLE applicant DROP COLUMN has_public_profile;

ALTER TABLE applicant ADD COLUMN alias VARCHAR(40);
ALTER TABLE applicant ADD CONSTRAINT unique_applicant_alias UNIQUE (alias);