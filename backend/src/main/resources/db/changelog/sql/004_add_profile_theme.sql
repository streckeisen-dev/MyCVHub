--liquibase formatted sql

--changeset lstreckeisen:4
--validCheckSum: any
CREATE TABLE profile_theme_entity
(
    id bigserial NOT NULL,
    background_color varchar(7) NOT NULL,
    surface_color varchar(7) NOT NULL,
    text_color varchar(7) NOT NULL,
    profile_id bigint NOT NULL
);
ALTER TABLE profile_theme_entity ADD CONSTRAINT fk_profile_theme FOREIGN KEY (profile_id) REFERENCES profile_entity(id);