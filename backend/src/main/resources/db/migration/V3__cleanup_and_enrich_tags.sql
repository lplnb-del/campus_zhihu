-- ==========================================
-- 清理并重建标签数据
-- ==========================================

-- 1. 删除所有测试标签
DELETE FROM biz_tag WHERE name LIKE '交互测试%' OR name LIKE 'Tag_q%';

-- 2. 删除问题标签关联表中的无效数据
DELETE FROM biz_question_tag WHERE tag_id NOT IN (SELECT id FROM biz_tag);

-- 3. 更新现有标签的描述和分类
UPDATE biz_tag SET description = 'Java 是一门面向对象的编程语言，不仅吸收了 C++ 语言的各种优点，还摒弃了 C++ 里难以理解的多继承、指针等概念。', category = 'programming', icon = 'Coffee', color = '#f89820' WHERE name = 'Java' AND id = 1;
UPDATE biz_tag SET description = 'Python 是一种跨平台的计算机程序设计语言，是一种面向对象的动态类型语言，最初被设计用于编写自动化脚本。', category = 'programming', icon = 'Document', color = '#3776ab' WHERE name = 'Python' AND id = 2;
UPDATE biz_tag SET description = 'JavaScript 是一种具有函数优先的轻量级，解释型或即时编译型的编程语言，主要作为 Web 页面的脚本语言。', category = 'programming', icon = 'VideoPlay', color = '#f7df1e' WHERE name = 'JavaScript' AND id = 3;
UPDATE biz_tag SET description = 'C++ 是一种通用的编程语言，支持多种编程范式，包括面向对象、泛型和函数式编程。', category = 'programming', icon = 'Cpu', color = '#00599c' WHERE name = 'C++' AND id = 4;
UPDATE biz_tag SET description = 'Go 是 Google 开发的开源编程语言，旨在提高程序员在多核处理器上的工作效率。', category = 'programming', icon = 'Lightning', color = '#00add8' WHERE name = 'Go' AND id = 5;
UPDATE biz_tag SET description = 'React 是由 Facebook 开发的用于构建用户界面的 JavaScript 库。', category = 'frontend', icon = 'Monitor', color = '#61dafb' WHERE name = 'React' AND id = 6;
UPDATE biz_tag SET description = 'Vue 是一套用于构建用户界面的渐进式框架，核心库只关注视图层。', category = 'frontend', icon = 'View', color = '#42b883' WHERE name = 'Vue' AND id = 7;
UPDATE biz_tag SET description = 'Angular 是一个由 Google 维护的开源 Web 应用框架，用于构建单页应用。', category = 'frontend', icon = 'Compass', color = '#dd0031' WHERE name = 'Angular' AND id = 8;
UPDATE biz_tag SET description = 'Spring Boot 是基于 Spring 框架的快速开发框架，通过约定优于配置的理念，让开发者可以快速构建生产级别的 Spring 应用。', category = 'backend', icon = 'Briefcase', color = '#6db33f' WHERE name = 'Spring Boot' AND id = 9;
UPDATE biz_tag SET description = 'Spring Cloud 是一系列框架的有序集合，它利用 Spring Boot 的开发便利性简化了分布式系统基础设施的开发。', category = 'backend', icon = 'Connection', color = '#6db33f' WHERE name = 'Spring Cloud' AND id = 10;
UPDATE biz_tag SET description = 'MySQL 是最流行的开源关系型数据库管理系统之一，广泛应用于各种 Web 应用程序中。', category = 'database', icon = 'Coin', color = '#4479a1' WHERE name = 'MySQL' AND id = 11;
UPDATE biz_tag SET description = 'TypeScript 是 JavaScript 的一个超集，添加了可选的静态类型和基于类的面向对象编程，让代码更加健壮和易于维护。', category = 'programming', icon = 'Reading', color = '#3178c6' WHERE name = 'TypeScript' AND id = 12;
UPDATE biz_tag SET description = 'Node.js 是一个基于 Chrome V8 引擎的 JavaScript 运行环境，使用了一个事件驱动、非阻塞式 I/O 的模型，使其轻量又高效。', category = 'backend', icon = 'MoreFilled', color = '#339933' WHERE name = 'Node.js' AND id = 13;
UPDATE biz_tag SET description = 'Docker 是一个开源的应用容器引擎，让开发者可以打包他们的应用以及依赖包到一个可移植的容器中，然后发布到任何流行的 Linux 机器上，也可以实现虚拟化。', category = 'devops', icon = 'Monitor', color = '#2496ed' WHERE name = 'Docker' AND id = 14;

