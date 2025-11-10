CREATE TABLE IF NOT EXISTS plugin_sync_job
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    article_id BIGINT,
    platform   VARCHAR(32),
    status     VARCHAR(32),
    message    VARCHAR(2000),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);