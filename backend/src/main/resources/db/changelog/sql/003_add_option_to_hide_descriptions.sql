--liquibase formatted sql

--changeset lstreckeisen:3
--validCheckSum: any
ALTER TABLE profile_entity ADD COLUMN hide_descriptions boolean NOT NULL CONSTRAINT df_profile_hide_description DEFAULT true;
ALTER TABLE profile_entity ALTER COLUMN hide_descriptions DROP DEFAULT;