# CLAUDE.md

本文件为 Claude Code (claude.ai/code) 在此仓库中工作时提供指导。

## 项目概述

CampusZhihu（校园知乎）是一个前后端分离的高校知识共享问答平台，支持问题发布、回答、评论、点赞、收藏、积分悬赏等核心功能，采用 Vue 3 + Spring Boot 现代化技术栈。

## 常用命令

### 前端

```bash
cd frontend

# 安装依赖（使用 pnpm）
pnpm install

# 启动开发服务器（端口 3000）
pnpm dev

# 构建生产版本
pnpm build

# 代码检查
pnpm lint

# 代码格式化
pnpm format
```

### 后端

```bash
cd backend

# 构建项目
mvn clean install

# 启动应用
mvn spring-boot:run

# 仅编译
mvn compile

# 运行测试
mvn test
```

## 架构

### 项目结构

```
campus_zhihu/
├── backend/                    # Spring Boot 后端
│   └── src/main/java/com/campus/zhihu/
│       ├── controller/         # 控制器层
│       ├── service/impl/      # 服务实现
│       ├── mapper/            # 数据访问层
│       ├── entity/            # 实体类
│       ├── dto/               # 数据传输对象
│       ├── vo/                # 视图对象
│       ├── common/            # 通用组件
│       │   ├── Result.java              # 统一响应结果
│       │   ├── ResultCode.java          # 状态码枚举
│       │   ├── BusinessException.java   # 业务异常
│       │   └── GlobalExceptionHandler.java
│       ├── config/            # 配置类
│       └── util/              # 工具类
│
├── frontend/                   # Vue 3 前端
│   └── src/
│       ├── api/               # API 接口模块
│       │   ├── answer.ts      # 回答接口
│       │   ├── user.ts        # 用户接口
│       │   ├── question.ts    # 问题接口
│       │   ├── tag.ts         # 标签接口
│       │   ├── collection.ts  # 收藏接口
│       │   ├── interaction.ts # 交互接口
│       │   └── file.ts        # 文件接口
│       ├── components/        # 公共组件
│       ├── views/             # 页面视图
│       ├── router/index.ts    # 路由配置
│       ├── stores/user.ts     # Pinia 用户状态
│       └── utils/request.ts   # Axios 封装
│
└── docs/
    └── schema.sql             # 数据库建表脚本
```

### 前端核心模块

