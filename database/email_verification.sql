-- 邮箱验证表
CREATE TABLE IF NOT EXISTS `sys_email_verification` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `email` varchar(100) NOT NULL COMMENT '邮箱地址',
  `token` varchar(255) NOT NULL COMMENT '验证Token',
  `type` tinyint NOT NULL DEFAULT 1 COMMENT '验证类型（1: 注册验证, 2: 密码重置）',
  `is_verified` tinyint NOT NULL DEFAULT 0 COMMENT '是否已验证（0: 未验证, 1: 已验证）',
  `verified_time` datetime DEFAULT NULL COMMENT '验证时间',
  `expire_time` datetime NOT NULL COMMENT '过期时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除标记（0: 未删除, 1: 已删除）',
  `version` int NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_token` (`token`),
  KEY `idx_email` (`email`),
  KEY `idx_type` (`type`),
  KEY `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='邮箱验证表';