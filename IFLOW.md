# CampusZhihu 项目上下文文档

## 项目概述

**CampusZhihu（校园知乎）** 是一个前后端分离的知识问答社区平台，专为高校学生设计。平台集学术交流、生活互助、积分悬赏于一体，采用现代化的技术栈和精美的 UI 设计。

### 核心特性

- 🎓 **学术交流**：提问、回答、采纳机制，促进知识分享
- 💰 **积分悬赏**：通过积分激励优质回答
- 🏷️ **话题标签**：多维度话题分类，快速定位内容
- 💬 **二级评论**：支持对回答进行深度讨论
- ⭐ **收藏点赞**：个性化内容管理
- 🔔 **消息通知**：实时推送互动消息
- 🎨 **精美 UI**：Academic Modern 设计风格，玻璃拟态效果

---

## 技术栈

### 后端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.2.0 | 核心框架 |
| JDK | 17+ | Java 开发工具包 |
| MyBatis-Plus | 3.5.5 | ORM 框架 |
| MySQL | 8.0+ | 关系型数据库 |
| Druid | 1.2.20 | 数据库连接池 |
| JWT | 0.12.3 | Token 鉴权 |
| Spring Security | 3.2.0 | 安全框架（密码加密） |
| MinIO | 8.5.7 | 对象存储（文件上传） |
| Lombok | - | 代码简化工具 |
| Hutool | 5.8.23 | Java 工具类库 |

### 前端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue 3 | 3.4.0 | 渐进式 JavaScript 框架 |
| TypeScript | 5.3.3 | JavaScript 超集 |
| Vite | 5.0.8 | 前端构建工具 |
| Pinia | 2.1.7 | 状态管理 |
| Vue Router | 4.2.5 | 路由管理 |
| Element Plus | 2.5.0 | UI 组件库（按需引入） |
| Axios | 1.6.2 | HTTP 请求库 |
| Sass | 1.69.5 | CSS 预处理器 |
| NProgress | 0.2.0 | 进度条 |
| Markdown-it | 14.0.0 | Markdown 渲染 |

---

## 项目结构

```
campus_zhihu/
├── backend/                          # 后端项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/campus/zhihu/
│   │   │   │   ├── common/          # 通用组件
│   │   │   │   │   ├── Result.java              # 统一响应结果
│   │   │   │   │   ├── ResultCode.java          # 状态码枚举
│   │   │   │   │   ├── BusinessException.java   # 业务异常
│   │   │   │   │   └── GlobalExceptionHandler.java # 全局异常处理
│   │   │   │   ├── config/          # 配置类
│   │   │   │   │   ├── MybatisPlusConfig.java   # MyBatis-Plus 配置
│   │   │   │   │   ├── MyMetaObjectHandler.java # 字段自动填充
│   │   │   │   │   └── SecurityConfig.java     # Spring Security 配置
│   │   │   │   ├── controller/      # 控制器层
│   │   │   │   ├── service/         # 服务层
│   │   │   │   │   └── impl/        # 服务实现
│   │   │   │   ├── mapper/          # 数据访问层
│   │   │   │   ├── entity/          # 实体类
│   │   │   │   ├── dto/             # 数据传输对象
│   │   │   │   ├── vo/              # 视图对象
│   │   │   │   ├── util/            # 工具类
│   │   │   │   └── CampusZhihuApplication.java  # 启动类
│   │   │   └── resources/
│   │   │       ├── mapper/          # MyBatis XML 映射文件
│   │   │       └── application.yml  # 应用配置文件
│   │   └── test/                    # 测试代码
│   ├── logs/                         # 日志文件
│   ├── pom.xml                      # Maven 配置文件
│   └── target/                      # 编译输出
│
├── frontend/                         # 前端项目
│   ├── public/                      # 静态资源
│   ├── src/
│   │   ├── api/                     # API 接口
│   │   │   ├── user.ts              # 用户 API
│   │   │   ├── question.ts          # 问题 API
│   │   │   ├── answer.ts            # 回答 API
│   │   │   ├── interaction.ts       # 交互 API（点赞、收藏、评论）
│   │   │   └── tag.ts               # 标签 API
│   │   ├── assets/                  # 资源文件
│   │   │   └── styles/              # 样式文件
│   │   │       ├── element-vars.scss    # Element Plus 主题变量
│   │   │       └── global.scss          # 全局样式
│   │   ├── components/              # 公共组件
│   │   ├── views/                   # 页面视图
│   │   │   ├── Home.vue                 # 首页
│   │   │   ├── Questions.vue            # 问题广场
│   │   │   ├── QuestionDetail.vue       # 问题详情
│   │   │   ├── AskQuestion.vue          # 提问页面
│   │   │   ├── Tags.vue                 # 话题标签
│   │   │   ├── auth/                    # 认证页面
│   │   │   │   ├── Login.vue            # 登录
│   │   │   │   └── Register.vue         # 注册
│   │   │   └── user/                    # 用户中心
│   │   │       ├── Profile.vue          # 个人资料
│   │   │       ├── MyQuestions.vue      # 我的提问
│   │   │       ├── MyAnswers.vue        # 我的回答
│   │   │       ├── MyCollections.vue    # 我的收藏
│   │   │       ├── MyPoints.vue         # 我的积分
│   │   │       └── MyNotices.vue        # 消息通知
│   │   ├── router/                  # 路由配置
│   │   │   └── index.ts             # 路由主文件
│   │   ├── stores/                  # 状态管理
│   │   │   ├── app.ts               # 应用状态
│   │   │   └── user.ts              # 用户状态
│   │   ├── utils/                   # 工具函数
│   │   │   └── request.ts           # Axios 封装
│   │   ├── App.vue                  # 根组件
│   │   └── main.ts                  # 入口文件
│   ├── .env.development             # 开发环境变量
│   ├── .env.production              # 生产环境变量
│   ├── package.json                 # 依赖配置
│   ├── tsconfig.json                # TypeScript 配置
│   └── vite.config.ts               # Vite 配置
│
├── docs/                            # 文档
│   ├── schema.sql                   # 数据库建表脚本
│   ├── reset_database.sql           # 数据库重置脚本
│   ├── init_db.bat                  # Windows 初始化脚本
│   └── init_db.sh                   # Linux/Mac 初始化脚本
│
└── README.md                        # 项目说明文档
```

