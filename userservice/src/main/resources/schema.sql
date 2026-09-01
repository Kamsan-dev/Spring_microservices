/*

* --- General rules ---

* Use underscore_names
* Table names should be plural
* Spell out id fields (item_id instead of id)
* Don't use ambiguous column names
* Name foreign key columns the same as the columns they refer to
* Use caps for all SQL keywords

*/

BEGIN;

-- Authorization server --

CREATE TABLE IF NOT EXISTS oauth2_registered_client (
    id VARCHAR(100) PRIMARY KEY,
    client_id VARCHAR(100) NOT NULL,
    client_id_issued_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP NOT NULL,
    client_secret VARCHAR(200) DEFAULT NULL,
    client_secret_expires_at TIMESTAMPTZ DEFAULT NULL,
    client_name VARCHAR(200) NOT NULL,
    client_authentication_methods VARCHAR(1000) NOT NULL,
    authorization_grant_types VARCHAR(1000) NOT NULL,
    redirect_uris VARCHAR(1000) DEFAULT NULL,
    post_logout_redirect_uris VARCHAR(1000) DEFAULT NULL,
    scopes VARCHAR(1000) NOT NULL,
    client_settings VARCHAR(2000) NOT NULL,
    token_settings VARCHAR(2000) NOT NULL
);

-- User Service --

CREATE TABLE IF NOT EXISTS users (
    user_id BIGSERIAL PRIMARY KEY,
    user_public_id UUID NOT NULL,
    username VARCHAR(25) NOT NULL,
    first_name VARCHAR(25) NOT NULL,
    last_name VARCHAR(25) NOT NULL,
    email VARCHAR(40) NOT NULL,
    member_id VARCHAR(40) NOT NULL,
    phone VARCHAR(15) DEFAULT NULL,
    address VARCHAR(100) DEFAULT NULL,
    bio VARCHAR(100) DEFAULT NULL,
    qr_code_secret VARCHAR(50) DEFAULT NULL,
    qr_code_image_uri TEXT DEFAULT NULL,
    image_url VARCHAR(255) DEFAULT 'https://cdn.vectorstock.com/i/1000v/41/91/avatar-default-user-profile-icon-simple-flat-grey-vector-57234191.jpg',
    last_login TIMESTAMPTZ DEFAULT NULL,
    login_attempts INTEGER DEFAULT 0,
    is_using_mfa BOOLEAN NOT NULL DEFAULT FALSE,
    is_account_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    is_account_expired BOOLEAN NOT NULL DEFAULT FALSE,
    is_account_locked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_user_public_id UNIQUE (user_public_id)
);

CREATE TABLE IF NOT EXISTS roles (
    role_id BIGSERIAL PRIMARY KEY,
    role_public_id UUID NOT NULL,
    name VARCHAR(25) NOT NULL,
    authority TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_roles_name UNIQUE (name),
    CONSTRAINT uq_roles_role_public_id UNIQUE (role_public_id)
);


CREATE TABLE IF NOT EXISTS user_roles (
    user_role_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    CONSTRAINT fk_user_roles_user_id FOREIGN KEY (user_id) REFERENCES users(user_id) MATCH SIMPLE ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_user_roles_role_id FOREIGN KEY (role_id) REFERENCES roles(role_id) MATCH SIMPLE ON UPDATE CASCADE ON DELETE RESTRICT
);


CREATE TABLE IF NOT EXISTS credentials (
    credential_id BIGSERIAL PRIMARY KEY,
    credential_public_id UUID NOT NULL,
    user_id BIGINT NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_credentials_credential_public_id UNIQUE(credential_public_id),
    CONSTRAINT uq_credentials_user_id UNIQUE(user_id),
    CONSTRAINT fk_credentials_user_id FOREIGN KEY (user_id) REFERENCES users(user_id) MATCH SIMPLE ON UPDATE CASCADE ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS account_tokens (
    account_token_id BIGSERIAL PRIMARY KEY,
    token VARCHAR(40) NOT NULL,
    user_id BIGINT NOT NULL,
    is_expired BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_account_tokens_token UNIQUE(token),
    CONSTRAINT uq_account_tokens_user_id UNIQUE(user_id),
    CONSTRAINT fk_account_tokens_user_id FOREIGN KEY (user_id) REFERENCES users(user_id) MATCH SIMPLE ON UPDATE CASCADE ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS password_tokens (
    password_token_id BIGSERIAL PRIMARY KEY,
    token VARCHAR(40) NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_password_tokens_token UNIQUE(token),
    CONSTRAINT uq_password_tokens_user_id UNIQUE(user_id),
    CONSTRAINT fk_password_tokens_user_id FOREIGN KEY (user_id) REFERENCES users(user_id) MATCH SIMPLE ON UPDATE CASCADE ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS devices (
    device_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    machine VARCHAR(40) NOT NULL,
    client VARCHAR(40) NOT NULL,
    ip_address VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_devices_user_id FOREIGN KEY (user_id) REFERENCES users(user_id) MATCH SIMPLE ON UPDATE CASCADE ON DELETE RESTRICT
);

-- Stored Procedures --

CREATE OR REPLACE PROCEDURE create_user(
    IN p_public_id VARCHAR(40),
    IN p_first_name VARCHAR(25),
    IN p_last_name VARCHAR(25),
    IN p_email VARCHAR(40),
    IN p_username VARCHAR(25),
    IN p_password VARCHAR(255),
    IN p_credential_public_id VARCHAR(40),
    IN p_token VARCHAR(40),
    IN p_member_id VARCHAR(40)
    )
    LANGUAGE PLPGSQL
    AS $$
    DECLARE
        v_user_id BIGINT;
    BEGIN
    --- queries
        CREATE EXTENSION IF NOT EXISTS pgcrypto;
        INSERT INTO users (user_public_id, first_name, last_name, email, username, member_id) VALUES (
            p_public_id, p_first_name, p_last_name, p_email, encode(gen_random_bytes(9), 'hex'), p_member_id) RETURNING user_id INTO v_user_id;

        INSERT INTO credentials (credential_public_id, user_id, password) VALUES (
            p_credential_public_id, v_user_id, p_password);

        INSERT INTO user_roles (user_id, role_id) VALUES(
            v_user_id,
            (SELECT roles.role_id FROM roles WHERE roles.name = 'USER')
        );

        INSERT INTO account_tokens(token, user_id) VALUES (p_token, v_user_id);

    END;
    $$
