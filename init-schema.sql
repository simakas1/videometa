CREATE TABLE users
(
    id          UUID PRIMARY KEY,
    username    VARCHAR(255) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    active      BOOLEAN      NOT NULL,
    authorities TEXT         NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_username ON users (username);

INSERT INTO users (id, username, password, active, authorities)
VALUES (gen_random_uuid(),
        'admin',
        '$2a$10$VaM0X0dQyNLm3B4WSxe9eOdMfEmwwHZ1ZA.SElg33oCmh6v7GaUWm',
        true,
        'ADMIN');

INSERT INTO users (id, username, password, active, authorities)
VALUES (gen_random_uuid(),
        'analytic',
        '$2a$10$VaM0X0dQyNLm3B4WSxe9eOdMfEmwwHZ1ZA.SElg33oCmh6v7GaUWm',
        true,
        'USER,VIDEO_ANALYTICS');

INSERT INTO users (id, username, password, active, authorities)
VALUES (gen_random_uuid(),
        'default',
        '$2a$10$VaM0X0dQyNLm3B4WSxe9eOdMfEmwwHZ1ZA.SElg33oCmh6v7GaUWm',
        true,
        'USER');

INSERT INTO users (id, username, password, active, authorities)
VALUES (gen_random_uuid(),
        'default_video_importer_analytic',
        '$2a$10$VaM0X0dQyNLm3B4WSxe9eOdMfEmwwHZ1ZA.SElg33oCmh6v7GaUWm',
        true,
        'USER,VIDEO_IMPORTER,VIDEO_ANALYTICS');

CREATE TABLE videos
(
    id          UUID PRIMARY KEY,
    title       VARCHAR(255) NOT NULL,
    url         VARCHAR(255) NOT NULL,
    duration    INTEGER      NOT NULL,
    source      VARCHAR(255) NOT NULL,
    upload_date TIMESTAMP,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_videos_source ON videos (source);
CREATE INDEX idx_videos_upload_date ON videos (upload_date);
CREATE INDEX idx_videos_duration ON videos (duration);
CREATE INDEX idx_videos_all_filters ON videos (source, upload_date, duration);

ALTER TABLE videos
    ADD CONSTRAINT uk_videos_url UNIQUE (url);

CREATE OR REPLACE VIEW video_stats_per_source AS
SELECT source,
       COUNT(*)                AS total_videos,
       ROUND(AVG(duration), 2) AS average_duration
FROM videos
GROUP BY source;