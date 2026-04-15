# CampusZhihu 环境变量配置指南

## 概述

为提高项目安全性，所有敏感配置信息（密码、密钥等）已从配置文件中移除，改为通过**环境变量**注入。

**生产环境配置文件**: `backend/src/main/resources/application-prod.yml`

---

## 必需的环境变量

| 环境变量 | 说明 | 示例值 | 必需 |
|---------|------|--------|------|
| `DB_PASSWORD` | MySQL 数据库密码 | `your_secure_password` | ✅ 是 |
| `JWT_SECRET` | JWT 密钥（至少32位） | `random_secret_key_at_least_32_chars` | ✅ 是 |
| `DRUID_PASSWORD` | Druid 监控台密码 | `your_druid_password` | ✅ 是 |
| `MINIO_ACCESS_KEY` | MinIO 访问密钥 | `your_minio_access_key` | ✅ 是 |
| `MINIO_SECRET_KEY` | MinIO 秘密密钥 | `your_minio_secret_key` | ✅ 是 |

## 可选的环境变量

| 环境变量 | 说明 | 默认值 |
|---------|------|--------|
| `DRUID_USERNAME` | Druid 监控台用户名 | `admin` |
| `MINIO_ENDPOINT` | MinIO 服务地址 | `http://localhost:9000` |
| `SERVER_PORT` | 服务器端口 | `8080` |

---

## 环境变量设置方法

### Windows PowerShell

**临时设置**（仅当前会话有效）:
```powershell
# 数据库配置
$env:DB_PASSWORD = "your_secure_password"

# JWT 配置（至少32位随机字符串）
$env:JWT_SECRET = "CampusZhihuJWTSecretKey2024SecureRandomStringForTokenGeneration"

# Druid 监控配置
$env:DRUID_USERNAME = "admin"
$env:DRUID_PASSWORD = "your_druid_password"

# MinIO 配置
$env:MINIO_ACCESS_KEY = "your_minio_access_key"
$env:MINIO_SECRET_KEY = "your_minio_secret_key"
$env:MINIO_ENDPOINT = "http://localhost:9000"

# 服务器配置
$env:SERVER_PORT = "8080"

# 启动应用
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

**永久设置**（系统级别）:
```powershell
# 以管理员身份运行 PowerShell
[System.Environment]::SetEnvironmentVariable("DB_PASSWORD", "your_secure_password", "User")
[System.Environment]::SetEnvironmentVariable("JWT_SECRET", "your_jwt_secret_key_at_least_32_characters", "User")
```

---

### Windows CMD

**临时设置**（仅当前会话有效）:
```cmd
REM 数据库配置
set DB_PASSWORD=your_secure_password

REM JWT 配置
set JWT_SECRET=your_jwt_secret_key_at_least_32_characters

REM Druid 监控配置
set DRUID_USERNAME=admin
set DRUID_PASSWORD=your_druid_password

REM MinIO 配置
set MINIO_ACCESS_KEY=your_minio_access_key
set MINIO_SECRET_KEY=your_minio_secret_key

REM 启动应用
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

**永久设置**（系统级别）:
```cmd
setx DB_PASSWORD "your_secure_password"
setx JWT_SECRET "your_jwt_secret_key_at_least_32_characters"
```

---

### Linux / macOS

**临时设置**（仅当前会话有效）:
```bash
# 数据库配置
export DB_PASSWORD="your_secure_password"

# JWT 配置
export JWT_SECRET="your_jwt_secret_key_at_least_32_characters"

# Druid 监控配置
export DRUID_USERNAME="admin"
export DRUID_PASSWORD="your_druid_password"

# MinIO 配置
export MINIO_ACCESS_KEY="your_minio_access_key"
export MINIO_SECRET_KEY="your_minio_secret_key"
export MINIO_ENDPOINT="http://localhost:9000"

# 服务器配置
export SERVER_PORT="8080"

# 启动应用
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

**永久设置**（添加到 ~/.bashrc 或 ~/.zshrc）:
```bash
echo 'export DB_PASSWORD="your_secure_password"' >> ~/.bashrc
echo 'export JWT_SECRET="your_jwt_secret_key_at_least_32_characters"' >> ~/.bashrc
echo 'export DRUID_PASSWORD="your_druid_password"' >> ~/.bashrc
echo 'export MINIO_ACCESS_KEY="your_minio_access_key"' >> ~/.bashrc
echo 'export MINIO_SECRET_KEY="your_minio_secret_key"' >> ~/.bashrc
source ~/.bashrc
```

---

### 使用 .env 文件（推荐用于开发环境）

创建 `backend/.env` 文件：
```properties
# 数据库配置
DB_PASSWORD=your_secure_password

