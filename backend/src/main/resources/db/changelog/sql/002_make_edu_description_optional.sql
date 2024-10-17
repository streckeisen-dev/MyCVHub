--liquibase formatted sql

--changeset lstreckeisen:2
--validCheckSum: any
ALTER TABLE education_entity ALTER COLUMN description DROP NOT NULL;