---

## 构建和运行

### 环境要求

- **后端**：JDK 17+, Maven 3.6+, MySQL 8.0+
- **前端**：Node.js 18+, pnpm 8+

### 数据库初始化

```bash
# Windows
cd docs
init_db.bat

# Linux/Mac
cd docs
chmod +x init_db.sh
./init_db.sh

# 或手动执行
mysql -u root -p < docs/schema.sql
```

### 后端启动

```bash
# 1. 进入后端目录
cd backend

# 2. 修改配置文件（如果需要）
# 编辑 src/main/resources/application.yml
# 修改数据库连接信息（url, username, password）

# 3. 清理并安装依赖
mvn clean install

# 4. 启动应用
mvn spring-boot:run

# 5. 访问后端 API
# 后端地址：http://localhost:8080/api
# Druid 监控：http://localhost:8080/api/druid
```

### 前端启动

```bash
# 1. 进入前端目录
cd frontend

# 2. 安装依赖
pnpm install

# 3. 启动开发服务器
pnpm dev

# 4. 访问前端应用
# 前端地址：http://localhost:3000
```

### 测试

```bash
# 后端测试
cd backend
mvn test

# 前端测试（如有）
cd frontend
pnpm test

# 前端代码检查
cd frontend
pnpm lint

# 前端代码格式化
cd frontend
pnpm format
```

### 生产构建

```bash
# 后端打包
cd backend
mvn clean package -DskipTests
# 生成的 JAR 文件：target/zhihu-1.0.0.jar

# 前端打包
cd frontend
pnpm build
# 生成的静态文件：dist/
```

---

## 开发规范

### 后端开发规范

1. **命名规范**
   - 类名：大驼峰（PascalCase），如 `UserService`
   - 方法名：小驼峰（camelCase），如 `getUserById`
   - 常量：全大写下划线分隔，如 `DEFAULT_PAGE_SIZE`

2. **注释规范**
   - 所有 public 方法必须添加 JavaDoc 注释
   - 复杂逻辑必须添加行内注释说明

3. **异常处理**
   - 使用 `BusinessException` 抛出业务异常
   - `GlobalExceptionHandler` 统一处理所有异常

4. **日志规范**
   - 使用 Slf4j 日志框架
   - 合理使用 debug/info/warn/error 级别
   - 敏感信息（密码、Token）禁止记录日志

5. **事务管理**
   - 涉及多表操作必须添加 `@Transactional` 注解
   - 积分相关操作使用乐观锁防止并发问题

6. **API 响应格式**
   - 统一使用 `Result<T>` 封装响应
   - 成功：`code = 200`
   - 失败：`code = 1000-9999`

### 前端开发规范

1. **组件命名**
   - 组件文件：大驼峰（PascalCase），如 `QuestionCard.vue`
   - 工具文件：小驼峰（camelCase），如 `userApi.ts`

2. **TypeScript 规范**
   - 强制类型检查，避免使用 `any`
   - 使用 interface 定义数据结构
   - 使用 enum 定义常量

