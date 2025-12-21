--liquibase formatted sql

--changeset lstreckeisen:9
--validCheckSum: any
CREATE TABLE application_entity
(
    id          BIGSERIAL    NOT NULL,
    job_title   VARCHAR(100) NOT NULL,
    company     VARCHAR(100) NOT NULL,
    status      VARCHAR(50)  NOT NULL,
    source      TEXT,
    description TEXT,
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ,
    account_id  BIGINT       NOT NULL
);
ALTER TABLE application_entity
    ADD CONSTRAINT pk_application PRIMARY KEY (id);
ALTER TABLE application_entity
    ADD CONSTRAINT pk_application_account FOREIGN KEY (account_id) REFERENCES applicant_account_entity (id);

CREATE TABLE application_history_entity
(
    id             BIGSERIAL   NOT NULL,
    source         VARCHAR(50) NOT NULL,
    target         VARCHAR(50) NOT NULL,
    comment        VARCHAR(255),
    timestamp      TIMESTAMPTZ NOT NULL,
    application_id BIGINT      NOT NULL
);
ALTER TABLE application_history_entity
    ADD CONSTRAINT pk_application_history PRIMARY KEY (id);
ALTER TABLE application_history_entity
    ADD CONSTRAINT fk_application_history_application FOREIGN KEY (application_id) REFERENCES application_entity (id);