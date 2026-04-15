-- ==========================================
-- 为热门标签添加问题-标签关联数据
-- ==========================================

-- 为TypeScript标签添加问题
INSERT IGNORE INTO biz_question_tag (question_id, tag_id) VALUES
(1, 12),
(1, 5),
(2, 12),
(3, 12);

-- 为前端开发标签添加问题
INSERT IGNORE INTO biz_question_tag (question_id, tag_id) VALUES
(1, 1),
(2, 1),
(3, 1);

-- 为后端开发标签添加问题
INSERT IGNORE INTO biz_question_tag (question_id, tag_id) VALUES
(1, 13),
(2, 13),
(3, 13);

-- 为Spring Boot标签添加问题
INSERT IGNORE INTO biz_question_tag (question_id, tag_id) VALUES
(1, 9),
(2, 9),
(3, 9);

-- 为Java标签添加问题
INSERT IGNORE INTO biz_question_tag (question_id, tag_id) VALUES
(1, 1),
(2, 1),
(3, 1);

-- 为JavaScript标签添加问题
INSERT IGNORE INTO biz_question_tag (question_id, tag_id) VALUES
(1, 3),
(2, 3),
(3, 3);

-- 为React标签添加问题
INSERT IGNORE INTO biz_question_tag (question_id, tag_id) VALUES
(1, 11),
(2, 11),
(3, 11);

-- 为Node.js标签添加问题
INSERT IGNORE INTO biz_question_tag (question_id, tag_id) VALUES
(1, 13),
(2, 13),
(3, 13);

-- 为Docker标签添加问题
INSERT IGNORE INTO biz_question_tag (question_id, tag_id) VALUES
(1, 14),
(2, 14),
(3, 14);

-- 更新标签的使用次数
UPDATE biz_tag SET use_count = use_count + 1 WHERE id IN (1, 5, 12, 13, 9, 11, 3, 14);
