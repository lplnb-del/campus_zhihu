-- ==========================================
-- 添加分类字段到标签表
-- ==========================================

-- 添加category字段
ALTER TABLE biz_tag ADD COLUMN category VARCHAR(20) DEFAULT 'other' COMMENT '分类：tech-技术，study-学习，life-生活，career-职业，other-其他';

-- 为现有数据设置分类（示例数据，实际可根据业务需求调整）
UPDATE biz_tag SET category = 'tech' WHERE name LIKE '%Java%' OR name LIKE '%Python%' OR name LIKE '%前端%' OR name LIKE '%后端%';
UPDATE biz_tag SET category = 'study' WHERE name LIKE '%学习%' OR name LIKE '%考试%' OR name LIKE '%课程%';
UPDATE biz_tag SET category = 'life' WHERE name LIKE '%生活%' OR name LIKE '%美食%' OR name LIKE '%旅行%';
UPDATE biz_tag SET category = 'career' WHERE name LIKE '%职业%' OR name LIKE '%工作%' OR name LIKE '%面试%';

-- 为category字段创建索引以提升查询性能
CREATE INDEX idx_category ON biz_tag(category);
