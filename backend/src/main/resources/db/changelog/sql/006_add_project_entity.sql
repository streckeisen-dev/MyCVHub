--liquibase formatted sql

--changeset lstreckeisen:6
--validCheckSum: any
CREATE TABLE project_entity (
    id BIGSERIAL,
    name VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL,
    description TEXT,
    project_start date NOT NULL,
    project_end date,
    profile_id BIGINT NOT NULL
);
ALTER TABLE project_entity ADD CONSTRAINT pk_project PRIMARY KEY (id);
ALTER TABLE project_entity ADD CONSTRAINT fk_project_profile FOREIGN KEY (profile_id) REFERENCES profile_entity(id);

CREATE TABLE project_links (
    url TEXT NOT NULL,
    type VARCHAR(20) NOT NULL,
    project_id BIGINT NOT NULL
);
ALTER TABLE project_links ADD CONSTRAINT fk_project_links_project FOREIGN KEY (project_id) REFERENCES project_entity(id);