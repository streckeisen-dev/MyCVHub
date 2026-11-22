--liquibase formatted sql

--changeset lstreckeisen:15

ALTER TABLE admin_audit_log_entity ADD COLUMN actor_username varchar(100);
ALTER TABLE admin_audit_log_entity ADD COLUMN actor_id_snapshot bigint;

UPDATE admin_audit_log_entity log
SET actor_username = account.username, actor_id_snapshot = account.id
FROM admin_account_entity account
WHERE log.admin_id = account.id;
