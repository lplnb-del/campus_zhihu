-- ============================================
-- CampusZhihu 测试数据脚本
-- 生成日期: 2026-01-05
-- 用途: 丰富数据库测试数据
-- ============================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE campus_zhihu;

-- ============================================
-- 1. 清理现有测试数据（可选）
-- ============================================
-- DELETE FROM biz_like_record WHERE id > 0;
-- DELETE FROM biz_collection WHERE id > 0;
-- DELETE FROM biz_comment WHERE id > 0;
-- DELETE FROM biz_question_tag WHERE id > 0;
-- DELETE FROM biz_answer WHERE id > 0;
-- DELETE FROM biz_question WHERE id > 0;
-- DELETE FROM biz_tag WHERE id > 0;
-- DELETE FROM sys_user WHERE id > 1;

-- ============================================
-- 2. 插入更多测试用户（密码都是 Test123456）
-- ============================================
INSERT INTO `sys_user` (`username`, `password`, `email`, `points`, `major`, `grade`, `student_id`, `nickname`, `bio`, `school`) VALUES
('zhaoliu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'zhaoliu@campus.edu', 450, '软件工程', '2022级', '2022001003', '赵六', '热爱编程，喜欢分享', '清华大学'),
('sunqi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'sunqi@campus.edu', 380, '计算机科学与技术', '2023级', '2023001002', '孙七', '前端开发爱好者', '北京大学'),
('zhouba', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'zhouba@campus.edu', 520, '数据科学', '2021级', '2021001002', '周八', '数据分析与机器学习', '复旦大学'),
('wujiu', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'wujiu@campus.edu', 290, '人工智能', '2023级', '2023001003', '吴九', '深度学习研究', '上海交通大学'),
('zhengshi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'zhengshi@campus.edu', 680, '网络安全', '2020级', '2020001001', '郑十', '网络安全工程师', '浙江大学'),
('wangmazi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'wangmazi@campus.edu', 340, '软件工程', '2022级', '2022001004', '王麻子', '全栈开发工程师', '南京大学'),
('chenxi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'chenxi@campus.edu', 560, '计算机科学与技术', '2021级', '2021001003', '陈希', '算法竞赛选手', '华中科技大学'),
('yangming', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'yangming@campus.edu', 410, '数据科学', '2022级', '2022001005', '杨明', '大数据分析师', '武汉大学'),
('lihua', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'lihua@campus.edu', 720, '人工智能', '2020级', '2020001002', '李华', 'NLP研究员', '中山大学'),
('zhangwei', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'zhangwei@campus.edu', 390, '软件工程', '2023级', '2023001004', '张伟', '云原生技术爱好者', '西安交通大学');

-- ============================================
-- 3. 插入更多测试标签
-- ============================================
INSERT INTO `biz_tag` (`name`, `description`, `icon`, `color`, `use_count`) VALUES
('React', 'React框架学习与实践', '⚛️', '#61dafb', 0),
('TypeScript', 'TypeScript编程语言', '📘', '#3178c6', 0),
('Node.js', 'Node.js后端开发', '🟢', '#339933', 0),
('Docker', '容器化技术', '🐳', '#2496ed', 0),
('Kubernetes', '容器编排平台', '☸️', '#326ce5', 0),
('Git', '版本控制系统', '📦', '#f05032', 0),
('Linux', 'Linux操作系统', '🐧', '#fcc624', 0),
('Redis', 'Redis缓存数据库', '🔴', '#dc382d', 0),
('MongoDB', 'MongoDB文档数据库', '🍃', '#47a248', 0),
('Elasticsearch', '搜索引擎', '🔍', '#005571', 0),
('Nginx', 'Web服务器', '🌐', '#009639', 0),
('Webpack', '前端构建工具', '📦', '#8dd6f9', 0),
('Vite', '下一代前端构建工具', '⚡', '#646cff', 0),
('Jenkins', '持续集成工具', '🔧', '#d33833', 0),
('GitLab', '代码托管平台', '🦊', '#fc6d26', 0);

-- ============================================
-- 4. 插入更多测试问题
-- ============================================
INSERT INTO `biz_question` (`user_id`, `title`, `content`, `reward_points`, `view_count`, `answer_count`, `like_count`, `collection_count`, `is_solved`, `create_time`) VALUES
(6, 'React Hooks最佳实践是什么？', '最近在学习React Hooks，想了解一下Hooks的最佳实践和使用技巧，特别是useEffect和useMemo的使用场景。', 15, 145, 4, 12, 8, 1, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(7, 'TypeScript泛型怎么理解？', 'TypeScript的泛型总是让我很困惑，特别是泛型约束和条件类型，有没有通俗易懂的解释？', 25, 198, 6, 18, 10, 1, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(8, 'Docker容器化部署实战经验分享', '想了解一下Docker在生产环境中的部署经验，包括镜像优化、网络配置、数据持久化等方面。', 40, 267, 5, 22, 15, 0, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(9, 'Kubernetes入门学习路径推荐', '想学习Kubernetes，但是概念太多，有点无从下手，请问有什么好的学习路径和资源推荐？', 35, 234, 7, 25, 18, 0, NOW()),
(10, 'Git工作流最佳实践', '团队开发中Git工作流怎么选择？Git Flow、GitHub Flow、GitLab Flow各有什么优缺点？', 20, 178, 5, 15, 12, 1, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(11, 'Redis缓存穿透、击穿、雪崩怎么解决？', '最近在项目中使用Redis，遇到了缓存穿透、击穿、雪崩的问题，请问有什么好的解决方案？', 45, 312, 8, 30, 20, 1, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(12, 'MongoDB聚合查询怎么优化？', 'MongoDB的聚合查询性能很差，数据量大了之后查询很慢，请问有什么优化方法？', 30, 245, 4, 16, 11, 0, DATE_SUB(NOW(), INTERVAL 6 DAY)),
(13, 'Elasticsearch索引设计原则', '在Elasticsearch中设计索引时应该注意哪些原则？字段类型、分片、副本怎么配置？', 35, 289, 6, 21, 14, 0, DATE_SUB(NOW(), INTERVAL 7 DAY)),
(14, 'Webpack打包优化技巧', '项目打包后体积很大，加载很慢，请问Webpack有哪些打包优化的技巧？', 25, 201, 5, 17, 13, 1, DATE_SUB(NOW(), INTERVAL 8 DAY)),
(15, 'Vite相比Webpack有哪些优势？', '听说Vite比Webpack快很多，想了解一下Vite相比Webpack有哪些优势？适合在什么场景使用？', 20, 189, 6, 19, 15, 0, DATE_SUB(NOW(), INTERVAL 9 DAY)),
(16, 'Jenkins持续集成配置指南', '想搭建Jenkins持续集成环境，请问如何配置？包括Git集成、构建、部署等流程。', 50, 345, 7, 28, 22, 0, DATE_SUB(NOW(), INTERVAL 10 DAY)),
(17, 'GitLab CI/CD最佳实践', '在使用GitLab CI/CD时有哪些最佳实践？如何编写高效的.gitlab-ci.yml文件？', 40, 298, 5, 23, 16, 0, DATE_SUB(NOW(), INTERVAL 11 DAY)),
(5, 'Linux常用命令总结', '作为后端开发，Linux是必备技能，请问有哪些常用的Linux命令？如何高效使用？', 10, 156, 8, 14, 9, 1, DATE_SUB(NOW(), INTERVAL 12 DAY)),
(6, 'Nginx反向代理配置详解', '想了解Nginx反向代理的配置方法，包括负载均衡、SSL证书、缓存等配置。', 30, 223, 6, 20, 14, 0, DATE_SUB(NOW(), INTERVAL 13 DAY)),
(7, 'Node.js事件循环机制深度解析', 'Node.js的事件循环机制总是让我很困惑，microtask和macrotask的执行顺序是怎样的？', 35, 267, 7, 24, 17, 1, DATE_SUB(NOW(), INTERVAL 14 DAY));

-- ============================================
-- 5. 关联问题和标签
-- ============================================
INSERT INTO `biz_question_tag` (`question_id`, `tag_id`) VALUES
(6, 11), (6, 12),
(7, 12), (7, 8),
(8, 13), (8, 14),
(9, 14), (9, 13),
(10, 15), (10, 16),
(11, 17), (11, 3),
(12, 18), (12, 3),
(13, 19), (13, 18),
(14, 20), (14, 1),
(15, 21), (15, 5),
(16, 22), (16, 15),
(17, 23), (17, 15),
(18, 24), (18, 2),
(19, 25), (19, 2),
(20, 26), (20, 8);

-- 更新标签使用次数
UPDATE biz_tag SET use_count = (
    SELECT COUNT(*) FROM biz_question_tag WHERE biz_question_tag.tag_id = biz_tag.id
);

-- ============================================
-- 6. 插入更多测试回答
-- ============================================
INSERT INTO `biz_answer` (`question_id`, `user_id`, `content`, `like_count`, `comment_count`, `is_accepted`) VALUES
(6, 7, 'React Hooks最佳实践：\n1. 使用useCallback缓存函数，避免不必要的重渲染\n2. 使用useMemo缓存计算结果\n3. useEffect的依赖项要准确，避免无限循环\n4. 自定义Hooks抽离复用逻辑\n5. 避免在循环中使用Hooks\n6. 使用React.memo优化组件性能', 18, 3, 1),
(6, 8, '补充几点：\n- 使用useReducer管理复杂状态\n- 使用useContext传递全局状态\n- Hooks的调用顺序要保持一致\n- 不要在条件语句中使用Hooks', 12, 2, 0),
(7, 6, 'TypeScript泛型可以这样理解：\n泛型就像是一个占位符，在使用时才确定具体类型。\n\n泛型约束：\ninterface Lengthwise {\n  length: number;\n}\nfunction loggingIdentity<T extends Lengthwise>(arg: T): T {\n  console.log(arg.length);\n  return arg;\n}\n\n条件类型：\ntype NonNullable<T> = T extends null | undefined ? never : T;', 22, 4, 1),
(7, 9, '简单来说，泛型就是让函数、接口、类能够支持多种类型，提高代码复用性。', 10, 1, 0),
(8, 10, 'Docker容器化部署实战经验：\n1. 镜像优化：使用多阶段构建，减少镜像体积\n2. 网络配置：使用自定义网络，容器间通过服务名通信\n3. 数据持久化：使用Volume或Bind Mount\n4. 资源限制：设置CPU和内存限制\n5. 健康检查：配置HEALTHCHECK\n6. 日志管理：使用日志驱动，集中收集日志', 28, 5, 1),
(9, 11, 'Kubernetes学习路径：\n1. 先了解容器技术基础\n2. 学习Kubernetes核心概念：Pod、Service、Deployment\n3. 实践部署一个应用\n4. 学习配置管理：ConfigMap、Secret\n5. 学习存储：PV、PVC\n6. 学习网络：Ingress、NetworkPolicy\n7. 学习监控和日志\n8. 实践生产环境部署', 32, 6, 1),
(10, 12, 'Git工作流选择：\n- 小团队：GitHub Flow简单直接\n- 中等团队：GitLab Flow适合持续交付\n- 大团队：Git Flow适合有发布周期的项目\n\n关键是根据团队规模和发布周期选择合适的工作流。', 20, 3, 1),
(11, 13, 'Redis缓存问题解决方案：\n\n缓存穿透：\n1. 布隆过滤器\n2. 缓存空值\n\n缓存击穿：\n1. 互斥锁\n2. 热点数据永不过期\n\n缓存雪崩：\n1. 设置随机过期时间\n2. 使用缓存预热\n3. 限流降级', 35, 7, 1),
(12, 14, 'MongoDB聚合查询优化：\n1. 尽早使用$match过滤数据\n2. 合理使用索引\n3. 使用$project减少返回字段\n4. 使用$limit限制结果集\n5. 避免使用$lookup（性能差）', 18, 2, 0),
(13, 15, 'Elasticsearch索引设计原则：\n1. 字段类型选择要准确\n2. 分片数不宜过多（建议3-5个）\n3. 副本数根据查询量设置\n4. 使用合理的分词器\n5. 避免索引过多字段\n6. 定期优化索引', 25, 4, 1),
(14, 16, 'Webpack打包优化技巧：\n1. 使用Tree Shaking删除无用代码\n2. 代码分割：SplitChunksPlugin\n3. 按需加载：import()\n4. 压缩代码：TerserPlugin\n5. 使用CDN加载第三方库\n6. 开启Gzip压缩\n7. 使用缓存：contenthash', 22, 3, 1),
(15, 17, 'Vite相比Webpack的优势：\n1. 开发服务器启动快（使用ESM）\n2. 热更新速度快（按需编译）\n3. 配置简单，开箱即用\n4. 更好的TypeScript支持\n5. 更好的插件生态\n\n适合：新项目、Vue3项目、开发体验要求高的项目', 24, 4, 1),
(16, 18, 'Jenkins持续集成配置：\n1. 安装必要插件：Git、Pipeline、Docker等\n2. 配置Git凭据\n3. 创建Pipeline任务\n4. 编写Jenkinsfile\n5. 配置构建触发器\n6. 配置构建后操作：部署、通知等', 30, 5, 1),
(17, 19, 'GitLab CI/CD最佳实践：\n1. 使用缓存加速构建\n2. 并行执行job\n3. 使用artifacts传递文件\n4. 合理配置stages\n5. 使用only/except控制执行\n6. 配置环境变量\n7. 使用Docker镜像隔离环境', 26, 4, 1),
(18, 20, 'Linux常用命令：\n文件操作：ls, cd, pwd, cp, mv, rm, mkdir, touch, cat, less, tail\n权限管理：chmod, chown, chgrp\n进程管理：ps, top, kill, nohup\n网络：ping, ifconfig, netstat, curl, wget\n压缩：tar, gzip, zip, unzip\n查找：find, grep\n系统：df, du, free, uname', 16, 6, 1),
(19, 21, 'Nginx反向代理配置：\n\n基本配置：\nupstream backend {\n  server 127.0.0.1:8080;\n  server 127.0.0.1:8081;\n}\n\nserver {\n  listen 80;\n  server_name example.com;\n  location / {\n    proxy_pass http://backend;\n  }\n}\n\n负载均衡策略：round-robin、least_conn、ip_hash', 21, 3, 1),
(20, 22, 'Node.js事件循环机制：\n\n执行顺序：\n1. 同步代码\n2. microtask（Promise.then）\n3. macrotask（setTimeout、setInterval）\n4. 重复2、3\n\n关键点：\n- microtask优先于macrotask\n- 每个macrotask执行完后会清空所有microtask\n- 不同类型的macrotask执行顺序可能不同', 27, 5, 1);

-- ============================================
-- 7. 插入更多测试评论
-- ============================================
INSERT INTO `biz_comment` (`user_id`, `target_type`, `target_id`, `parent_id`, `content`, `create_time`) VALUES
(8, 2, 16, 0, '总结得很全面，收藏了！', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(9, 2, 16, 0, 'React.memo使用时要注意，如果props变化频繁，反而会降低性能。', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(10, 2, 16, 8, '确实，需要根据实际情况选择是否使用。', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(6, 2, 17, 0, '泛型约束这个例子很清楚，感谢！', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(7, 2, 17, 0, '条件类型还可以用来做类型推断，很强大。', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(8, 2, 18, 0, '多阶段构建确实能减少很多体积，我的镜像从500M减到了100M！', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(9, 2, 18, 0, '健康检查很重要，避免服务异常时还接收流量。', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(10, 2, 19, 0, '学习路径很清晰，按这个路线来应该没问题。', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(11, 2, 19, 0, '建议配合minikube或k3s本地实践。', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(12, 2, 20, 0, '我们团队用的是GitLab Flow，感觉还不错。', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(13, 2, 20, 0, 'GitHub Flow确实简单，适合快速迭代。', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(14, 2, 21, 0, '布隆过滤器的实现原理是什么？', DATE_SUB(NOW(), INTERVAL 6 DAY)),
(15, 2, 21, 14, '布隆过滤器使用多个哈希函数和位数组，可能存在误判但不会漏判。', DATE_SUB(NOW(), INTERVAL 6 DAY)),
(16, 2, 22, 0, '聚合查询确实慢，正在考虑换成Elasticsearch。', DATE_SUB(NOW(), INTERVAL 7 DAY)),
(17, 2, 22, 0, '$lookup确实很慢，建议在应用层关联。', DATE_SUB(NOW(), INTERVAL 7 DAY)),
(18, 2, 23, 0, '分片数设置多少比较合适？', DATE_SUB(NOW(), INTERVAL 8 DAY)),
(19, 2, 23, 18, '一般3-5个分片就够了，分片太多会增加管理开销。', DATE_SUB(NOW(), INTERVAL 8 DAY)),
(20, 2, 24, 0, 'Tree Shaking需要ES6模块支持，这个要注意。', DATE_SUB(NOW(), INTERVAL 9 DAY)),
(21, 2, 24, 0, 'SplitChunksPlugin配置有点复杂，有没有推荐的配置？', DATE_SUB(NOW(), INTERVAL 9 DAY)),
(22, 2, 25, 0, 'Vite确实快，开发体验好很多！', DATE_SUB(NOW(), INTERVAL 10 DAY)),
(23, 2, 25, 0, '但是生产环境打包还是Webpack更成熟。', DATE_SUB(NOW(), INTERVAL 10 DAY)),
(24, 2, 26, 0, 'Jenkinsfile用Pipeline语法还是Declarative语法？', DATE_SUB(NOW(), INTERVAL 11 DAY)),
(25, 2, 26, 24, '推荐使用Declarative语法，更易读易维护。', DATE_SUB(NOW(), INTERVAL 11 DAY)),
(26, 2, 27, 0, 'GitLab CI的缓存配置怎么写？', DATE_SUB(NOW(), INTERVAL 12 DAY)),
(27, 2, 27, 26, '使用cache关键字，指定paths和key。', DATE_SUB(NOW(), INTERVAL 12 DAY)),
(28, 2, 28, 0, 'grep命令很强大，推荐配合正则表达式使用。', DATE_SUB(NOW(), INTERVAL 13 DAY)),
(29, 2, 28, 0, '建议学习awk和sed，文本处理更高效。', DATE_SUB(NOW(), INTERVAL 13 DAY)),
(30, 2, 29, 0, 'Nginx的负载均衡策略怎么选择？', DATE_SUB(NOW(), INTERVAL 14 DAY)),
(31, 2, 29, 30, '一般用round-robin，如果需要会话保持用ip_hash。', DATE_SUB(NOW(), INTERVAL 14 DAY)),
(32, 2, 30, 0, 'macrotask的执行顺序是固定的吗？', DATE_SUB(NOW(), INTERVAL 15 DAY)),
(33, 2, 30, 32, '不是固定的，不同类型的macrotask执行顺序可能不同。', DATE_SUB(NOW(), INTERVAL 15 DAY));

-- ============================================
-- 8. 插入更多测试点赞记录
-- ============================================
INSERT INTO `biz_like_record` (`user_id`, `target_type`, `target_id`) VALUES
(6, 2, 16), (7, 2, 16), (8, 2, 16), (9, 2, 16), (10, 2, 16),
(7, 2, 17), (8, 2, 17), (9, 2, 17), (10, 2, 17), (11, 2, 17),
(8, 2, 18), (9, 2, 18), (10, 2, 18), (11, 2, 18), (12, 2, 18),
(9, 2, 19), (10, 2, 19), (11, 2, 19), (12, 2, 19), (13, 2, 19),
(10, 2, 20), (11, 2, 20), (12, 2, 20), (13, 2, 20), (14, 2, 20),
(11, 2, 21), (12, 2, 21), (13, 2, 21), (14, 2, 21), (15, 2, 21),
(12, 2, 22), (13, 2, 22), (14, 2, 22), (15, 2, 22), (16, 2, 22),
(13, 2, 23), (14, 2, 23), (15, 2, 23), (16, 2, 23), (17, 2, 23),
(14, 2, 24), (15, 2, 24), (16, 2, 24), (17, 2, 24), (18, 2, 24),
(15, 2, 25), (16, 2, 25), (17, 2, 25), (18, 2, 25), (19, 2, 25),
(16, 2, 26), (17, 2, 26), (18, 2, 26), (19, 2, 26), (20, 2, 26),
(17, 2, 27), (18, 2, 27), (19, 2, 27), (20, 2, 27), (21, 2, 27),
(18, 2, 28), (19, 2, 28), (20, 2, 28), (21, 2, 28), (22, 2, 28),
(19, 2, 29), (20, 2, 29), (21, 2, 29), (22, 2, 29), (23, 2, 29),
(20, 2, 30), (21, 2, 30), (22, 2, 30), (23, 2, 30), (24, 2, 30);

-- ============================================
-- 9. 插入更多测试收藏记录
-- ============================================
INSERT INTO `biz_collection` (`user_id`, `target_type`, `target_id`) VALUES
(6, 1, 6), (7, 1, 6), (8, 1, 6),
(7, 1, 7), (8, 1, 7), (9, 1, 7),
(8, 1, 8), (9, 1, 8), (10, 1, 8),
(9, 1, 9), (10, 1, 9), (11, 1, 9),
(10, 1, 10), (11, 1, 10), (12, 1, 10),
(11, 1, 11), (12, 1, 11), (13, 1, 11),
(12, 1, 12), (13, 1, 12), (14, 1, 12),
(13, 1, 13), (14, 1, 13), (15, 1, 13),
(14, 1, 14), (15, 1, 14), (16, 1, 14),
(15, 1, 15), (16, 1, 15), (17, 1, 15),
(16, 1, 16), (17, 1, 16), (18, 1, 16),
(17, 1, 17), (18, 1, 17), (19, 1, 17),
(18, 1, 18), (19, 1, 18), (20, 1, 18),
(19, 1, 19), (20, 1, 19), (21, 1, 19),
(20, 1, 20), (21, 1, 20), (22, 1, 20);

-- ============================================
-- 10. 更新问题的统计数据
-- ============================================
UPDATE biz_question q
SET
    answer_count = (SELECT COUNT(*) FROM biz_answer WHERE question_id = q.id),
    like_count = (SELECT COUNT(*) FROM biz_like_record WHERE target_type = 1 AND target_id = q.id),
    collection_count = (SELECT COUNT(*) FROM biz_collection WHERE target_type = 1 AND target_id = q.id),
    is_solved = (SELECT COUNT(*) > 0 FROM biz_answer WHERE question_id = q.id AND is_accepted = 1);

-- ============================================
-- 11. 更新回答的统计数据
-- ============================================
UPDATE biz_answer a
SET
    like_count = (SELECT COUNT(*) FROM biz_like_record WHERE target_type = 2 AND target_id = a.id),
    comment_count = (SELECT COUNT(*) FROM biz_comment WHERE target_type = 2 AND target_id = a.id);

-- ============================================
-- 12. 插入一些测试消息通知
-- ============================================
INSERT INTO `sys_notice` (`receiver_id`, `sender_id`, `type`, `content`, `target_type`, `target_id`, `is_read`) VALUES
(2, 3, 2, 'zhangsan 回答了你的问题 "如何学习Vue 3？"', 1, 1, 1),
(2, 4, 2, 'lisi 回答了你的问题 "如何学习Vue 3？"', 1, 1, 1),
(3, 2, 1, 'zhangsan 点赞了你的回答', 2, 1, 0),
(4, 2, 1, 'zhangsan 点赞了你的回答', 2, 2, 0),
(6, 7, 2, 'sunqi 回答了你的问题 "React Hooks最佳实践是什么？"', 1, 6, 0),
(7, 6, 1, 'zhaoliu 点赞了你的回答', 2, 17, 0),
(8, 10, 2, 'wujiu 回答了你的问题 "Docker容器化部署实战经验分享"', 1, 8, 0),
(9, 11, 2, 'zhengshi 回答了你的问题 "Kubernetes入门学习路径推荐"', 1, 9, 0),
(10, 12, 2, 'wangmazi 回答了你的问题 "Git工作流最佳实践"', 1, 10, 0),
(11, 13, 2, 'chenxi 回答了你的问题 "Redis缓存穿透、击穿、雪崩怎么解决？"', 1, 11, 0);

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 数据统计
-- ============================================
SELECT '========================================' AS '';
SELECT '测试数据插入完成！' AS '';
SELECT '========================================' AS '';
SELECT CONCAT('用户数: ', COUNT(*)) AS '' FROM sys_user;
SELECT CONCAT('问题数: ', COUNT(*)) AS '' FROM biz_question;
SELECT CONCAT('回答数: ', COUNT(*)) AS '' FROM biz_answer;
SELECT CONCAT('评论数: ', COUNT(*)) AS '' FROM biz_comment;
SELECT CONCAT('标签数: ', COUNT(*)) AS '' FROM biz_tag;
SELECT CONCAT('点赞记录: ', COUNT(*)) AS '' FROM biz_like_record;
SELECT CONCAT('收藏记录: ', COUNT(*)) AS '' FROM biz_collection;
SELECT CONCAT('通知数: ', COUNT(*)) AS '' FROM sys_notice;
SELECT '========================================' AS '';
SELECT '✅ 数据库已丰富，可以开始测试了！' AS '';
SELECT '========================================' AS '';