# JWT 配置
JWT_SECRET=your_jwt_secret_key_at_least_32_characters

# Druid 监控配置
DRUID_USERNAME=admin
DRUID_PASSWORD=your_druid_password

# MinIO 配置
MINIO_ACCESS_KEY=your_minio_access_key
MINIO_SECRET_KEY=your_minio_secret_key
MINIO_ENDPOINT=http://localhost:9000

# 服务器配置
SERVER_PORT=8080
```

**注意**：
- `.env` 文件应该添加到 `.gitignore`，不要提交到版本控制系统
- 需要安装 `dotenv-maven-plugin` 插件才能自动加载 `.env` 文件

---

## 生成安全的 JWT 密钥

**方法1：使用 OpenSSL（推荐）**
```bash
# Linux / macOS
openssl rand -base64 32

# Windows (Git Bash)
openssl rand -base64 32
```

**方法2：使用在线工具**
访问：https://www.random.org/strings/

**要求**：
- 至少 32 位字符
- 包含大小写字母、数字、特殊字符
- 不要使用常见单词或可预测的模式

---

## 验证环境变量

### Windows
```powershell
# PowerShell
echo $env:DB_PASSWORD
echo $env:JWT_SECRET
```

```cmd
# CMD
echo %DB_PASSWORD%
echo %JWT_SECRET%
```

### Linux / macOS
```bash
echo $DB_PASSWORD
echo $JWT_SECRET
```

---

## 启动应用时指定 Profile

**使用 Maven**:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

**使用 JAR 包**:
```bash
java -jar target/campus-zhihu-backend-1.0.0.jar --spring.profiles.active=prod
```

**在 application.yml 中设置**:
```yaml
spring:
  profiles:
    active: prod
```

---

## 安全建议

1. **不要将环境变量提交到 Git**
   - `.env` 文件必须添加到 `.gitignore`
   - 生产环境使用系统环境变量或密钥管理服务

2. **定期轮换密钥**
   - JWT_SECRET 每 3-6 个月更换一次
   - 数据库密码每 3-6 个月更换一次

3. **使用强密码**
   - 最少 12 位字符
   - 包含大小写字母、数字、特殊字符
   - 不要使用字典单词

4. **生产环境密钥管理**
   - 使用 AWS Secrets Manager / Azure Key Vault / HashiCorp Vault
   - 或使用 Docker Secrets / Kubernetes Secrets

---

## 故障排查

### 问题1：启动时报错 "Could not resolve placeholder 'DB_PASSWORD'"

**原因**：环境变量未设置

**解决**：
1. 检查环境变量是否正确设置
2. 重启终端或 IDE
3. 使用 `echo $DB_PASSWORD` 验证

### 问题2：JWT 验证失败

**原因**：JWT_SECRET 不匹配

**解决**：
1. 确保所有服务使用相同的 JWT_SECRET
2. 清除浏览器缓存中的旧 Token
3. 重新登录获取新 Token

### 问题3：MinIO 连接失败

**原因**：MinIO 密钥错误或服务未启动

**解决**：
1. 检查 MinIO 服务是否运行：`curl http://localhost:9000`
2. 验证 MINIO_ACCESS_KEY 和 MINIO_SECRET_KEY 是否正确
3. 确认 bucket-name 是否存在

---

**文档更新时间**: 2026-01-02