| 模块 | 文件 | 职责 |
|------|------|------|
| 路由 | `router/index.ts` | 路由守卫、NProgress 进度条、登录拦截 |
| 状态 | `stores/user.ts` | 用户信息、Token、积分管理（Pinia + 持久化） |
| 请求 | `utils/request.ts` | Axios 封装、自动 Token 注入、响应拦截 |
| 视图 | `views/` | Home、Questions、QuestionDetail、AskQuestion、Tags、user/*、auth/* |

### 后端核心模块

| 模块 | 文件 | 职责 |
|------|------|------|
| 统一响应 | `common/Result.java` | 标准 JSON 响应格式 {code, msg, data, timestamp} |
| 异常处理 | `common/GlobalExceptionHandler.java` | 全局异常拦截，返回 JSON 错误 |
| 积分事务 | `BizAnswerServiceImpl` | 采纳回答时原子性扣除/增加积分、乐观锁 |

### 数据流

```
用户操作 -> Vue Component -> Pinia Store -> Axios API
                                              |
                                              v
                                      Spring Boot Controller
                                              |
                                              v
                                      MyBatis-Plus Mapper
                                              |
                                              v
                                           MySQL
```

## 技术栈

- **前端框架**: Vue 3.4 + TypeScript 5.3 + Vite 5.0
- **UI 组件**: Element Plus 2.5（按需引入 + Sass 主题定制）
- **状态管理**: Pinia 2.1（持久化到 localStorage）
- **路由**: Vue Router 4.2（懒加载、导航守卫）
- **后端框架**: Spring Boot 3.2 + JDK 17
- **ORM**: MyBatis-Plus 3.5
- **数据库**: MySQL 8.0 + Druid 连接池
- **认证**: JWT 0.12（7 天有效期）+ Spring Security BCrypt
- **文件存储**: MinIO 8.5
- **缓存**: Redis

## 编码规范

### 前端规范

- **组件命名**: 大驼峰，如 `QuestionCard.vue`
- **文件命名**: 小驼峰，如 `userApi.ts`
- **TypeScript**: 禁止使用 `any`，启用严格类型检查
- **语法**: 使用 `<script setup>` 组合式 API
- **样式**: 使用 Scss，Element Plus 主题变量在 `assets/styles/element-vars.scss`
- **路径别名**: `@/` 和 `~/` 均指向 `src/`

### 后端规范

- **命名**: 驼峰命名法，类名首字母大写
- **注释**: 所有 public 方法必须添加 JavaDoc
- **异常**: 使用 `BusinessException` 抛出业务异常
- **事务**: 涉及多表操作必须添加 `@Transactional`
- **日志**: 使用 Slf4j，合理使用 debug/info/warn/error 级别

### 代码质量

- 文件长度: 最多 800 行
- 函数长度: < 50 行
- 嵌套层级: < 4 层
- 禁止硬编码密钥（使用环境变量或配置中心）

## 状态管理

### Pinia Store (`stores/user.ts`)

```typescript
// State
token: string
userInfo: UserInfo | null
isLoggedIn: boolean

// Getters
userId, username, avatar, points, hasToken

// Actions
login(), logout(), setToken(), updateUserInfo(), addPoints(), subtractPoints()
```

**持久化**: Token 和用户信息自动保存到 localStorage

## API 设计

### 统一响应格式

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {...},
  "timestamp": 1234567890
}
```

### 认证方式

- Token 存储在 `localStorage`
- 请求头: `Authorization: Bearer <token>`
- Axios 拦截器自动注入 Token

### 主要接口

| 模块 | 接口 |
|------|------|
| 用户 | `/user/register`, `/user/login`, `/user/info`, `/user/update` |
| 问题 | `/question/add`, `/question/list`, `/question/{id}`, `/question/update`, `/question/delete` |
| 回答 | `/answer/add`, `/answer/list`, `/answer/accept/{id}`, `/answer/like/{id}` |
| 标签 | `/tag/list`, `/tag/hot`, `/tag/add` |
| 收藏 | `/collection/add`, `/collection/remove`, `/collection/list` |
| 通知 | `/notice/list`, `/notice/read/{id}`, `/notice/unread-count` |

## 安全机制

- **密码加密**: BCrypt 算法（盐值自动生成）
- **JWT**: 7 天有效期，过期自动跳转登录页
- **请求拦截**: 后端 Filter 验证 Token，前端 Axios 自动添加
- **CORS**: 后端已配置跨域，前端使用 Vite 代理（开发环境）

## 测试要求

- **后端测试**: JUnit 5 + Spring Boot Test + H2 内存数据库
- **前端测试**: Vitest + Vue Test Utils（按需补充）
- **覆盖率目标**: ≥ 80%
- **TDD**: 新功能先写测试，再实现

## Git 规范

- **提交格式**: `<type>: <description>`
- **类型**: feat, fix, refactor, docs, test, chore, perf, ci
- **原则**: 小而专注的提交，提交前本地测试

## UI 设计规范

### 设计风格

- **名称**: Academic Modern（学术现代风格）
- **特色**: 玻璃拟态效果、卡片式设计、微动效

### 配色方案

- **主色**: Deep Ocean Blue `#0056D2`
- **强调色**: Vibrant Coral `#FF6B6B`（悬赏积分、点赞）
- **背景色**: 浅灰蓝 `#F5F7FA`

### 组件样式

- 卡片圆角: `12px`
- 阴影: `0 4px 12px rgba(0, 0, 0, 0.05)`
- 悬停效果: 上浮 `4px` + 阴影加深
- 动效: `0.3s ease-out`

## 扩展开发

### 添加新页面

1. 在 `views/` 创建 `.vue` 组件
2. 在 `router/index.ts` 添加路由配置
3. 如需权限，在 meta 设置 `requiresAuth: true`
4. 在对应 `api/` 文件中添加接口

### 添加新 API

1. 在 `api/` 目录创建或扩展 `*.ts` 文件
2. 使用 `utils/request.ts` 中的封装方法
3. 定义 TypeScript 接口类型

### 添加新实体（后端）

1. 在 `entity/` 创建 `Xxx.java`
2. 在 `mapper/` 创建 `XxxMapper.java`
3. 在 `service/` 创建接口和实现
4. 在 `controller/` 创建 REST 控制器
5. 更新数据库建表脚本 `docs/schema.sql`

## 禁止事项

- 禁止在生产代码中使用 `console.log`（前端）
- 禁止使用 `any` 类型（前端）
- 禁止硬编码密钥、密码、Token
- 禁止不带 `@Transactional` 的多表事务操作
- 禁止空 `catch` 块
- 禁止提交 `node_modules`、`dist`、`target` 目录

## 重要文件

| 文件 | 用途 |
|------|------|
| `docs/schema.sql` | 数据库建表脚本 |
| `frontend/src/utils/request.ts` | Axios 封装（Token 拦截器） |
| `frontend/src/stores/user.ts` | 用户状态管理 |
| `backend/src/main/resources/application.yml` | 后端配置文件 |
| `frontend/.env.development` | 前端开发环境变量 |
| `frontend/.env.production` | 前端生产环境变量 |
