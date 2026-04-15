SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

USE campus_zhihu;

-- 插入测试标签
INSERT INTO `biz_tag` (`name`, `description`, `icon`, `color`, `use_count`) VALUES
('React', 'React框架学习与实践', '⚛️', '#61dafb', 0),
('TypeScript', 'TypeScript编程语言', '📘', '#3178c6', 0),
('Node.js', 'Node.js后端开发', '🟢', '#339933', 0),
('Docker', '容器化技术', '🐳', '#2496ed', 0),
('Kubernetes', '容器编排平台', '☸️', '#326ce5', 0),
('Git', '版本控制系统', '📦', '#f05032', 0),
('Linux', 'Linux操作系统', '🐧', '#fcc624', 0),
('Redis', 'Redis缓存数据库', '🔴', '#dc382d', 0);

-- 插入测试问题
INSERT INTO `biz_question` (`user_id`, `title`, `content`, `reward_points`, `view_count`, `answer_count`, `like_count`, `collection_count`, `is_solved`, `create_time`) VALUES
(6, 'React Hooks最佳实践是什么？', '最近在学习React Hooks，想了解一下Hooks的最佳实践和使用技巧，特别是useEffect和useMemo的使用场景。', 15, 145, 2, 12, 8, 1, NOW()),
(7, 'TypeScript泛型怎么理解？', 'TypeScript的泛型总是让我很困惑，特别是泛型约束和条件类型，有没有通俗易懂的解释？', 25, 198, 2, 18, 10, 1, NOW()),
(8, 'Docker容器化部署实战经验分享', '想了解一下Docker在生产环境中的部署经验，包括镜像优化、网络配置、数据持久化等方面。', 40, 267, 2, 22, 15, 0, NOW()),
(9, 'Kubernetes入门学习路径推荐', '想学习Kubernetes，但是概念太多，有点无从下手，请问有什么好的学习路径和资源推荐？', 35, 234, 2, 25, 18, 0, NOW()),
(10, 'Git工作流最佳实践', '团队开发中Git工作流怎么选择？Git Flow、GitHub Flow、GitLab Flow各有什么优缺点？', 20, 178, 2, 15, 12, 1, NOW());

-- 关联问题和标签
INSERT INTO `biz_question_tag` (`question_id`, `tag_id`) VALUES
(6, 11), (6, 12),
(7, 12), (7, 8),
(8, 13), (8, 14),
(9, 14), (9, 13),
(10, 15), (10, 16);

-- 更新标签使用次数
UPDATE biz_tag SET use_count = (SELECT COUNT(*) FROM biz_question_tag WHERE biz_question_tag.tag_id = biz_tag.id);

