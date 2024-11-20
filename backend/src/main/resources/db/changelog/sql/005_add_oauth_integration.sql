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
    country      varchar(2) NOT NULL
);
ALTER TABLE account_details_entity
    ADD CONSTRAINT pk_account_details PRIMARY KEY (id);

ALTER TABLE applicant_account_entity
    ALTER COLUMN password TYPE varchar(60);
ALTER TABLE applicant_account_entity
    ADD COLUMN username varchar(100);
ALTER TABLE applicant_account_entity
    ADD CONSTRAINT unique_account_username UNIQUE (username);
ALTER TABLE applicant_account_entity
    ADD COLUMN is_oauth_user boolean NOT NULL
        CONSTRAINT df_account_oauth DEFAULT false;
ALTER TABLE applicant_account_entity
    ALTER COLUMN is_oauth_user DROP DEFAULT;
ALTER TABLE applicant_account_entity
    ADD COLUMN account_details_id bigint NULL;
ALTER TABLE applicant_account_entity
    ADD CONSTRAINT fk_account_details FOREIGN KEY (account_details_id) REFERENCES account_details_entity (id);
ALTER TABLE applicant_account_entity
    ADD COLUMN is_verified boolean NOT NULL
        CONSTRAINT df_account_verified DEFAULT false;
ALTER TABLE applicant_account_entity
    ALTER COLUMN is_verified DROP DEFAULT;

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
SET username = a.email
WHERE username IS NULL;

ALTER TABLE applicant_account_entity
    ALTER COLUMN username SET NOT NULL;
ALTER TABLE applicant_account_entity
    ALTER COLUMN password DROP NOT NULL;

ALTER TABLE applicant_account_entity
    ADD CONSTRAINT check_password_not_null CHECK (is_oauth_user OR (NOT is_oauth_user AND password IS NOT NULL));

CREATE TABLE oauth_integration_entity
(
    id           varchar(255) NOT NULL,
    type         varchar(30)  NOT NULL,
    account_id   bigint       NOT NULL
);
ALTER TABLE oauth_integration_entity
    ADD CONSTRAINT pk_oauth_integration PRIMARY KEY (id, type);
ALTER TABLE oauth_integration_entity
    ADD CONSTRAINT fk_oauth_integration_account FOREIGN KEY (account_id) REFERENCES applicant_account_entity (id);

CREATE TABLE account_verification_entity
(
    id              bigserial   NOT NULL,
    token           varchar(64) NOT NULL,
    expiration_date timestamp   NOT NULL,
    account_id      bigint      NOT NULL
);
ALTER TABLE account_verification_entity
    ADD CONSTRAINT pk_account_verification PRIMARY KEY (id);
ALTER TABLE account_verification_entity
    ADD CONSTRAINT fk_account_verification_account FOREIGN KEY (account_id) REFERENCES applicant_account_entity (id);
ALTER TABLE account_verification_entity
    ADD CONSTRAINT unique_account_verification_token UNIQUE (token);
ALTER TABLE account_verification_entity
    ADD CONSTRAINT unique_account_verification_account UNIQUE (account_id);

CREATE TABLE upgrade_task_execution_entity
(
    id             int          NOT NULL,
    task_name      varchar(255) NOT NULL,
    execution_date timestamp    NOT NULL
);
ALTER TABLE upgrade_task_execution_entity
    ADD CONSTRAINT pk_upgrade_task_execution PRIMARY KEY (id);

ALTER TABLE applicant_account_entity
    DROP COLUMN email,
    DROP COLUMN firstname,
    DROP COLUMN lastname,
    DROP COLUMN phone,
    DROP COLUMN birthday,
    DROP COLUMN street,
    DROP COLUMN house_number,
    DROP COLUMN postcode,
    DROP COLUMN city,
    DROP COLUMN country;

ALTER TABLE profile_entity
    DROP COLUMN alias;