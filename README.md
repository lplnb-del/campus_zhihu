# CampusZhihu - 校园知乎

> 一个集学术交流、生活互助、积分悬赏于一体的高校知识共享平台

## 📖 项目简介

CampusZhihu（校园知乎）是一个前后端分离的知识问答社区平台，专为高校学生设计。平台支持问题发布、回答、评论、点赞、收藏、悬赏积分等核心功能，采用现代化的技术栈和精美的 UI 设计。

### ✨ 核心特性

- 🎓 **学术交流**：提问、回答、采纳机制，促进知识分享
- 💰 **积分悬赏**：通过积分激励优质回答
- 🏷️ **话题标签**：多维度话题分类，快速定位内容
- 💬 **二级评论**：支持对回答进行深度讨论
- ⭐ **收藏点赞**：个性化内容管理
- 🔔 **消息通知**：实时推送互动消息
- 🎨 **精美 UI**：Academic Modern 设计风格，玻璃拟态效果

## 🛠️ 技术栈

### 后端技术栈

| 技术               | 版本      | 说明                     |
| ------------------ | --------- | ------------------------ |
| Spring Boot        | 3.2.0     | 核心框架                 |
| JDK                | 17+       | Java 开发工具包          |
| MyBatis-Plus       | 3.5.5     | ORM 框架                 |
| MySQL              | 8.0+      | 关系型数据库             |
| Druid              | 1.2.20    | 数据库连接池             |
| JWT                | 0.12.3    | Token 鉴权               |
| Spring Security    | 3.2.0     | 安全框架（密码加密）     |
| MinIO              | 8.5.7     | 对象存储（文件上传）     |
| Lombok             | -         | 代码简化工具             |
| Hutool             | 5.8.23    | Java 工具类库            |

### 前端技术栈

| 技术              | 版本     | 说明                  |
| ----------------- | -------- | --------------------- |
| Vue 3             | 3.4.0    | 渐进式 JavaScript 框架 |
| TypeScript        | 5.3.3    | JavaScript 超集       |
| Vite              | 5.0.8    | 前端构建工具          |
| Pinia             | 2.1.7    | 状态管理              |
| Vue Router        | 4.2.5    | 路由管理              |
| Element Plus      | 2.5.0    | UI 组件库（按需引入）  |
| Axios             | 1.6.2    | HTTP 请求库           |
| Sass              | 1.69.5   | CSS 预处理器          |
| NProgress         | 0.2.0    | 进度条                |
| Markdown-it       | 14.0.0   | Markdown 渲染         |

## 📁 项目结构

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
│   │   │   │   │   └── MyMetaObjectHandler.java # 字段自动填充
│   │   │   │   ├── controller/      # 控制器层
│   │   │   │   ├── service/         # 服务层
│   │   │   │   │   └── impl/        # 服务实现
│   │   │   │   ├── mapper/          # 数据访问层
│   │   │   │   ├── entity/          # 实体类
│   │   │   │   │   ├── SysUser.java             # 用户实体
│   │   │   │   │   ├── SysNotice.java           # 通知实体
│   │   │   │   │   ├── BizQuestion.java         # 问题实体
│   │   │   │   │   ├── BizAnswer.java           # 回答实体
│   │   │   │   │   ├── BizTag.java              # 标签实体
│   │   │   │   │   ├── BizComment.java          # 评论实体
│   │   │   │   │   └── BizCollection.java       # 收藏实体
│   │   │   │   ├── dto/             # 数据传输对象
│   │   │   │   ├── vo/              # 视图对象
│   │   │   │   ├── util/            # 工具类
│   │   │   │   └── CampusZhihuApplication.java  # 启动类
│   │   │   └── resources/
│   │   │       ├── mapper/          # MyBatis XML 映射文件
│   │   │       └── application.yml  # 应用配置文件
│   │   └── test/                    # 测试代码
│   └── pom.xml                      # Maven 配置文件
│
├── frontend/                         # 前端项目
│   ├── public/                      # 静态资源
│   ├── src/
│   │   ├── api/                     # API 接口
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
│   │   │   └── user.ts              # 用户状态
│   │   ├── utils/                   # 工具函数
│   │   │   └── request.ts           # Axios 封装
│   │   ├── App.vue                  # 根组件
│   │   └── main.ts                  # 入口文件
│   ├── .env.development             # 开发环境变量
│   ├── .env.production              # 生产环境变量
│   ├── index.html                   # HTML 模板
│   ├── package.json                 # 依赖配置
│   ├── tsconfig.json                # TypeScript 配置
│   └── vite.config.ts               # Vite 配置
│
├── docs/                            # 文档
│   └── schema.sql                   # 数据库建表脚本
│
└── README.md                        # 项目说明文档
```

## 🗄️ 数据库设计

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

## 🎨 UI/UX 设计规范

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

## 🚀 快速开始

### 环境要求

- **后端**：JDK 17+, Maven 3.6+, MySQL 8.0+
- **前端**：Node.js 18+, pnpm 8+

### 后端启动

```bash
# 1. 进入后端目录
cd backend