-- 插入测试回答
INSERT INTO `biz_answer` (`question_id`, `user_id`, `content`, `like_count`, `comment_count`, `is_accepted`) VALUES
(6, 7, 'React Hooks最佳实践：
1. 使用useCallback缓存函数，避免不必要的重渲染
2. 使用useMemo缓存计算结果
3. useEffect的依赖项要准确，避免无限循环
4. 自定义Hooks抽离复用逻辑
5. 避免在循环中使用Hooks
6. 使用React.memo优化组件性能', 18, 2, 1),
(6, 8, '补充几点：
- 使用useReducer管理复杂状态
- 使用useContext传递全局状态
- Hooks的调用顺序要保持一致
- 不要在条件语句中使用Hooks', 12, 1, 0),
(7, 6, 'TypeScript泛型可以这样理解：
泛型就像是一个占位符，在使用时才确定具体类型。

泛型约束：
interface Lengthwise {
  length: number;
}
function loggingIdentity<T extends Lengthwise>(arg: T): T {
  console.log(arg.length);
  return arg;
}

条件类型：
type NonNullable<T> = T extends null | undefined ? never : T;', 22, 2, 1),
(7, 9, '简单来说，泛型就是让函数、接口、类能够支持多种类型，提高代码复用性。', 10, 0, 0),
(8, 10, 'Docker容器化部署实战经验：
1. 镜像优化：使用多阶段构建，减少镜像体积
2. 网络配置：使用自定义网络，容器间通过服务名通信
3. 数据持久化：使用Volume或Bind Mount
4. 资源限制：设置CPU和内存限制
5. 健康检查：配置HEALTHCHECK
6. 日志管理：使用日志驱动，集中收集日志', 28, 2, 1),
(8, 9, '补充：
- 使用.dockerignore排除不必要的文件
- 合理设置镜像标签
- 定期清理无用镜像和容器', 15, 1, 0),
(9, 11, 'Kubernetes学习路径：
1. 先了解容器技术基础
2. 学习Kubernetes核心概念：Pod、Service、Deployment
3. 实践部署一个应用
4. 学习配置管理：ConfigMap、Secret
5. 学习存储：PV、PVC
6. 学习网络：Ingress、NetworkPolicy
7. 学习监控和日志
8. 实践生产环境部署', 32, 3, 1),
(9, 10, '推荐学习资源：
- Kubernetes官方文档
- Kubernetes中文社区
- minikube或k3s本地实践环境
- 尚硅谷Kubernetes教程', 18, 1, 0),
(10, 12, 'Git工作流选择：
- 小团队：GitHub Flow简单直接
- 中等团队：GitLab Flow适合持续交付
- 大团队：Git Flow适合有发布周期的项目

关键是根据团队规模和发布周期选择合适的工作流。', 20, 2, 1),
(10, 11, 'Git Flow特点：
- 主分支：master（生产）、develop（开发）
- 支持分支：feature、release、hotfix
- 适合有严格发布周期的项目
- 分支管理复杂，适合大团队', 15, 1, 0);

-- 插入测试评论
INSERT INTO `biz_comment` (`user_id`, `target_type`, `target_id`, `parent_id`, `content`, `create_time`) VALUES
(8, 2, 16, 0, '总结得很全面，收藏了！', NOW()),
(9, 2, 16, 0, 'React.memo使用时要注意，如果props变化频繁，反而会降低性能。', NOW()),
(10, 2, 16, 8, '确实，需要根据实际情况选择是否使用。', NOW()),
(6, 2, 17, 0, '泛型约束这个例子很清楚，感谢！', NOW()),
(7, 2, 17, 0, '条件类型还可以用来做类型推断，很强大。', NOW()),
(8, 2, 18, 0, '多阶段构建确实能减少很多体积，我的镜像从500M减到了100M！', NOW()),
(9, 2, 18, 0, '健康检查很重要，避免服务异常时还接收流量。', NOW()),
(10, 2, 19, 0, '学习路径很清晰，按这个路线来应该没问题。', NOW()),
(11, 2, 19, 0, '建议配合minikube或k3s本地实践。', NOW()),
(12, 2, 20, 0, '我们团队用的是GitLab Flow，感觉还不错。', NOW()),
(13, 2, 20, 0, 'GitHub Flow确实简单，适合快速迭代。', NOW());

-- 插入测试点赞记录
INSERT INTO `biz_like_record` (`user_id`, `target_type`, `target_id`) VALUES
(6, 2, 16), (7, 2, 16), (8, 2, 16), (9, 2, 16), (10, 2, 16),
(7, 2, 17), (8, 2, 17), (9, 2, 17), (10, 2, 17), (11, 2, 17),
(8, 2, 18), (9, 2, 18), (10, 2, 18), (11, 2, 18), (12, 2, 18),
(9, 2, 19), (10, 2, 19), (11, 2, 19), (12, 2, 19), (13, 2, 19),
(10, 2, 20), (11, 2, 20), (12, 2, 20), (13, 2, 20), (14, 2, 20);

-- 插入测试收藏记录
INSERT INTO `biz_collection` (`user_id`, `target_type`, `target_id`) VALUES
(6, 1, 6), (7, 1, 6), (8, 1, 6),
(7, 1, 7), (8, 1, 7), (9, 1, 7),
(8, 1, 8), (9, 1, 8), (10, 1, 8),
(9, 1, 9), (10, 1, 9), (11, 1, 9),
(10, 1, 10), (11, 1, 10), (12, 1, 10);

SET FOREIGN_KEY_CHECKS = 1;

SELECT 'Test data inserted successfully' as status;