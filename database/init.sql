-- ============================================
-- CampusZhihu 数据库初始化脚本
-- 生成日期: 2026-01-03
-- 基于实体类自动生成
-- ============================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- 1. 用户表 (sys_user)
-- ============================================
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `email` VARCHAR(100) NOT NULL COMMENT '邮箱',
    `password` VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    `points` INT NOT NULL DEFAULT 0 COMMENT '积分',
    `major` VARCHAR(100) DEFAULT NULL COMMENT '专业',
    `grade` VARCHAR(50) DEFAULT NULL COMMENT '年级',
    `student_id` VARCHAR(50) DEFAULT NULL COMMENT '学号',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除(0:未删除 1:已删除)',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`),
    KEY `idx_student_id` (`student_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================
-- 2. 消息通知表 (sys_notice)
-- ============================================
DROP TABLE IF EXISTS `sys_notice`;
CREATE TABLE `sys_notice` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '通知ID',
    `receiver_id` BIGINT NOT NULL COMMENT '接收者ID',
    `sender_id` BIGINT NOT NULL COMMENT '发送者ID',
    `type` TINYINT NOT NULL COMMENT '通知类型(1:点赞 2:回答 3:采纳 4:评论)',
    `content` VARCHAR(500) NOT NULL COMMENT '通知内容',
    `target_type` TINYINT NOT NULL COMMENT '目标类型：1-问题，2-回答，3-评论',
    `target_id` BIGINT NOT NULL COMMENT '目标ID（问题ID、回答ID或评论ID）',
    `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读(0:未读 1:已读)',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除(0:未删除 1:已删除)',
    PRIMARY KEY (`id`),
    KEY `idx_receiver_id` (`receiver_id`, `is_read`),
    KEY `idx_sender_id` (`sender_id`),
    KEY `idx_target` (`target_type`, `target_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息通知表';

-- ============================================
-- 3. 邮箱验证表 (sys_email_verification)
-- ============================================
DROP TABLE IF EXISTS `sys_email_verification`;
CREATE TABLE `sys_email_verification` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `email` VARCHAR(100) NOT NULL COMMENT '邮箱地址',
    `token` VARCHAR(255) NOT NULL COMMENT '验证Token',
    `type` TINYINT NOT NULL COMMENT '验证类型（1: 注册验证, 2: 密码重置）',
    `is_verified` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已验证（0: 未验证, 1: 已验证）',
    `verified_time` DATETIME DEFAULT NULL COMMENT '验证时间',
    `expire_time` DATETIME NOT NULL COMMENT '过期时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记（0: 未删除, 1: 已删除）',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_token` (`token`),
    KEY `idx_email` (`email`),
    KEY `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='邮箱验证表';

-- ============================================
-- 4. 问题表 (biz_question)
-- ============================================
DROP TABLE IF EXISTS `biz_question`;
CREATE TABLE `biz_question` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '问题ID',
    `user_id` BIGINT NOT NULL COMMENT '提问用户ID',
    `title` VARCHAR(200) NOT NULL COMMENT '问题标题',
    `content` TEXT NOT NULL COMMENT '问题内容',
    `images` VARCHAR(1000) DEFAULT NULL COMMENT '图片URLs（逗号分隔）',
    `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览次数',
    `answer_count` INT NOT NULL DEFAULT 0 COMMENT '回答数量',
    `collection_count` INT NOT NULL DEFAULT 0 COMMENT '收藏次数',
    `like_count` INT NOT NULL DEFAULT 0 COMMENT '点赞次数',
    `reward_points` INT NOT NULL DEFAULT 0 COMMENT '悬赏积分',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-草稿，1-已发布，2-已关闭',
    `is_draft` TINYINT NOT NULL DEFAULT 0 COMMENT '是否为草稿：0-否，1-是',
    `is_solved` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已解决：0-未解决，1-已解决',
    `accepted_answer_id` BIGINT DEFAULT NULL COMMENT '被采纳的回答ID',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`, `is_solved`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_reward_points` (`reward_points`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问题表';

-- ============================================
-- 5. 回答表 (biz_answer)
-- ============================================
DROP TABLE IF EXISTS `biz_answer`;
CREATE TABLE `biz_answer` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '回答ID',
    `question_id` BIGINT NOT NULL COMMENT '问题ID',
    `user_id` BIGINT NOT NULL COMMENT '回答用户ID',
    `content` TEXT NOT NULL COMMENT '回答内容',
    `images` VARCHAR(1000) DEFAULT NULL COMMENT '图片URLs（逗号分隔）',
    `like_count` INT NOT NULL DEFAULT 0 COMMENT '点赞次数',
    `comment_count` INT NOT NULL DEFAULT 0 COMMENT '评论次数',
    `is_accepted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否被采纳：0-未采纳，1-已采纳',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-草稿，1-已发布',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_question_id` (`question_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_is_accepted` (`is_accepted`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='回答表';

-- ============================================
-- 6. 评论表 (biz_comment)
-- ============================================
DROP TABLE IF EXISTS `biz_comment`;
CREATE TABLE `biz_comment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评论ID',
    `user_id` BIGINT NOT NULL COMMENT '评论用户ID',
    `target_type` TINYINT NOT NULL COMMENT '目标类型：1-问题，2-回答',
    `target_id` BIGINT NOT NULL COMMENT '目标ID（问题ID或回答ID）',
    `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父评论ID（0表示顶级评论）',
    `content` VARCHAR(1000) NOT NULL COMMENT '评论内容',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_target` (`target_type`, `target_id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

-- ============================================
-- 7. 标签表 (biz_tag)
-- ============================================
DROP TABLE IF EXISTS `biz_tag`;
CREATE TABLE `biz_tag` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '标签ID',
    `name` VARCHAR(50) NOT NULL COMMENT '标签名称',
    `description` VARCHAR(200) DEFAULT NULL COMMENT '标签描述',
    `icon` VARCHAR(100) DEFAULT NULL COMMENT '标签图标',
    `color` VARCHAR(20) DEFAULT NULL COMMENT '标签颜色',
    `use_count` INT NOT NULL DEFAULT 0 COMMENT '使用次数',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`),
    KEY `idx_use_count` (`use_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签表';

-- ============================================
-- 8. 问题-标签关联表 (biz_question_tag)
-- ============================================
DROP TABLE IF EXISTS `biz_question_tag`;
CREATE TABLE `biz_question_tag` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联ID',
    `question_id` BIGINT NOT NULL COMMENT '问题ID',
    `tag_id` BIGINT NOT NULL COMMENT '标签ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_question_tag` (`question_id`, `tag_id`),
    KEY `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问题-标签关联表';

-- ============================================
-- 9. 收藏表 (biz_collection)
-- ============================================
DROP TABLE IF EXISTS `biz_collection`;
CREATE TABLE `biz_collection` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `target_type` TINYINT NOT NULL COMMENT '目标类型：1-问题，2-回答',
    `target_id` BIGINT NOT NULL COMMENT '目标ID（问题ID或回答ID）',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_target` (`user_id`, `target_type`, `target_id`),
    KEY `idx_target` (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏表';

-- ============================================
-- 10. 点赞记录表 (biz_like_record)
-- ============================================
DROP TABLE IF EXISTS `biz_like_record`;
CREATE TABLE `biz_like_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `target_type` TINYINT NOT NULL COMMENT '目标类型：1-问题，2-回答，3-评论',
    `target_id` BIGINT NOT NULL COMMENT '目标ID',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_target` (`user_id`, `target_type`, `target_id`),
    KEY `idx_target` (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='点赞记录表';

-- ============================================
-- 11. 用户关注标签表 (biz_user_tag)
-- ============================================
DROP TABLE IF EXISTS `biz_user_tag`;
CREATE TABLE `biz_user_tag` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '关注ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `tag_id` BIGINT NOT NULL COMMENT '标签ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_tag` (`user_id`, `tag_id`),
    KEY `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户关注标签表';

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 插入测试数据
-- ============================================

-- 插入测试用户（密码都是 Test123456，BCrypt加密后的值）
INSERT INTO `sys_user` (`username`, `password`, `email`, `points`, `major`, `grade`, `student_id`, `nickname`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'admin@campus.edu', 1000, '计算机科学与技术', '2021级', '2021001001', '管理员'),
('zhangsan', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'zhangsan@campus.edu', 500, '软件工程', '2022级', '2022001001', '张三'),
('lisi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'lisi@campus.edu', 300, '数据科学', '2022级', '2022001002', '李四'),
('wangwu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'wangwu@campus.edu', 200, '人工智能', '2023级', '2023001001', '王五');

-- 插入测试标签
INSERT INTO `biz_tag` (`name`, `description`, `icon`, `color`) VALUES
('前端开发', '前端开发相关技术讨论', '💻', '#409EFF'),
('后端开发', '后端开发相关技术讨论', '⚙️', '#67C23A'),
('数据库', '数据库设计与优化', '🗄️', '#E6A23C'),
('算法', '算法与数据结构', '🧮', '#F56C6C'),
('Vue.js', 'Vue.js框架学习与实践', '🎨', '#42b883'),
('Spring Boot', 'Spring Boot开发', '🍃', '#6db33f'),
('Java', 'Java编程语言', '☕', '#f89820'),
('JavaScript', 'JavaScript编程', '📜', '#f7df1e'),
('Python', 'Python编程语言', '🐍', '#3776ab'),
('MySQL', 'MySQL数据库', '🐬', '#00758f');

-- 插入测试问题
INSERT INTO `biz_question` (`user_id`, `title`, `content`, `reward_points`, `view_count`, `answer_count`) VALUES
(2, '如何学习Vue 3？', '我是一个前端新手，想学习Vue 3框架，请问有什么好的学习路径和资源推荐吗？', 20, 128, 3),
(3, 'Spring Boot如何集成MyBatis-Plus？', '请问在Spring Boot项目中如何集成MyBatis-Plus？需要配置哪些内容？', 50, 256, 5),
(2, 'MySQL索引优化有哪些技巧？', '最近在做数据库性能优化，想了解一下MySQL索引优化的最佳实践。', 30, 189, 4),
(4, 'JavaScript异步编程怎么理解？', 'Promise、async/await这些异步编程的概念总是搞不清楚，求大神指点！', 0, 95, 2),
(3, 'Vue 3 Composition API的优势是什么？', '相比Options API，Composition API有哪些优势？适合在什么场景使用？', 10, 167, 6);

-- 关联问题和标签
INSERT INTO `biz_question_tag` (`question_id`, `tag_id`) VALUES
(1, 1), (1, 5),
(2, 2), (2, 6), (2, 7),
(3, 3), (3, 10),
(4, 1), (4, 8),
(5, 1), (5, 5);

-- 插入测试回答
INSERT INTO `biz_answer` (`question_id`, `user_id`, `content`, `like_count`) VALUES
(1, 3, '学习Vue 3的建议路径：\n1. 先掌握HTML、CSS、JavaScript基础\n2. 学习Vue 3核心概念：响应式、组件、生命周期\n3. 实践Composition API\n4. 学习Vue Router和Pinia状态管理\n5. 做几个实战项目', 15),
(1, 4, '推荐几个学习资源：\n- Vue 3官方文档（中文）\n- 尚硅谷Vue3教程\n- 掘金社区的Vue专栏\n建议多动手实践！', 8),
(2, 1, 'MyBatis-Plus集成步骤：\n1. 添加依赖：mybatis-plus-boot-starter\n2. 配置数据源\n3. 配置MyBatis-Plus属性\n4. 创建Mapper继承BaseMapper\n5. 启用@MapperScan扫描', 25),
(3, 2, 'MySQL索引优化技巧：\n1. 为WHERE、ORDER BY、JOIN字段添加索引\n2. 避免在索引列上使用函数\n3. 使用覆盖索引减少回表\n4. 定期分析表统计信息\n5. 避免索引失效的情况', 12);

-- 插入测试评论
INSERT INTO `biz_comment` (`user_id`, `target_type`, `target_id`, `content`) VALUES
(4, 1, 1, '我也想学Vue 3，关注这个问题！'),
(2, 2, 1, '说得很详细，感谢分享！'),
(3, 2, 3, '这个方法很实用，已经实践过了👍'),
(4, 1, 2, '我也遇到同样的问题');

-- 插入测试点赞记录
INSERT INTO `biz_like_record` (`user_id`, `target_type`, `target_id`) VALUES
(2, 1, 1), (3, 1, 1), (4, 1, 1),
(2, 2, 1), (4, 2, 1),
(2, 2, 3), (3, 2, 3);

-- 插入测试收藏记录
INSERT INTO `biz_collection` (`user_id`, `target_type`, `target_id`) VALUES
(2, 1, 1), (3, 1, 1),
(2, 1, 2), (4, 1, 2),
(2, 2, 1), (3, 2, 3);

-- ============================================
-- 数据库初始化完成
-- ============================================
SELECT '========================================' AS '';
SELECT 'CampusZhihu 数据库初始化完成！' AS '';
SELECT '========================================' AS '';
SELECT CONCAT('表总数: ', COUNT(*)) AS '' FROM information_schema.tables WHERE table_schema = 'campus_zhihu' AND table_type = 'base table';
SELECT CONCAT('用户数: ', COUNT(*)) AS '' FROM sys_user;
SELECT CONCAT('问题数: ', COUNT(*)) AS '' FROM biz_question;
SELECT CONCAT('回答数: ', COUNT(*)) AS '' FROM biz_answer;
SELECT CONCAT('标签数: ', COUNT(*)) AS '' FROM biz_tag;
SELECT '========================================' AS '';
SELECT '✅ 可以开始使用了！' AS '';
SELECT '========================================' AS '';