-- 4. 插入新的标签（使用 INSERT IGNORE 避免重复）
INSERT IGNORE INTO biz_tag (name, description, category, icon, color, use_count, create_time, update_time) VALUES
('Linux', 'Linux 是一种自由和开放源码的类 UNIX 操作系统内核，广泛应用于服务器和嵌入式设备。', 'devops', 'Monitor', '#fcc624', 5, NOW(), NOW()),
('Git', 'Git 是一个开源的分布式版本控制系统，可以有效、高速地处理从很小到非常大的项目版本管理。', 'devops', 'FolderOpened', '#f05032', 3, NOW(), NOW()),
('Redis', 'Redis 是一个开源的内存数据结构存储系统，可以用作数据库、缓存和消息中间件。', 'database', 'Coffee', '#dc382d', 4, NOW(), NOW()),
('MongoDB', 'MongoDB 是一个基于文档的 NoSQL 数据库，具有高性能、高可用性和可扩展性。', 'database', 'Coin', '#47a248', 2, NOW(), NOW()),
('RabbitMQ', 'RabbitMQ 是一个开源的消息代理软件，实现了高级消息队列协议（AMQP）。', 'devops', 'Message', '#ff6600', 1, NOW(), NOW()),
('Kubernetes', 'Kubernetes 是一个开源的容器编排平台，用于自动化部署、扩展和管理容器化应用程序。', 'devops', 'Platform', '#326ce5', 3, NOW(), NOW()),
('Nginx', 'Nginx 是一个高性能的 HTTP 和反向代理 web 服务器，同时也提供了 IMAP/POP3/SMTP 服务。', 'devops', 'Connection', '#009639', 2, NOW(), NOW()),
('Jenkins', 'Jenkins 是一个开源的自动化服务器，用于自动化各种与构建、测试和部署相关的任务。', 'devops', 'Tools', '#d33833', 1, NOW(), NOW()),
('HTML5', 'HTML5 是 HTML 的最新版本，引入了许多新的语义元素和 API，使 Web 开发更加简单和强大。', 'frontend', 'Document', '#e34c26', 6, NOW(), NOW()),
('CSS3', 'CSS3 是 CSS 的最新版本，引入了许多新的样式和动画特性，使网页设计更加灵活和美观。', 'frontend', 'Brush', '#1572b6', 5, NOW(), NOW()),
('Webpack', 'Webpack 是一个现代 JavaScript 应用程序的静态模块打包器，它将所有依赖项打包成一个或多个 bundle。', 'frontend', 'Box', '#8dd6f9', 2, NOW(), NOW()),
('Vite', 'Vite 是一个新一代前端构建工具，利用浏览器原生 ES 模块导入，提供了极快的开发服务器启动速度。', 'frontend', 'Lightning', '#646cff', 3, NOW(), NOW()),
('Rust', 'Rust 是一门系统编程语言，专注于安全、并发和性能，同时保持了开发效率。', 'programming', 'Cpu', '#dea584', 1, NOW(), NOW()),
('Kotlin', 'Kotlin 是一种在 Java 虚拟机上运行的静态类型编程语言，被 Android 官方推荐用于 Android 开发。', 'programming', 'Document', '#7f52ff', 2, NOW(), NOW()),
('Swift', 'Swift 是 Apple 公司推出的一种编程语言，用于开发 iOS、macOS、watchOS 和 tvOS 应用程序。', 'programming', 'Document', '#f05138', 1, NOW(), NOW()),
('PHP', 'PHP 是一种通用开源脚本语言，尤其适用于 Web 开发并可嵌入 HTML 中。', 'backend', 'Document', '#777bb4', 2, NOW(), NOW()),
('Ruby', 'Ruby 是一种动态、反射、面向对象的编程语言，以其简洁优雅的语法而闻名。', 'programming', 'Document', '#cc342d', 1, NOW(), NOW()),
('Scala', 'Scala 是一门多范式的编程语言，集成了面向对象编程和函数式编程的特性。', 'programming', 'Document', '#dc322f', 1, NOW(), NOW()),
('Elasticsearch', 'Elasticsearch 是一个基于 Lucene 的搜索引擎，提供了分布式、多用户能力的全文搜索引擎。', 'database', 'Search', '#005571', 2, NOW(), NOW()),
('PostgreSQL', 'PostgreSQL 是一个强大的开源对象关系数据库系统，以其可靠性和数据完整性而闻名。', 'database', 'Coin', '#336791', 2, NOW(), NOW()),
('GraphQL', 'GraphQL 是一种用于 API 的查询语言，提供了一种更高效、强大和灵活的方式来描述数据需求。', 'backend', 'Connection', '#e535ab', 1, NOW(), NOW()),
('RESTful', 'RESTful 是一种软件架构风格，用于设计网络应用程序的 API。', 'backend', 'Connection', '#009688', 3, NOW(), NOW()),
('Microservices', '微服务架构是一种将单一应用程序开发为一套小型服务的方法，每个服务运行在自己的进程中。', 'architecture', 'Platform', '#6db33f', 2, NOW(), NOW()),
('Serverless', 'Serverless 是一种云计算执行模型，云提供商动态分配机器资源。', 'architecture', 'Cloud', '#fd9f28', 1, NOW(), NOW()),
('DevOps', 'DevOps 是一种重视软件开发人员和 IT 运维人员之间沟通合作的文化、运动或惯例。', 'devops', 'Tools', '#326ce5', 4, NOW(), NOW()),
('CI/CD', 'CI/CD 是持续集成和持续部署的缩写，是一种软件开发实践，旨在自动化软件交付过程。', 'devops', 'Tools', '#009688', 3, NOW(), NOW()),
('Testing', '软件测试是评估软件属性或能力的过程，以确定它是否满足预期结果。', 'testing', 'CircleCheck', '#009688', 2, NOW(), NOW()),
('Unit Testing', '单元测试是对软件中最小的可测试单元进行检查和验证的过程。', 'testing', 'CircleCheck', '#009688', 1, NOW(), NOW()),
('Integration Testing', '集成测试是将多个单元组合在一起进行测试的过程，以验证它们之间的交互。', 'testing', 'CircleCheck', '#009688', 1, NOW(), NOW()),
('Performance', '性能优化是提高软件系统响应速度和资源利用率的过程。', 'optimization', 'Lightning', '#ff6600', 2, NOW(), NOW()),
('Security', '网络安全是保护网络和数据免受未经授权的访问或攻击的措施。', 'security', 'Lock', '#009688', 3, NOW(), NOW()),
('Algorithm', '算法是解决特定问题的一系列明确指令，是计算机科学的核心。', 'algorithm', 'Cpu', '#009688', 4, NOW(), NOW()),
('Data Structure', '数据结构是计算机存储、组织数据的方式，是算法的基础。', 'algorithm', 'Document', '#009688', 3, NOW(), NOW()),
('Machine Learning', '机器学习是人工智能的一个分支，通过算法让计算机从数据中学习。', 'ai', 'MagicStick', '#f59e0b', 2, NOW(), NOW()),
('Deep Learning', '深度学习是机器学习的一个子集，使用多层神经网络来学习数据的表示。', 'ai', 'MagicStick', '#f59e0b', 1, NOW(), NOW()),
('Natural Language Processing', '自然语言处理是人工智能的一个分支，致力于让计算机理解和生成人类语言。', 'ai', 'ChatDotRound', '#f59e0b', 1, NOW(), NOW()),
('Computer Vision', '计算机视觉是人工智能的一个分支，致力于让计算机理解和解释视觉信息。', 'ai', 'Camera', '#f59e0b', 1, NOW(), NOW()),
('Mobile Development', '移动开发是指为移动设备（如智能手机和平板电脑）开发应用程序的过程。', 'mobile', 'Cellphone', '#009688', 3, NOW(), NOW()),
('iOS Development', 'iOS 开发是指为 Apple 的 iOS 操作系统开发应用程序的过程。', 'mobile', 'Iphone', '#009688', 2, NOW(), NOW()),
('Android Development', 'Android 开发是指为 Google 的 Android 操作系统开发应用程序的过程。', 'mobile', 'Cellphone', '#009688', 2, NOW(), NOW()),
('Cross Platform', '跨平台开发是指使用一套代码在多个平台上运行的开发方式。', 'mobile', 'Platform', '#009688', 1, NOW(), NOW()),
('Cloud Computing', '云计算是通过互联网提供计算服务的模式，包括服务器、存储、数据库、网络等。', 'cloud', 'Cloud', '#009688', 3, NOW(), NOW()),
('AWS', 'Amazon Web Services 是亚马逊提供的云计算平台服务。', 'cloud', 'Cloud', '#ff9900', 2, NOW(), NOW()),
('Azure', 'Microsoft Azure 是微软提供的云计算平台服务。', 'cloud', 'Cloud', '#0089d6', 1, NOW(), NOW()),
('Google Cloud', 'Google Cloud Platform 是 Google 提供的云计算平台服务。', 'cloud', 'Cloud', '#4285f4', 1, NOW(), NOW()),
('Big Data', '大数据是指无法在一定时间范围内用常规软件工具进行捕捉、管理和处理的数据集合。', 'data', 'DataLine', '#009688', 2, NOW(), NOW()),
('Data Mining', '数据挖掘是从大量数据中发现有用信息的过程。', 'data', 'DataAnalysis', '#009688', 1, NOW(), NOW()),
('Data Visualization', '数据可视化是将数据以图形或图像的形式呈现，以便更好地理解和分析数据。', 'data', 'TrendCharts', '#009688', 1, NOW(), NOW()),
('Agile', '敏捷开发是一种以人为核心、迭代、循序渐进的开发方法。', 'methodology', 'Compass', '#009688', 2, NOW(), NOW()),
('Scrum', 'Scrum 是一种敏捷软件开发框架，用于管理和控制复杂的产品开发。', 'methodology', 'Compass', '#009688', 1, NOW(), NOW()),
('Kanban', '看板是一种可视化的工作管理方法，用于优化工作流程。', 'methodology', 'Compass', '#009688', 1, NOW(), NOW());
