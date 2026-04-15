package com.campus.zhihu.integration;

import com.campus.zhihu.CampusZhihuApplication;
import com.campus.zhihu.common.Result;
import com.campus.zhihu.dto.LoginDTO;
import com.campus.zhihu.dto.RegisterDTO;
import com.campus.zhihu.dto.UserUpdateDTO;
import com.campus.zhihu.entity.SysUser;
import com.campus.zhihu.mapper.UserMapper;
import com.campus.zhihu.util.JwtUtil;
import com.campus.zhihu.vo.LoginVO;
import com.campus.zhihu.vo.UserVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 用户模块集成测试
 * 测试用户注册、登录、信息更新等完整业务流程
 *
 * @author CampusZhihu Team
 */
@SpringBootTest(classes = CampusZhihuApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("用户模块集成测试")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_MAJOR = "计算机科学与技术";
    private static final String TEST_GRADE = "2024级";

    private static Long testUserId;
    private static String testToken;

    private static final Random random = new Random();

    // 生成随机用户名（3-20个字符）
    private String generateRandomUsername() {
        // 使用UUID的前8位作为随机字符串，确保唯一性
        String uuidPart = UUID.randomUUID().toString().substring(0, 8);
        return "u" + uuidPart;
    }

    // 生成随机邮箱
    private String generateRandomEmail() {
        String uuidPart = UUID.randomUUID().toString().substring(0, 8);
        return "u" + uuidPart + "@example.com";
    }

    // 生成随机学号
    private String generateRandomStudentId() {
        return "2024" + String.format("%04d", random.nextInt(10000));
    }

    // 清理测试数据
    private void cleanTestData(String username, String email, String studentId) {
        if (username != null) {
            userMapper.deleteByUsername(username);
        }
        if (email != null) {
            userMapper.deleteByEmail(email);
        }
        if (studentId != null) {
            userMapper.deleteByStudentId(studentId);
        }
    }

    @BeforeEach
    void setUp() {
        // 每个测试前清空测试数据
        cleanTestData("testuser", "test@example.com", "20240001");
    }

    @Nested
    @DisplayName("用户注册流程测试")
    class UserRegistrationTest {

        private String testUsername;
        private String testEmail;
        private String testStudentId;

        @BeforeEach
        void setUp() {
            // 生成随机测试数据
            testUsername = generateRandomUsername();
            testEmail = generateRandomEmail();
            testStudentId = generateRandomStudentId();
            // 清理可能存在的旧数据
            cleanTestData(testUsername, testEmail, testStudentId);
        }

        @Test
        @Order(1)
        @DisplayName("用户注册 - 成功")
        void testUserRegisterSuccess() throws Exception {
            RegisterDTO registerDTO = new RegisterDTO();
            registerDTO.setUsername(testUsername);
            registerDTO.setEmail(testEmail);
            registerDTO.setPassword(TEST_PASSWORD);
            registerDTO.setStudentId(testStudentId);
            registerDTO.setMajor(TEST_MAJOR);
            registerDTO.setGrade(TEST_GRADE);

            MvcResult result = mockMvc.perform(post("/user/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value("注册成功"))
                    .andExpect(jsonPath("$.data.username").value(testUsername))
                    .andExpect(jsonPath("$.data.points").value(100)) // 注册赠送100积分
                    .andReturn();

            // 解析响应获取用户ID
            String response = result.getResponse().getContentAsString();
            Result<UserVO> resultObj = objectMapper.readValue(response,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, UserVO.class));
            testUserId = resultObj.getData().getId();

            // 验证数据库中的数据
            SysUser user = userMapper.selectById(testUserId);
            assertNotNull(user);
            assertEquals(testUsername, user.getUsername());
            assertEquals(testEmail, user.getEmail());
            assertEquals(testStudentId, user.getStudentId());
            assertEquals(TEST_MAJOR, user.getMajor());
            assertEquals(TEST_GRADE, user.getGrade());
            assertEquals(100, user.getPoints());
            assertNotNull(user.getPassword());
        }

        @Test
        @Order(2)
        @DisplayName("用户注册 - 用户名已存在")
        void testUserRegisterUsernameExists() throws Exception {
            String existingEmail = generateRandomEmail();
            String existingStudentId = generateRandomStudentId();

            // 先手动插入测试数据
            SysUser existingUser = new SysUser();
            existingUser.setUsername(testUsername);
            existingUser.setEmail(existingEmail);
            existingUser.setPassword("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH");
            existingUser.setStudentId(existingStudentId);
            existingUser.setMajor(TEST_MAJOR);
            existingUser.setGrade(TEST_GRADE);
            existingUser.setPoints(100);
            existingUser.setVersion(0);
            userMapper.insert(existingUser);

            // 尝试使用相同的用户名注册
            String newEmail = generateRandomEmail();
            String newStudentId = generateRandomStudentId();

            RegisterDTO registerDTO = new RegisterDTO();
            registerDTO.setUsername(testUsername); // 重复的用户名
            registerDTO.setEmail(newEmail);
            registerDTO.setPassword(TEST_PASSWORD);
            registerDTO.setStudentId(newStudentId);
            registerDTO.setMajor(TEST_MAJOR);
            registerDTO.setGrade(TEST_GRADE);

            mockMvc.perform(post("/user/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(2003)) // 用户名已存在
                    .andExpect(jsonPath("$.msg").exists());

            // 清理测试数据
            cleanTestData(testUsername, existingEmail, existingStudentId);
            cleanTestData(null, newEmail, newStudentId);
        }

        @Test
        @Order(3)
        @DisplayName("用户注册 - 邮箱已存在")
        void testUserRegisterEmailExists() throws Exception {
            String existingUsername = generateRandomUsername();
            String existingStudentId = generateRandomStudentId();

            // 先手动插入测试数据
            SysUser existingUser = new SysUser();
            existingUser.setUsername(existingUsername);
            existingUser.setEmail(testEmail);
            existingUser.setPassword("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH");
            existingUser.setStudentId(existingStudentId);
            existingUser.setMajor(TEST_MAJOR);
            existingUser.setGrade(TEST_GRADE);
            existingUser.setPoints(100);
            existingUser.setVersion(0);
            userMapper.insert(existingUser);

            // 尝试使用相同的邮箱注册
            String newUsername = generateRandomUsername();
            String newStudentId = generateRandomStudentId();

            RegisterDTO registerDTO = new RegisterDTO();
            registerDTO.setUsername(newUsername);
            registerDTO.setEmail(testEmail); // 重复的邮箱
            registerDTO.setPassword(TEST_PASSWORD);
            registerDTO.setStudentId(newStudentId);
            registerDTO.setMajor(TEST_MAJOR);
            registerDTO.setGrade(TEST_GRADE);

            mockMvc.perform(post("/user/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(2004)); // 邮箱已存在

            // 清理测试数据
            cleanTestData(existingUsername, testEmail, existingStudentId);
            cleanTestData(newUsername, null, newStudentId);
        }

        @Test
        @Order(4)
        @DisplayName("用户注册 - 参数校验失败")
        void testUserRegisterValidationFailure() throws Exception {
            RegisterDTO registerDTO = new RegisterDTO();
            registerDTO.setUsername("ab"); // 用户名太短
            registerDTO.setEmail("invalid-email"); // 邮箱格式错误
            registerDTO.setPassword("123"); // 密码太短

            mockMvc.perform(post("/user/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(400)); // 参数错误
        }
    }

    @Nested
    @DisplayName("用户登录流程测试")
    class UserLoginTest {

        private String testUsername;
        private String testEmail;
        private String testStudentId;

        @BeforeEach
        void setUp() throws Exception {
            // 生成随机测试数据
            testUsername = generateRandomUsername();
            testEmail = generateRandomEmail();
            testStudentId = generateRandomStudentId();
            // 清理可能存在的旧数据
            cleanTestData(testUsername, testEmail, testStudentId);

            // 注册测试用户
            RegisterDTO registerDTO = new RegisterDTO();
            registerDTO.setUsername(testUsername);
            registerDTO.setEmail(testEmail);
            registerDTO.setPassword(TEST_PASSWORD);
            registerDTO.setStudentId(testStudentId);
            registerDTO.setMajor(TEST_MAJOR);
            registerDTO.setGrade(TEST_GRADE);

            MvcResult result = mockMvc.perform(post("/user/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerDTO)))
                    .andReturn();

            String response = result.getResponse().getContentAsString();
            Result<UserVO> resultObj = objectMapper.readValue(response,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, UserVO.class));
            testUserId = resultObj.getData().getId();
        }

        @Test
        @Order(5)
        @DisplayName("用户登录 - 成功")
        void testUserLoginSuccess() throws Exception {
            LoginDTO loginDTO = new LoginDTO();
            loginDTO.setUsername(testUsername);
            loginDTO.setPassword(TEST_PASSWORD);

            MvcResult result = mockMvc.perform(post("/user/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value("登录成功"))
                    .andExpect(jsonPath("$.data.token").exists())
                    .andExpect(jsonPath("$.data.userInfo.username").value(testUsername))
                    .andReturn();

            // 解析响应获取Token
            String response = result.getResponse().getContentAsString();
            Result<LoginVO> resultObj = objectMapper.readValue(response,
            objectMapper.getTypeFactory().constructParametricType(Result.class, LoginVO.class));
            testToken = resultObj.getData().getToken();
            // 验证Token有效性
            assertNotNull(testToken);
            Long userId = jwtUtil.getUserIdFromToken(testToken);
            assertEquals(testUserId, userId);
        }

        @Test
        @Order(6)
        @DisplayName("用户登录 - 用户名错误")
        void testUserLoginWrongUsername() throws Exception {
            LoginDTO loginDTO = new LoginDTO();
            loginDTO.setUsername("wrongusername");
            loginDTO.setPassword(TEST_PASSWORD);

            mockMvc.perform(post("/user/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(2002)); // 用户名或密码错误
        }

        @Test
        @Order(7)
        @DisplayName("用户登录 - 密码错误")
        void testUserLoginWrongPassword() throws Exception {
            LoginDTO loginDTO = new LoginDTO();
            loginDTO.setUsername(testUsername);
            loginDTO.setPassword("wrongpassword");

            mockMvc.perform(post("/user/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(2002)); // 用户名或密码错误
        }
    }

    @Nested
    @DisplayName("用户信息查询测试")
    class UserInfoQueryTest {

        private String testUsername;
        private String testEmail;
        private String testStudentId;

        @BeforeEach
        void setUp() throws Exception {
            // 生成随机测试数据
            testUsername = generateRandomUsername();
            testEmail = generateRandomEmail();
            testStudentId = generateRandomStudentId();
            // 清理可能存在的旧数据
            cleanTestData(testUsername, testEmail, testStudentId);

            // 注册测试用户
            RegisterDTO registerDTO = new RegisterDTO();
            registerDTO.setUsername(testUsername);
            registerDTO.setEmail(testEmail);
            registerDTO.setPassword(TEST_PASSWORD);
            registerDTO.setStudentId(testStudentId);
            registerDTO.setMajor(TEST_MAJOR);
            registerDTO.setGrade(TEST_GRADE);

            MvcResult result = mockMvc.perform(post("/user/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerDTO)))
                    .andReturn();

            String response = result.getResponse().getContentAsString();
            Result<UserVO> resultObj = objectMapper.readValue(response,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, UserVO.class));
            testUserId = resultObj.getData().getId();

            // 登录获取Token
            LoginDTO loginDTO = new LoginDTO();
            loginDTO.setUsername(testUsername);
            loginDTO.setPassword(TEST_PASSWORD);

            MvcResult loginResult = mockMvc.perform(post("/user/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginDTO)))
                    .andReturn();

            String loginResponse = loginResult.getResponse().getContentAsString();
            Result<LoginVO> loginResultObj = objectMapper.readValue(loginResponse,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, LoginVO.class));
            testToken = loginResultObj.getData().getToken();
        }

        @Test
        @Order(8)
        @DisplayName("获取当前用户信息 - 成功")
        void testGetCurrentUserInfoSuccess() throws Exception {
            mockMvc.perform(get("/user/info")
                            .header("Authorization", "Bearer " + testToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.id").value(testUserId))
                    .andExpect(jsonPath("$.data.username").value(testUsername));
        }

        @Test
        @Order(9)
        @DisplayName("获取用户信息 - 根据用户ID")
        void testGetUserByIdSuccess() throws Exception {
            mockMvc.perform(get("/user/" + testUserId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.id").value(testUserId))
                    .andExpect(jsonPath("$.data.username").value(testUsername));
        }

        @Test
        @Order(10)
        @DisplayName("获取用户信息 - 根据用户名")
        void testGetUserByUsernameSuccess() throws Exception {
            mockMvc.perform(get("/user/username/" + testUsername))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.id").value(testUserId))
                    .andExpect(jsonPath("$.data.username").value(testUsername));
        }

        @Test
        @Order(11)
        @DisplayName("检查用户名是否存在 - 存在")
        void testCheckUsernameExists() throws Exception {
            mockMvc.perform(get("/user/check/username")
                            .param("username", testUsername))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @Order(12)
        @DisplayName("检查用户名是否存在 - 不存在")
        void testCheckUsernameNotExists() throws Exception {
            mockMvc.perform(get("/user/check/username")
                            .param("username", "nonexistentuser"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(false));
        }

        @Test
        @Order(13)
        @DisplayName("检查学号是否存在 - 存在")
        void testCheckStudentIdExists() throws Exception {
            mockMvc.perform(get("/user/check/studentId")
                            .param("studentId", testStudentId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @Order(14)
        @DisplayName("获取用户信息 - 未登录（失败）")
        void testGetCurrentUserInfoWithoutAuth() throws Exception {
            mockMvc.perform(get("/user/info"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1001)); // 未登录
        }
    }

    @Nested
    @DisplayName("用户信息更新测试")
    class UserInfoUpdateTest {

        private String testUsername;
        private String testEmail;
        private String testStudentId;

        @BeforeEach
        void setUp() throws Exception {
            // 生成随机测试数据
            testUsername = generateRandomUsername();
            testEmail = generateRandomEmail();
            testStudentId = generateRandomStudentId();
            // 清理可能存在的旧数据
            cleanTestData(testUsername, testEmail, testStudentId);

            // 注册测试用户
            RegisterDTO registerDTO = new RegisterDTO();
            registerDTO.setUsername(testUsername);
            registerDTO.setEmail(testEmail);
            registerDTO.setPassword(TEST_PASSWORD);
            registerDTO.setStudentId(testStudentId);
            registerDTO.setMajor(TEST_MAJOR);
            registerDTO.setGrade(TEST_GRADE);

            MvcResult result = mockMvc.perform(post("/user/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerDTO)))
                    .andReturn();

            String response = result.getResponse().getContentAsString();
            Result<UserVO> resultObj = objectMapper.readValue(response,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, UserVO.class));
            testUserId = resultObj.getData().getId();

            // 登录获取Token
            LoginDTO loginDTO = new LoginDTO();
            loginDTO.setUsername(testUsername);
            loginDTO.setPassword(TEST_PASSWORD);

            MvcResult loginResult = mockMvc.perform(post("/user/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginDTO)))
                    .andReturn();

            String loginResponse = loginResult.getResponse().getContentAsString();
            Result<LoginVO> loginResultObj = objectMapper.readValue(loginResponse,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, LoginVO.class));
            testToken = loginResultObj.getData().getToken();
        }

        @Test
        @Order(15)
        @DisplayName("更新用户信息 - 成功")
        void testUpdateUserInfoSuccess() throws Exception {
            UserUpdateDTO updateDTO = new UserUpdateDTO();
            updateDTO.setUserId(testUserId);
            updateDTO.setMajor("软件工程");
            updateDTO.setGrade("2025级");
            updateDTO.setAvatar("https://example.com/avatar.jpg");
            mockMvc.perform(put("/user/update")
                            .header("Authorization", "Bearer " + testToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value("更新成功"))
                    .andExpect(jsonPath("$.data.major").value("软件工程"))
                    .andExpect(jsonPath("$.data.grade").value("2025级"))
                    .andExpect(jsonPath("$.data.avatar").value("https://example.com/avatar.jpg"));

            // 验证数据库中的数据已更新
            SysUser user = userMapper.selectById(testUserId);
            assertEquals("软件工程", user.getMajor());
            assertEquals("2025级", user.getGrade());
            assertEquals("https://example.com/avatar.jpg", user.getAvatar());
        }

        @Test
        @Order(16)
        @DisplayName("更新用户信息 - 未登录（失败）")
        void testUpdateUserInfoWithoutAuth() throws Exception {
            UserUpdateDTO updateDTO = new UserUpdateDTO();
            updateDTO.setMajor("新专业");

            mockMvc.perform(put("/user/update")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1001)); // 未登录
        }
    }

    @Nested
    @DisplayName("用户登出测试")
    class UserLogoutTest {

        private String testUsername;
        private String testEmail;
        private String testStudentId;

        @BeforeEach
        void setUp() throws Exception {
            // 生成随机测试数据
            testUsername = generateRandomUsername();
            testEmail = generateRandomEmail();
            testStudentId = generateRandomStudentId();
            // 清理可能存在的旧数据
            cleanTestData(testUsername, testEmail, testStudentId);

            // 注册测试用户
            RegisterDTO registerDTO = new RegisterDTO();
            registerDTO.setUsername(testUsername);
            registerDTO.setEmail(testEmail);
            registerDTO.setPassword(TEST_PASSWORD);
            registerDTO.setStudentId(testStudentId);
            registerDTO.setMajor(TEST_MAJOR);
            registerDTO.setGrade(TEST_GRADE);

            MvcResult result = mockMvc.perform(post("/user/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerDTO)))
                    .andReturn();

            String response = result.getResponse().getContentAsString();
            Result<UserVO> resultObj = objectMapper.readValue(response,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, UserVO.class));
            testUserId = resultObj.getData().getId();

            // 登录获取Token
            LoginDTO loginDTO = new LoginDTO();
            loginDTO.setUsername(testUsername);
            loginDTO.setPassword(TEST_PASSWORD);

            MvcResult loginResult = mockMvc.perform(post("/user/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginDTO)))
                    .andReturn();

            String loginResponse = loginResult.getResponse().getContentAsString();
            Result<LoginVO> loginResultObj = objectMapper.readValue(loginResponse,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, LoginVO.class));
            testToken = loginResultObj.getData().getToken();
        }

        @Test
        @Order(17)
        @DisplayName("用户登出 - 成功")
        void testUserLogoutSuccess() throws Exception {
            mockMvc.perform(post("/user/logout")
                            .header("Authorization", "Bearer " + testToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value("登出成功"));
        }
    }

    @Nested
    @DisplayName("完整业务流程测试")
    class CompleteWorkflowTest {

        @Test
        @Order(18)
        @DisplayName("用户注册 -> 登录 -> 获取信息 -> 更新信息 -> 登出")
        void testCompleteUserWorkflow() throws Exception {
            String username = generateRandomUsername();
            String email = generateRandomEmail();
            String studentId = generateRandomStudentId();
            String password = "workflow123";

            // 清理可能存在的旧数据
            cleanTestData(username, email, studentId);

            // 1. 注册
            RegisterDTO registerDTO = new RegisterDTO();
            registerDTO.setUsername(username);
            registerDTO.setEmail(email);
            registerDTO.setPassword(password);
            registerDTO.setStudentId(studentId);

            MvcResult registerResult = mockMvc.perform(post("/user/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andReturn();

            String registerResponse = registerResult.getResponse().getContentAsString();
            Result<UserVO> registerResultObj = objectMapper.readValue(registerResponse,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, UserVO.class));
            Long userId = registerResultObj.getData().getId();

            // 2. 登录
            LoginDTO loginDTO = new LoginDTO();
            loginDTO.setUsername(username);
            loginDTO.setPassword(password);

            MvcResult loginResult = mockMvc.perform(post("/user/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andReturn();

            String loginResponse = loginResult.getResponse().getContentAsString();
            Result<LoginVO> loginResultObj = objectMapper.readValue(loginResponse,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, LoginVO.class));
            String token = loginResultObj.getData().getToken();

            // 3. 获取用户信息
            mockMvc.perform(get("/user/info")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.id").value(userId))
                    .andExpect(jsonPath("$.data.username").value(username));

            // 4. 更新用户信息
            UserUpdateDTO updateDTO = new UserUpdateDTO();
            updateDTO.setUserId(userId);
            updateDTO.setMajor("工作流测试专业");

            mockMvc.perform(put("/user/update")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            // 5. 登出
            mockMvc.perform(post("/user/logout")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            // 6. 验证数据库状态
            SysUser user = userMapper.selectById(userId);
            assertNotNull(user);

            // 清理测试数据
            cleanTestData(username, email, studentId);
        }
    }
}