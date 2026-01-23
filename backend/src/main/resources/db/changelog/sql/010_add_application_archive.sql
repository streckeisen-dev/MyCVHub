--liquibase formatted sql

--changeset lstreckeisen:10
--validCheckSum: any
ALTER TABLE application_entity ADD COLUMN is_archived BOOLEAN;

UPDATE application_entity
SET is_archived = false;
ALTER TABLE application_entity ALTER COLUMN is_archived SET NOT NULL;