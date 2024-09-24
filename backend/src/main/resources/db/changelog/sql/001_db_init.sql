--liquibase formatted sql

--changeset lstreckeisen:1
--validCheckSum: any

CREATE TABLE applicant (
    id bigserial NOT NULL,
    firstname varchar(50) NOT NULL,
    lastname varchar(50) NOT NULL,
    email varchar(100) NOT NULL,
    phone varchar(30),
    birthday date NOT NULL,
    street varchar(100) NOT NULL,
    house_number varchar(10),
    postcode varchar(15) NOT NULL,
    city varchar(100) NOT NULL,
    country character(2) NOT NULL,
    has_public_profile boolean NOT NULL,
    password character(60) NOT NULL
);

ALTER TABLE applicant ADD CONSTRAINT pk_applicant PRIMARY KEY (id);
ALTER TABLE applicant ADD CONSTRAINT unique_applicant_email UNIQUE (email);

CREATE TABLE work_experience (
    id bigserial NOT NULL,
    company varchar(100) NOT NULL,
    position_start date NOT NULL,
    position_end date,
    location varchar(100) NOT NULL,
    description text,
    applicant_id bigint NOT NULL
);
ALTER TABLE work_experience ADD CONSTRAINT pk_work_experience PRIMARY KEY (id);
ALTER TABLE work_experience ADD CONSTRAINT fk_work_experience_applicant FOREIGN KEY (applicant_id) REFERENCES applicant(id);

CREATE TABLE skill (
    id bigserial NOT NULL,
    name varchar(100) NOT NULL,
    type varchar(100) NOT NULL,
    level smallint NOT NULL,
    applicant_id bigint NOT NULL
);
ALTER TABLE skill ADD CONSTRAINT pk_skill PRIMARY KEY (id);
ALTER TABLE skill ADD CONSTRAINT fk_skill_applicant FOREIGN KEY (applicant_id) REFERENCES applicant(id);

CREATE TABLE education (
    id bigserial NOT NULL,
    school varchar(150) NOT NULL,
    location varchar(100) NOT NULL,
    education_start date NOT NULL,
    education_end date,
    degree_name varchar(150) NOT NULL,
    description text,
    applicant_id bigint NOT NULL
);
ALTER TABLE education ADD CONSTRAINT pk_education PRIMARY KEY (id);
ALTER TABLE education ADD CONSTRAINT fk_education_applicant FOREIGN KEY (applicant_id) REFERENCES applicant(id);