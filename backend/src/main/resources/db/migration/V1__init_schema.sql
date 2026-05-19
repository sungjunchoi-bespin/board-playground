-- Conduit RealWorld — Initial Schema
-- 7 tables: users, articles, tags, article_tags, comments, follows, favorites

-- ========================================
-- 1. users
-- ========================================
CREATE TABLE users (
    id         BIGSERIAL    PRIMARY KEY,
    email      VARCHAR(255) NOT NULL,
    username   VARCHAR(255) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    bio        TEXT,
    image      VARCHAR(512),
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_users_email    UNIQUE (email),
    CONSTRAINT uq_users_username UNIQUE (username)
);

CREATE INDEX idx_users_created_at ON users (created_at);

-- ========================================
-- 2. articles
-- ========================================
CREATE TABLE articles (
    id               BIGSERIAL    PRIMARY KEY,
    slug             VARCHAR(255) NOT NULL,
    title            VARCHAR(255) NOT NULL,
    description      VARCHAR(512) NOT NULL,
    body             TEXT         NOT NULL,
    author_id        BIGINT       NOT NULL,
    favorites_count  INTEGER      NOT NULL DEFAULT 0,
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_articles_slug UNIQUE (slug),
    CONSTRAINT fk_articles_author FOREIGN KEY (author_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_articles_author_id      ON articles (author_id);
CREATE INDEX idx_articles_created_at_desc ON articles (created_at DESC);

-- ========================================
-- 3. tags
-- ========================================
CREATE TABLE tags (
    id   BIGSERIAL    PRIMARY KEY,
    name VARCHAR(255) NOT NULL,

    CONSTRAINT uq_tags_name UNIQUE (name)
);

-- ========================================
-- 4. article_tags (join table)
-- ========================================
CREATE TABLE article_tags (
    article_id BIGINT NOT NULL,
    tag_id     BIGINT NOT NULL,

    PRIMARY KEY (article_id, tag_id),
    CONSTRAINT fk_article_tags_article FOREIGN KEY (article_id) REFERENCES articles (id) ON DELETE CASCADE,
    CONSTRAINT fk_article_tags_tag     FOREIGN KEY (tag_id)     REFERENCES tags (id)     ON DELETE CASCADE
);

CREATE INDEX idx_article_tags_tag_id ON article_tags (tag_id);

-- ========================================
-- 5. comments
-- ========================================
CREATE TABLE comments (
    id         BIGSERIAL PRIMARY KEY,
    body       TEXT      NOT NULL,
    article_id BIGINT    NOT NULL,
    author_id  BIGINT    NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_comments_article FOREIGN KEY (article_id) REFERENCES articles (id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_author  FOREIGN KEY (author_id)  REFERENCES users (id)    ON DELETE CASCADE
);

CREATE INDEX idx_comments_article_created ON comments (article_id, created_at DESC);
CREATE INDEX idx_comments_author_id       ON comments (author_id);

-- ========================================
-- 6. follows
-- ========================================
CREATE TABLE follows (
    follower_id BIGINT    NOT NULL,
    followee_id BIGINT    NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (follower_id, followee_id),
    CONSTRAINT fk_follows_follower FOREIGN KEY (follower_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_follows_followee FOREIGN KEY (followee_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX idx_follows_followee_id ON follows (followee_id);

-- ========================================
-- 7. favorites
-- ========================================
CREATE TABLE favorites (
    user_id    BIGINT    NOT NULL,
    article_id BIGINT    NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (user_id, article_id),
    CONSTRAINT fk_favorites_user    FOREIGN KEY (user_id)    REFERENCES users (id)    ON DELETE CASCADE,
    CONSTRAINT fk_favorites_article FOREIGN KEY (article_id) REFERENCES articles (id) ON DELETE CASCADE
);

CREATE INDEX idx_favorites_article_id ON favorites (article_id);
