-- ==========================================
-- H2 Database Schema for Testing
-- ==========================================

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    points INT DEFAULT 100,
    major VARCHAR(100),
    grade VARCHAR(20),
    student_id VARCHAR(20),
    nickname VARCHAR(50),
    avatar VARCHAR(255),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted INT DEFAULT 0,
    version INT DEFAULT 0
);

-- 标签表
CREATE TABLE IF NOT EXISTS biz_tag (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(200),
    icon VARCHAR(255),
    color VARCHAR(20),
    category VARCHAR(20),
    use_count INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted INT DEFAULT 0
);

-- 问题表
CREATE TABLE IF NOT EXISTS biz_question (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    images TEXT,
    view_count INT DEFAULT 0,
    answer_count INT DEFAULT 0,
    collection_count INT DEFAULT 0,
    like_count INT DEFAULT 0,
    reward_points INT DEFAULT 0,
    status INT DEFAULT 1,
    is_draft INT DEFAULT 0,
    is_solved INT DEFAULT 0,
    accepted_answer_id BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted INT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES sys_user(id)
);

-- 问题-标签关联表
CREATE TABLE IF NOT EXISTS biz_question_tag (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (question_id) REFERENCES biz_question(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES biz_tag(id) ON DELETE CASCADE,
    CONSTRAINT uk_question_tag UNIQUE (question_id, tag_id)
);

-- 回答表
CREATE TABLE IF NOT EXISTS biz_answer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    images TEXT,
    like_count INT DEFAULT 0,
    comment_count INT DEFAULT 0,
    is_accepted INT DEFAULT 0,
    status INT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted INT DEFAULT 0,
    FOREIGN KEY (question_id) REFERENCES biz_question(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES sys_user(id)
);

-- 评论表
CREATE TABLE IF NOT EXISTS biz_comment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    target_type INT NOT NULL,
    target_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    parent_id BIGINT,
    like_count INT DEFAULT 0,
    status INT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted INT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES sys_user(id)
);

-- 收藏表
CREATE TABLE IF NOT EXISTS biz_collection (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    target_type INT NOT NULL,
    target_id BIGINT NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES sys_user(id),
    CONSTRAINT uk_user_target UNIQUE (user_id, target_type, target_id)
);

-- 点赞记录表
CREATE TABLE IF NOT EXISTS biz_like_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    target_type INT NOT NULL,
    target_id BIGINT NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES sys_user(id),
    CONSTRAINT uk_user_target UNIQUE (user_id, target_type, target_id)
);

-- 用户-标签关联表
CREATE TABLE IF NOT EXISTS biz_user_tag (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES biz_tag(id) ON DELETE CASCADE,
    CONSTRAINT uk_user_tag UNIQUE (user_id, tag_id)
);

-- 消息通知表
CREATE TABLE IF NOT EXISTS sys_notice (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    is_read INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES sys_user(id)
);

-- 邮箱验证表
CREATE TABLE IF NOT EXISTS sys_email_verification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL,
    code VARCHAR(10) NOT NULL,
    type VARCHAR(20) NOT NULL,
    is_used INT DEFAULT 0,
    expire_time TIMESTAMP NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_email_code UNIQUE (email, code)
);