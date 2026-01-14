--liquibase formatted sql

--changeset lstreckeisen:12
--validCheckSum: any

CREATE TABLE application_template_entity
(
    id                 BIGSERIAL    NOT NULL,
    name               VARCHAR(100) NOT NULL,
    cv_configuration   TEXT         NOT NULL,
    document_checklist TEXT         NULL,
    account_id         BIGINT       NOT NULL
);
ALTER TABLE application_template_entity
    ADD CONSTRAINT pk_application_template_entity PRIMARY KEY (id);
ALTER TABLE application_template_entity
    ADD CONSTRAINT fk_application_template_account FOREIGN KEY (account_id) REFERENCES applicant_account_entity (id);
ALTER TABLE application_template_entity
    ADD CONSTRAINT uq_application_template_name UNIQUE (account_id, name);