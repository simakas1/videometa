CREATE TABLE users
(
    id          UUID PRIMARY KEY,
    username    VARCHAR(255) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    active      BOOLEAN      NOT NULL,
    authorities TEXT         NOT NULL
);

INSERT INTO users (id, username, password, active, authorities)
VALUES ('550e8400-e29b-41d4-a716-446655440000',
        'testUser',
        '$2a$10$VaM0X0dQyNLm3B4WSxe9eOdMfEmwwHZ1ZA.SElg33oCmh6v7GaUWm',
        true,
        'ROLE_USER,ROLE_ADMIN');
