--liquibase formatted sql

--changeset lstreckeisen:1
--validCheckSum: any

CREATE TABLE applicant_account_entity
(
    id           bigserial     NOT NULL,
    firstname    varchar(50)   NOT NULL,
    lastname     varchar(50)   NOT NULL,
    email        varchar(100)  NOT NULL,
    phone        varchar(30),
    birthday     date          NOT NULL,
    street       varchar(100)  NOT NULL,
    house_number varchar(10),
    postcode     varchar(15)   NOT NULL,
    city         varchar(100)  NOT NULL,
    country      character(2)  NOT NULL,
    password     character(60) NOT NULL
);

ALTER TABLE applicant_account_entity
    ADD CONSTRAINT pk_applicant PRIMARY KEY (id);
ALTER TABLE applicant_account_entity
    ADD CONSTRAINT unique_applicant_email UNIQUE (email);

CREATE TABLE profile_entity
(
    id                bigserial    NOT NULL,
    alias             varchar(40)  NOT NULL,
    job_title         varchar(100) NOT NULL,
    about_me          text,
    is_profile_public BOOLEAN      NOT NULL,
    is_email_public   BOOLEAN      NOT NULL,
    is_phone_public   BOOLEAN      NOT NULL,
    is_address_public BOOLEAN      NOT NULL,
    account_id        bigint       NOT NULL
);
ALTER TABLE profile_entity
    ADD CONSTRAINT pk_profile PRIMARY KEY (id);
ALTER TABLE profile_entity
    ADD CONSTRAINT unique_profile_alias UNIQUE (alias);
ALTER TABLE profile_entity
    ADD CONSTRAINT fk_profile_account FOREIGN KEY (account_id) REFERENCES applicant_account_entity(id);

CREATE TABLE work_experience_entity
(
    id             bigserial    NOT NULL,
    job_title      varchar(100) NOT NULL,
    company        varchar(100) NOT NULL,
    position_start date         NOT NULL,
    position_end   date,
    location       varchar(100) NOT NULL,
    description    text,
    profile_id     bigint       NOT NULL
);
ALTER TABLE work_experience_entity
    ADD CONSTRAINT pk_work_experience PRIMARY KEY (id);
ALTER TABLE work_experience_entity
    ADD CONSTRAINT fk_work_experience_applicant FOREIGN KEY (profile_id) REFERENCES profile_entity (id);

CREATE TABLE skill_entity
(
    id         bigserial    NOT NULL,
    name       varchar(100) NOT NULL,
    type       varchar(100) NOT NULL,
    level      smallint     NOT NULL,
    profile_id bigint       NOT NULL
);
ALTER TABLE skill_entity
    ADD CONSTRAINT pk_skill PRIMARY KEY (id);
ALTER TABLE skill_entity
    ADD CONSTRAINT fk_skill_applicant FOREIGN KEY (profile_id) REFERENCES profile_entity (id);

CREATE TABLE education_entity
(
    id              bigserial    NOT NULL,
    institution     varchar(150) NOT NULL,
    location        varchar(100) NOT NULL,
    education_start date         NOT NULL,
    education_end   date,
    degree_name     varchar(150) NOT NULL,
    description     text,
    profile_id      bigint       NOT NULL
);
ALTER TABLE education_entity
    ADD CONSTRAINT pk_education PRIMARY KEY (id);
ALTER TABLE education_entity
    ADD CONSTRAINT fk_education_applicant FOREIGN KEY (profile_id) REFERENCES profile_entity (id);