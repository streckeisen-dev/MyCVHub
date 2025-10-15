--liquibase formatted sql

--changeset lstreckeisen:8
--validCheckSum: any
ALTER TABLE account_details_entity ADD COLUMN language VARCHAR(2);

UPDATE account_details_entity SET language = 'en';

ALTER TABLE account_details_entity ALTER COLUMN language SET NOT NULL;