3. **代码风格**
   - 使用 Prettier 统一代码格式
   - 使用 ESLint 进行代码检查
   - 遵循 Vue 3 Composition API 最佳实践

4. **API 调用**
   - 使用 `src/utils/request.ts` 封装的 Axios 实例
   - 所有 API 调用定义在 `src/api/` 目录下
   - 统一错误处理和 loading 状态

5. **路由管理**
   - 使用 Vue Router 进行路由管理
   - 需要登录的页面设置 `meta.requiresAuth = true`
   - 使用路由守卫进行权限控制

---

## 核心业务逻辑

### 1. 积分事务机制

回答被采纳时，使用 `@Transactional` 保证原子性：
- 扣除提问者积分（悬赏分）
- 增加回答者积分（悬赏分 + 额外奖励）
- 使用乐观锁（version 字段）防止并发扣减导致负数

### 2. 统一响应格式

所有 API 返回标准 JSON 格式：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {...},
  "timestamp": 1234567890
}
```

### 3. 全局异常处理

`GlobalExceptionHandler` 拦截所有异常，确保前端永远不会看到 500 错误页面，而是收到标准的 JSON 错误响应。

### 4. JWT 认证流程

1. 用户登录成功后，后端生成 JWT Token
2. Token 存储在前端 `localStorage`
3. 请求时在 Header 中携带：`Authorization: Bearer <token>`
4. 后端 Filter 验证 Token 有效性
5. Token 过期自动跳转登录页

### 5. 密码加密

- 使用 BCrypt 算法加密存储
- 盐值自动生成，防止彩虹表攻击

---

## 数据库设计

### 核心数据表

1. **sys_user** - 用户表：用户基本信息、积分、学号等
2. **sys_notice** - 消息通知表：点赞、回答、采纳等通知
3. **biz_tag** - 话题标签表：话题分类管理
4. **biz_question** - 问题表：问题标题、内容、悬赏分等
5. **biz_question_tag** - 问题-标签关联表：多对多关系
6. **biz_answer** - 回答表：回答内容、点赞数、采纳状态
7. **biz_comment** - 二级评论表：针对回答的评论
8. **biz_collection** - 收藏表：用户收藏的问题
9. **biz_like_record** - 点赞记录表：防止重复点赞

详细建表语句请查看：`docs/schema.sql`

### 数据库配置

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/campus_zhihu?useUnicode=true&characterEncoding=utf8
    username: root
    password: LPL20041212nb
```

---

## UI/UX 设计规范

### 设计风格：Academic Modern

融合学术严谨与现代 Web 设计美学

### 核心配色

- **主色**：Deep Ocean Blue `#0056D2` - 代表知识与信任
- **提亮色**：Vibrant Coral `#FF6B6B` - 用于悬赏积分、点赞等活跃交互
- **背景**：浅灰蓝 `#F5F7FA` - 替代纯白，增加层次感

### UI 特性

#### 1. 卡片式设计 (Card UI)
- 圆角：`12px`
- 阴影：`0 4px 12px rgba(0, 0, 0, 0.05)`
- 悬停效果：上浮 `4px` + 加深阴影

#### 2. 玻璃拟态 (Glassmorphism)
- 顶栏和侧边栏采用半透明背景
- 毛玻璃模糊效果：`backdrop-filter: blur(10px)`
- 营造通透、轻盈的视觉效果

#### 3. 微动效 (Micro-interactions)
- **Hover**：卡片上浮、颜色过渡（0.3s ease-out）
- **Click**：波纹效果 / 轻微缩放
- **Loading**：骨架屏（Skeleton）+ 流光动画，拒绝转圈圈
- **页面切换**：Fade-Slide-Up 动效

---

## API 接口文档

### 基础路径

- 后端 API 基础路径：`http://localhost:8080/api`
- 前端代理配置：`/api` → `http://localhost:8080/api`

### 用户模块

- `POST /user/register` - 用户注册
- `POST /user/login` - 用户登录
- `GET /user/info` - 获取用户信息
- `PUT /user/update` - 更新用户信息
- `GET /user/check/username/{username}` - 检查用户名是否存在

### 问题模块

- `POST /question/add` - 发布问题
- `GET /question/list` - 问题列表（分页）
- `GET /question/{id}` - 问题详情
- `PUT /question/update` - 更新问题
- `DELETE /question/{id}` - 删除问题

### 回答模块

- `POST /answer/add` - 添加回答
- `GET /answer/list` - 回答列表
- `PUT /answer/accept/{id}` - 采纳回答
- `POST /answer/like/{id}` - 点赞回答
- `DELETE /answer/{id}` - 删除回答

