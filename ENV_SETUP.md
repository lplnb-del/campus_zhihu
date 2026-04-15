# 环境变量配置指南

## 概述

为了保护敏感信息（数据库密码、JWT密钥等），项目已将所有敏感配置迁移到环境变量。本文档将指导你如何配置这些环境变量。

## 快速开始

### 1. 复制环境变量示例文件

```bash
cd backend
cp .env.example .env
```

### 2. 编辑 .env 文件

使用文本编辑器打开 `.env` 文件，填写实际的配置值。

### 3. 配置环境变量

根据你的操作系统和开发环境，选择以下方式之一：

#### 方式一：命令行设置（临时）

**Windows (PowerShell):**
```powershell
$env:DB_PASSWORD="your_password"
$env:JWT_SECRET="your_jwt_secret"
$env:MINIO_SECRET_KEY="your_minio_secret"
$env:DRUID_PASSWORD="your_druid_password"
```

**Windows (CMD):**
```cmd
set DB_PASSWORD=your_password
set JWT_SECRET=your_jwt_secret
set MINIO_SECRET_KEY=your_minio_secret
set DRUID_PASSWORD=your_druid_password
```

**Linux/Mac:**
```bash
export DB_PASSWORD="your_password"
export JWT_SECRET="your_jwt_secret"
export MINIO_SECRET_KEY="your_minio_secret"
export DRUID_PASSWORD="your_druid_password"
```

#### 方式二：IDE 配置（推荐）

**IntelliJ IDEA:**
1. 打开 Run/Debug Configurations
2. 选择你的 Spring Boot 运行配置
3. 点击 "Environment variables" 字段旁边的文件夹图标
4. 添加以下环境变量：
   - `DB_PASSWORD=your_password`
   - `JWT_SECRET=your_jwt_secret`
   - `MINIO_SECRET_KEY=your_minio_secret`
   - `DRUID_PASSWORD=your_druid_password`

**VS Code:**
在 `launch.json` 中添加：
```json
{
  "env": {
    "DB_PASSWORD": "your_password",
    "JWT_SECRET": "your_jwt_secret",
    "MINIO_SECRET_KEY": "your_minio_secret",
    "DRUID_PASSWORD": "your_druid_password"
  }
}
```

#### 方式三：使用 .env 文件（开发环境）

如果你使用的是支持 .env 文件的 IDE 或工具（如 Spring Boot DevTools），可以直接使用 `.env` 文件。

## 必需的环境变量

| 变量名 | 说明 | 示例值 |
|--------|------|--------|
| `DB_PASSWORD` | 数据库密码 | `your_secure_password` |
| `JWT_SECRET` | JWT 签名密钥 | `your_256_bit_random_secret_key` |
| `MINIO_SECRET_KEY` | MinIO 密钥 | `your_minio_secret_key` |
| `DRUID_PASSWORD` | Druid 监控密码 | `your_druid_password` |

## 可选的环境变量

| 变量名 | 说明 | 示例值 |
|--------|------|--------|
| `DB_USERNAME` | 数据库用户名 | `root` |
| `DRUID_USERNAME` | Druid 监控用户名 | `admin` |
| `MINIO_ACCESS_KEY` | MinIO 访问密钥 | `minioadmin` |
| `REDIS_HOST` | Redis 主机地址 | `localhost` |
| `REDIS_PORT` | Redis 端口 | `6379` |
| `REDIS_PASSWORD` | Redis 密钥 | `your_redis_password` |

## 生成安全的密钥

### JWT 密钥

使用 OpenSSL 生成 256 位的随机密钥：

**Windows (Git Bash):**
```bash
openssl rand -base64 32
```

**Linux/Mac:**
```bash
openssl rand -base64 32
```

**在线生成器：**
访问 https://www.grc.com/passwords.htm 生成强密码

### 数据库密码

建议使用以下规则：
- 至少 12 个字符
- 包含大小写字母、数字和特殊字符
- 避免使用常见单词或个人信息

示例：`MySecureP@ssw0rd2024!`

## 生产环境部署

### 使用 Docker

在 `docker-compose.yml` 中配置：

```yaml
services:
  backend:
    environment:
      - DB_PASSWORD=${DB_PASSWORD}
      - JWT_SECRET=${JWT_SECRET}
      - MINIO_SECRET_KEY=${MINIO_SECRET_KEY}
      - DRUID_PASSWORD=${DRUID_PASSWORD}
```

### 使用 Kubernetes

在 `deployment.yaml` 中使用 Secret：

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: campus-zhihu-secrets
type: Opaque
stringData:
  DB_PASSWORD: your_password
  JWT_SECRET: your_jwt_secret
  MINIO_SECRET_KEY: your_minio_secret_key
  DRUID_PASSWORD: your_druid_password
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: campus-zhihu
spec:
  template:
    spec:
      containers:
      - name: backend
        env:
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: campus-zhihu-secrets
              key: DB_PASSWORD
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: campus-zhihu-secrets
              key: JWT_SECRET
```

### 使用云服务

大多数云服务提供商（AWS、Azure、阿里云等）都提供环境变量或密钥管理服务，请参考相应文档。

## 验证配置

启动应用后，检查日志是否正常加载配置：

```bash
# 查看日志
tail -f logs/campus-zhihu.log

# 或在控制台查看
```

如果配置正确，应用应该能正常启动。如果缺少必需的环境变量，应用会使用默认值或启动失败。

## 常见问题

### Q1: 应用启动失败，提示缺少环境变量？

**A:** 确保所有必需的环境变量都已设置。检查环境变量名称是否正确（区分大小写）。

### Q2: 如何在团队中共享环境变量配置？

**A:** 不要将 `.env` 文件提交到版本控制系统。团队成员应该：
1. 复制 `.env.example` 为 `.env`
2. 填写自己的配置值
3. 或者使用团队共享的密钥管理服务

### Q3: 生产环境如何管理环境变量？

**A:** 生产环境应该使用专业的密钥管理服务：
- AWS Secrets Manager
- Azure Key Vault
- HashiCorp Vault
- 或者使用云服务商的环境变量配置功能

### Q4: 如何测试环境变量是否生效？

**A:** 在应用中添加测试代码：

```java
@Value("${DB_PASSWORD}")
private String dbPassword;

@GetMapping("/test-env")
public String testEnv() {
    return "DB Password: " + (dbPassword != null ? "***" : "NULL");
}
```

## 安全建议

1. **永远不要**将 `.env` 文件提交到版本控制系统
2. 定期更换敏感信息（密码、密钥）
3. 使用强密码和随机密钥
4. 限制对环境变量的访问权限
5. 生产环境使用专业的密钥管理服务
6. 定期审计环境变量的使用情况

## 联系支持

如果遇到问题，请联系开发团队或提交 Issue。

---

**最后更新：** 2026-01-03
**版本：** 1.0.0