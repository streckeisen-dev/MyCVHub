--liquibase formatted sql

--changeset lstreckeisen:5
--validCheckSum: any
CREATE TABLE account_details_entity
(
    id           bigserial    NOT NULL,
    firstname    varchar(50)  NOT NULL,
    lastname     varchar(50)  NOT NULL,
    email        varchar(100) NOT NULL,
    phone        varchar(30),
    birthday     date         NOT NULL,
    street       varchar(100) NOT NULL,
    house_number varchar(10),
    postcode     varchar(15)  NOT NULL,
    city         varchar(100) NOT NULL,
    country      character(2) NOT NULL
);
ALTER TABLE account_details_entity
    ADD CONSTRAINT pk_account_details PRIMARY KEY (id);

ALTER TABLE applicant_account_entity ADD COLUMN username varchar(100);
ALTER TABLE applicant_account_entity ADD CONSTRAINT unique_account_username UNIQUE (username);
ALTER TABLE applicant_account_entity ADD COLUMN is_oauth_user boolean NOT NULL CONSTRAINT df_account_oauth DEFAULT false;
ALTER TABLE applicant_account_entity ALTER COLUMN is_oauth_user DROP DEFAULT;
ALTER TABLE applicant_account_entity
    ADD COLUMN account_details_id bigint NULL;
ALTER TABLE applicant_account_entity
    ADD CONSTRAINT fk_account_details FOREIGN KEY (account_details_id) REFERENCES account_details_entity (id);

INSERT INTO account_details_entity (firstname,
                                    lastname,
                                    email,
                                    phone,
                                    birthday,
                                    street,
                                    house_number,
                                    postcode,
                                    city,
                                    country)
SELECT firstname,
       lastname,
       email,
       phone,
       birthday,
       street,
       house_number,
       postcode,
       city,
       country
FROM applicant_account_entity;

UPDATE applicant_account_entity aae
SET account_details_id = ade.id
FROM account_details_entity ade
WHERE aae.email = ade.email;

UPDATE applicant_account_entity a
SET username = COALESCE((SELECT p.alias FROM profile_entity p WHERE p.account_id = a.id), a.email);
ALTER TABLE applicant_account_entity ALTER COLUMN username SET NOT NULL;

CREATE TABLE oauth_integration_entity
(
    id           varchar(255) NOT NULL,
    type         varchar(30)  NOT NULL,
    access_token varchar(255) NOT NULL,
    account_id   bigint       NOT NULL
);
ALTER TABLE oauth_integration_entity
    ADD CONSTRAINT pk_oauth_integration PRIMARY KEY (id, type);
ALTER TABLE oauth_integration_entity
    ADD CONSTRAINT fk_oauth_integration_account FOREIGN KEY (account_id) REFERENCES applicant_account_entity (id);