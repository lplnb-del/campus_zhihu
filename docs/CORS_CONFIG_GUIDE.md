# 生产环境CORS配置指南

> **重要提醒**：生产环境必须配置正确的CORS域名，否则前端无法正常访问后端API！

---

## 📋 当前状态

✅ 开发环境配置：允许所有来源（`*`）- 便于本地开发调试
⚠️ 生产环境配置：**包含占位符域名** - 需要更新为实际域名

---

## 🎯 配置步骤

### 步骤1：确定前端域名

确认以下信息：

| 项目 | 示例值 | 你的值 |
|------|--------|--------|
| **前端协议** | https | ____________ |
| **前端域名** | campuszhihu.com | ____________ |
| **前端子域名** | www / api / (无) | ____________ |
| **完整域名** | https://www.campuszhihu.com | ____________ |

**常见部署场景**：

| 场景 | 示例URL |
|------|---------|
| **主站** | `https://campuszhihu.com` |
| **带www** | `https://www.campuszhihu.com` |
| **多子域名** | `https://app.campuszhihu.com`, `https://admin.campuszhihu.com` |
| **静态托管** | `https://campuszhihu.web.app` (Firebase), `https://campuszhihu.vercel.app` (Vercel) |

### 步骤2：修改配置文件

**文件位置**：`backend/src/main/java/com/campus/zhihu/config/SecurityConfig.java`

**修改位置**：第137-174行的 `prodCorsConfigurationSource()` 方法

**修改前**（第142-146行）：
```java
configuration.setAllowedOrigins(Arrays.asList(
    "http://localhost:5173",  // 本地开发前端地址
    "http://localhost:3000",  // 本地开发前端地址
    "https://your-frontend-domain.com"  // ⚠️ 占位符域名
));
```

**修改后**（示例）：
```java
configuration.setAllowedOrigins(Arrays.asList(
    "http://localhost:5173",  // 保留本地开发地址（如需要）
    "http://localhost:3000",  // 保留本地开发地址（如需要）
    "https://www.campuszhihu.com",  // ✅ 实际生产域名
    "https://campuszhihu.com"       // ✅ 不带www的域名
));
```

### 步骤3：编译和部署

```bash
cd backend
mvn clean package -DskipTests
# 部署到生产环境
```

### 步骤4：验证配置

**验证方法1：浏览器开发者工具**
1. 打开生产网站
2. 按F12打开开发者工具
3. 切换到Console标签
4. 观察是否有CORS错误

**验证方法2：curl测试**
```bash
curl -I -H "Origin: https://www.campuszhihu.com" \
     -H "Access-Control-Request-Method: GET" \
     -H "Access-Control-Request-Headers: Authorization" \
     -X OPTIONS \
     https://api.campuszhihu.com/api/question/hot
```

期望看到响应头：
```
Access-Control-Allow-Origin: https://www.campuszhihu.com
Access-Control-Allow-Credentials: true
```

---

## ⚠️ 常见问题

### 问题1：忘记删除localhost地址

**症状**：生产环境仍允许localhost访问，存在安全隐患

**解决**：
- 如果不需要本地调试连接生产环境，删除`localhost`地址
- 如果需要，确保只在测试阶段保留，正式上线时移除

### 问题2：域名包含端口号

**症状**：前端使用非标准端口（如8080），无法访问

**解决**：
```java
// 明确指定端口号
configuration.setAllowedOrigins(Arrays.asList(
    "https://www.campuszhihu.com:8080"
));
```

### 问题3：前后端分离部署在同一域名不同路径

**示例**：
- 前端：`https://example.com/app`
- 后端：`https://example.com/api`

**解决**：
```java
// 同源部署不需要CORS配置，但需要确保前端正确配置API基础路径
// 前端配置：VITE_API_BASE_URL=/api
```

### 问题4：使用CDN或静态托管

**场景**：前端部署在Vercel/Netlify/Firebase

**解决**：
```java
configuration.setAllowedOrigins(Arrays.asList(
    "https://campuszhihu.vercel.app",      // Vercel
    "https://campuszhihu.web.app"          // Firebase
));
```

---

## 🔒 安全最佳实践

### DO（推荐）✅

1. **仅添加必要的域名**
   ```java
   // ✅ 只包含实际使用的域名
   configuration.setAllowedOrigins(Arrays.asList(
       "https://www.campuszhihu.com"
   ));
   ```

2. **使用具体域名而非通配符**
   ```java
   // ✅ 明确指定
   configuration.setAllowedOrigins(Arrays.asList("https://www.campuszhihu.com"));

   // ❌ 避免使用（除非有充分理由）
   configuration.setAllowedOriginPatterns(Arrays.asList("https://*.campuszhihu.com"));
   ```

3. **定期审查域名列表**
   - 移除不再使用的域名
   - 确保所有域名都是必要的

4. **使用环境变量管理（可选）**
   ```java
   @Value("${app.allowed-origins}")
   private String[] allowedOrigins;

   configuration.setAllowedOrigins(Arrays.asList(allowedOrigins));
   ```

### DON'T（避免）❌

1. **生产环境使用通配符**
   ```java
   // ❌ 严重安全风险！
   configuration.setAllowedOriginPatterns(Arrays.asList("*"));
   ```

2. **包含开发地址**
   ```java
   // ❌ 生产环境不应包含localhost
   configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
   ```

3. **包含HTTP协议（除非有特殊需求）**
   ```java
   // ❌ 避免HTTP，强制HTTPS
   configuration.setAllowedOrigins(Arrays.asList("http://example.com"));
   ```

---

## 📊 配置检查清单

部署前检查：

- [ ] 已将占位符域名 `https://your-frontend-domain.com` 替换为实际域名
- [ ] 域名使用HTTPS协议
- [ ] 已移除或注释掉不需要的localhost地址
- [ ] 域名拼写正确（确认无typo）
- [ ] 已测试前后端联调
- [ ] 已验证浏览器控制台无CORS错误

---

## 🛠️ 故障排查

### 症状1：浏览器显示CORS错误

**错误信息**：
```
Access to XMLHttpRequest at 'https://api.example.com/api/xxx' from origin
'https://www.example.com' has been blocked by CORS policy
```

**排查步骤**：
1. 确认后端已重新编译和部署
2. 确认`spring.profiles.active=prod`
3. 确认CORS配置中包含前端域名
4. 检查Nginx/反向代理配置（如有）

### 症状2：OPTIONS请求返回403

**原因**：预检请求被拦截

**解决**：确保SecurityConfig允许OPTIONS方法
```java
configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
```

### 症状3：Cookie无法携带

**原因**：未设置`setAllowCredentials(true)`

**解决**：确认第162行配置
```java
configuration.setAllowCredentials(true);  // ✅ 必须为true
```

**注意**：当`setAllowCredentials(true)`时，`setAllowedOrigins`不能使用通配符`*`

---

## 📚 相关资源

- [MDN - CORS](https://developer.mozilla.org/zh-CN/docs/Web/HTTP/CORS)
- [Spring Security CORS文档](https://docs.spring.io/spring-security/reference/servlet/integrations/cors.html)
- [项目安全修复报告](./COMPLETE_FIX_REPORT.md)
- [环境变量配置指南](./ENV_SETUP_GUIDE.md)

---

**更新日期**：2026-01-02
**维护人**：CampusZhihu Team
