--liquibase formatted sql

--changeset lstreckeisen:1

CREATE TABLE applicant (
    id numeric(18, 0) NOT NULL
);

ALTER TABLE applicant ADD CONSTRAINT pk_applicant PRIMARY KEY (id);