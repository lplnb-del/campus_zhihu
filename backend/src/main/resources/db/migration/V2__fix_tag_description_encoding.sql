-- ==========================================
-- 修复标签描述乱码问题
-- ==========================================

-- 更新Docker标签描述
UPDATE biz_tag
SET description = 'Docker 是一个开源的应用容器引擎，让开发者可以打包他们的应用以及依赖包到一个可移植的容器中，然后发布到任何流行的 Linux 机器上，也可以实现虚拟化。',
    icon = 'Monitor'
WHERE name = 'Docker' AND id = 14;

-- 更新Node.js标签描述
UPDATE biz_tag
SET description = 'Node.js 是一个基于 Chrome V8 引擎的 JavaScript 运行环境，使用了一个事件驱动、非阻塞式 I/O 的模型，使其轻量又高效。',
    icon = 'MoreFilled'
WHERE name = 'Node.js' AND id = 13;

-- 更新TypeScript标签描述
UPDATE biz_tag
SET description = 'TypeScript 是 JavaScript 的一个超集，添加了可选的静态类型和基于类的面向对象编程，让代码更加健壮和易于维护。',
    icon = 'Reading'
WHERE name = 'TypeScript' AND id = 12;

-- 更新MySQL标签描述（如果存在）
UPDATE biz_tag
SET description = 'MySQL 是最流行的开源关系型数据库管理系统之一，广泛应用于各种 Web 应用程序中。',
    icon = 'Briefcase'
WHERE name = 'MySQL' AND description LIKE '%??%';

-- 更新Redis标签描述（如果存在）
UPDATE biz_tag
SET description = 'Redis 是一个开源的内存数据结构存储系统，可以用作数据库、缓存和消息中间件。',
    icon = 'Coffee'
WHERE name = 'Redis' AND description LIKE '%??%';

-- 更新Spring Boot标签描述（如果存在）
UPDATE biz_tag
SET description = 'Spring Boot 是基于 Spring 框架的快速开发框架，通过约定优于配置的理念，让开发者可以快速构建生产级别的 Spring 应用。',
    icon = 'Briefcase'
WHERE name = 'Spring Boot' AND description LIKE '%??%';