### 标签模块

- `GET /tag/list` - 标签列表
- `GET /tag/hot` - 热门标签
- `POST /tag/add` - 添加标签

### 收藏模块

- `POST /collection/add` - 收藏问题
- `DELETE /collection/remove` - 取消收藏
- `GET /collection/list` - 我的收藏

### 通知模块

- `GET /notice/list` - 通知列表
- `PUT /notice/read/{id}` - 标记已读
- `GET /notice/unread-count` - 未读数量

---

## 常见问题

### 1. 后端启动失败

**问题**：数据库连接失败
**解决**：
- 检查 MySQL 是否启动
- 检查 `application.yml` 中的数据库配置
- 确认数据库 `campus_zhihu` 是否已创建

### 2. 前端启动报错

**问题**：依赖安装失败
**解决**：
```bash
cd frontend
rm -rf node_modules pnpm-lock.yaml
pnpm install
```

### 3. 跨域问题

**问题**：API 请求被 CORS 拦截
**解决**：
- 后端已配置 CORS（见 `SecurityConfig.java`）
- 前端使用 Vite 代理（见 `vite.config.ts`）
- 确保后端服务已启动

### 4. Token 过期

**问题**：登录后频繁提示登录过期
**解决**：
- 检查 JWT 配置中的 `expiration` 时间
- 确认系统时间是否正确
- 检查 Token 是否正确存储在 `localStorage`

---

## 重要配置文件

### 后端配置

- `backend/src/main/resources/application.yml` - 主配置文件
- `backend/pom.xml` - Maven 依赖配置
- `backend/src/main/java/com/campus/zhihu/config/SecurityConfig.java` - 安全配置

### 前端配置

- `frontend/vite.config.ts` - Vite 构建配置
- `frontend/tsconfig.json` - TypeScript 配置
- `frontend/package.json` - NPM 依赖配置
- `frontend/.env.development` - 开发环境变量
- `frontend/.env.production` - 生产环境变量

---

## 测试

### 后端测试

- 单元测试：`backend/src/test/java/com/campus/zhihu/`
- 集成测试：使用 H2 内存数据库
- 测试数据：`backend/src/test/resources/sql/`

### 测试命令

```bash
# 运行所有测试
mvn test

# 运行指定测试类
mvn test -Dtest=UserServiceTest

# 跳过测试打包
mvn package -DskipTests
```

---

## 性能优化

### 后端优化

- MyBatis-Plus 分页插件，避免全表查询
- Druid 数据库连接池，优化连接管理
- 索引优化，为常用查询字段添加索引
- 懒加载，按需加载关联数据

### 前端优化

- Element Plus 按需引入，减少打包体积
- 路由懒加载，首屏加载更快
- 图片懒加载，优化网络请求
- 骨架屏代替 Loading，提升用户体验

---

## 安全机制

### JWT 认证

- Token 存储在 `localStorage`
- 请求头携带：`Authorization: Bearer <token>`
- Token 有效期：7 天（可配置）

### 密码加密

- 使用 BCrypt 算法加密存储
- 盐值自动生成，防止彩虹表攻击

### 请求拦截

- 前端 Axios 拦截器自动添加 Token
- 后端 Filter 验证 Token 有效性
- Token 过期自动跳转登录页

---

## 日志管理

### 日志配置

- 日志文件：`backend/logs/campus-zhihu.log`
- 日志级别：INFO（生产）、DEBUG（开发）
- 日志滚动：单文件最大 10MB，保留 30 天

### 查看日志

```bash
# 查看实时日志
tail -f backend/logs/campus-zhihu.log

# Windows 查看日志
Get-Content backend\logs\campus-zhihu.log -Wait
```

---

## 部署说明

### 后端部署

```bash
# 1. 打包
cd backend
mvn clean package -DskipTests

# 2. 运行
java -jar target/zhihu-1.0.0.jar

# 3. 或使用 nohup 后台运行
nohup java -jar target/zhihu-1.0.0.jar > app.log 2>&1 &
```

### 前端部署

```bash
# 1. 打包
cd frontend
pnpm build

# 2. 将 dist/ 目录部署到 Web 服务器（Nginx、Apache 等）
```

---

## 开发工具推荐

- **IDE**：IntelliJ IDEA（后端）、VSCode（前端）
- **数据库工具**：Navicat、DBeaver
- **API 测试**：Postman、Apifox
- **Git 客户端**：SourceTree、GitKraken

---

## 贡献指南

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

---

## 许可证

MIT License

---

## 联系方式

如有问题或建议，欢迎提 Issue 或 PR！

---

**Made with ❤️ by CampusZhihu Team**