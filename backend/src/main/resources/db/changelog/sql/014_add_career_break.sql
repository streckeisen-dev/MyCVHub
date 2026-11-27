--liquibase formatted sql

--changeset lstreckeisen:14
ALTER TABLE work_experience_entity
    ADD COLUMN career_break boolean NOT NULL DEFAULT false;
