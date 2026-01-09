--liquibase formatted sql

--changeset lstreckeisen:11
--validCheckSum: any

CREATE TABLE application_template_entity
(
    id                   BIGSERIAL    NOT NULL,
    name                 VARCHAR(100) NOT NULL,
    cv_configuration     JSONB        NOT NULL,
    document_checklist   JSONB        NOT NULL,
    applicant_account_id BIGINT       NOT NULL
);
ALTER TABLE application_template_entity
    ADD CONSTRAINT pk_application_template_entity PRIMARY KEY (id);
ALTER TABLE application_template_entity
    ADD CONSTRAINT fk_application_template_account FOREIGN KEY (applicant_account_id) REFERENCES applicant_account_entity (id);