# 2. 创建数据库并导入 schema.sql
mysql -u root -p < ../docs/schema.sql

# 3. 修改配置文件
# 编辑 src/main/resources/application.yml
# 修改数据库连接信息（url, username, password）

# 4. 安装依赖并启动
mvn clean install
mvn spring-boot:run

# 启动成功后访问：http://localhost:8080/api
```

### 前端启动

```bash
# 1. 进入前端目录
cd frontend

# 2. 安装依赖
pnpm install

# 3. 启动开发服务器
pnpm dev

# 启动成功后访问：http://localhost:3000
```

## 📝 核心业务逻辑

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

## 📊 API 接口文档

### 用户模块

- `POST /user/register` - 用户注册
- `POST /user/login` - 用户登录
- `GET /user/info` - 获取用户信息
- `PUT /user/update` - 更新用户信息

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

## 🔐 安全机制

### JWT 认证

- Token 存储在 `localStorage`
- 请求头携带：`Authorization: Bearer <token>`
- Token 有效期：7 天

### 密码加密

- 使用 BCrypt 算法加密存储
- 盐值自动生成，防止彩虹表攻击

### 请求拦截

- 前端 Axios 拦截器自动添加 Token
- 后端 Filter 验证 Token 有效性
- Token 过期自动跳转登录页

## 🎯 开发规范

### 后端开发规范

1. **命名规范**：驼峰命名法，类名首字母大写
2. **注释规范**：所有 public 方法必须添加 JavaDoc 注释
3. **异常处理**：使用 `BusinessException` 抛出业务异常
4. **日志规范**：使用 Slf4j，合理使用 debug/info/warn/error 级别
5. **事务管理**：涉及多表操作必须添加 `@Transactional`

### 前端开发规范

1. **组件命名**：大驼峰，如 `QuestionCard.vue`
2. **文件命名**：小驼峰，如 `userApi.ts`
3. **TypeScript**：强制类型检查，避免使用 `any`
4. **代码格式化**：使用 Prettier 统一代码风格
5. **响应式**：使用 `<script setup>` 语法糖

## 📈 性能优化

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

## 🐛 常见问题

### 1. 后端启动失败

**问题**：数据库连接失败  
**解决**：检查 `application.yml` 中的数据库配置，确保 MySQL 已启动

### 2. 前端启动报错

**问题**：依赖安装失败  
**解决**：删除 `node_modules` 和 `pnpm-lock.yaml`，重新执行 `pnpm install`

### 3. 跨域问题

**问题**：API 请求被 CORS 拦截  
**解决**：后端已配置 CORS，前端使用 Vite 代理，无需额外配置

## 📄 License

MIT License

## 👥 贡献者

CampusZhihu Team

## 📧 联系方式

如有问题或建议，欢迎提 Issue 或 PR！

---

**Made with ❤️ by CampusZhihu Team**