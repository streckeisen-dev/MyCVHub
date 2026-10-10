--liquibase formatted sql

--changeset lstreckeisen:13
--validCheckSum: any

CREATE TABLE admin_account_entity
(
    id                            bigserial    NOT NULL,
    username                      varchar(100) NOT NULL,
    password                      varchar(60)  NOT NULL,
    role                          varchar(30)  NOT NULL,
    is_active                     boolean      NOT NULL,
    must_change_password          boolean      NOT NULL,
    temporary_password_expires_at timestamp,
    created_at                    timestamp    NOT NULL,
    last_login_at                 timestamp,
    password_changed_at           timestamp
);
ALTER TABLE admin_account_entity
    ADD CONSTRAINT pk_admin_account PRIMARY KEY (id);
ALTER TABLE admin_account_entity
    ADD CONSTRAINT unique_admin_account_username UNIQUE (username);
ALTER TABLE admin_account_entity
    ADD CONSTRAINT check_admin_account_role CHECK (role IN ('ADMIN', 'SUPER_ADMIN'));

CREATE TABLE admin_audit_log_entity
(
    id           bigserial    NOT NULL,
    admin_id     bigint,
    actor_source varchar(50)  NOT NULL,
    action       varchar(100) NOT NULL,
    result       varchar(30)  NOT NULL,
    target_type  varchar(100),
    target_id    varchar(255),
    timestamp    timestamp    NOT NULL
);
ALTER TABLE admin_audit_log_entity
    ADD CONSTRAINT pk_admin_audit_log PRIMARY KEY (id);
ALTER TABLE admin_audit_log_entity
    ADD CONSTRAINT fk_admin_audit_log_account FOREIGN KEY (admin_id) REFERENCES admin_account_entity (id);

CREATE TABLE admin_login_attempt_entity
(
    username        varchar(100) NOT NULL,
    failed_attempts integer      NOT NULL,
    locked_until    timestamp,
    updated_at      timestamp    NOT NULL
);
ALTER TABLE admin_login_attempt_entity
    ADD CONSTRAINT pk_admin_login_attempt PRIMARY KEY (username);

CREATE TABLE user_activity_event_entity
(
    id                   bigserial   NOT NULL,
    applicant_account_id bigint      NOT NULL,
    event_type           varchar(30) NOT NULL,
    timestamp            timestamp   NOT NULL
);
ALTER TABLE user_activity_event_entity
    ADD CONSTRAINT pk_user_activity_event PRIMARY KEY (id);
ALTER TABLE user_activity_event_entity
    ADD CONSTRAINT check_user_activity_event_type CHECK (event_type IN ('LOGIN', 'SIGNUP'));
CREATE INDEX idx_user_activity_event_type_timestamp ON user_activity_event_entity (event_type, timestamp);
CREATE INDEX idx_user_activity_event_account_timestamp ON user_activity_event_entity (applicant_account_id, timestamp);

CREATE TABLE platform_daily_metric_entity
(
    metric_date  date        NOT NULL,
    metric_name  varchar(50) NOT NULL,
    metric_value bigint      NOT NULL,
    updated_at   timestamp   NOT NULL
);
ALTER TABLE platform_daily_metric_entity
    ADD CONSTRAINT pk_platform_daily_metric PRIMARY KEY (metric_date, metric_name);
