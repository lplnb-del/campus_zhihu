package com.campus.zhihu.integration;

import com.campus.zhihu.CampusZhihuApplication;
import com.campus.zhihu.common.Result;
import com.campus.zhihu.dto.LoginDTO;
import com.campus.zhihu.dto.QuestionPublishDTO;
import com.campus.zhihu.dto.QuestionQueryDTO;
import com.campus.zhihu.dto.QuestionUpdateDTO;
import com.campus.zhihu.dto.RegisterDTO;
import com.campus.zhihu.entity.BizQuestion;
import com.campus.zhihu.entity.BizTag;
import com.campus.zhihu.entity.SysUser;
import com.campus.zhihu.mapper.QuestionMapper;
import com.campus.zhihu.mapper.TagMapper;
import com.campus.zhihu.mapper.UserMapper;
import com.campus.zhihu.vo.LoginVO;
import com.campus.zhihu.vo.QuestionVO;
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

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 问题模块集成测试
 * 测试问题的发布、查询、更新、删除等完整业务流程
 *
 * @author CampusZhihu Team
 */
@SpringBootTest(classes = CampusZhihuApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("问题模块集成测试")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class QuestionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private TagMapper tagMapper;

    private static Long testQuestionId;
    private static Long testTagId1;
    private static Long testTagId2;

    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_MAJOR = "计算机科学与技术";
    private static final String TEST_GRADE = "2024级";

    private static final Random random = new Random();

    // 生成随机用户名（3-20个字符）
    private String generateRandomUsername() {
        String uuidPart = UUID.randomUUID().toString().substring(0, 8);
        return "q" + uuidPart;
    }

    // 生成随机邮箱
    private String generateRandomEmail() {
        String uuidPart = UUID.randomUUID().toString().substring(0, 8);
        return "q" + uuidPart + "@example.com";
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
        // 初始化测试标签（使用随机名称避免冲突）
        BizTag tag1 = new BizTag();
        tag1.setName("Tag_" + generateRandomUsername());
        tag1.setDescription("测试标签1");
        tag1.setColor("#FF0000");
        tagMapper.insert(tag1);
        testTagId1 = tag1.getId();

        BizTag tag2 = new BizTag();
        tag2.setName("Tag_" + generateRandomUsername());
        tag2.setDescription("测试标签2");
        tag2.setColor("#00FF00");
        tagMapper.insert(tag2);
        testTagId2 = tag2.getId();
    }

    /**
     * 生成随机标签名称
     */
    private String generateRandomTagName() {
        return "Tag_" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    @Nested
    @DisplayName("问题发布流程测试")
    class QuestionPublishTest {

        private Long testUserId;
        private String testToken;

        @BeforeEach
        void setUp() throws Exception {
            // 生成随机测试数据
            String testUsername = generateRandomUsername();
            String testEmail = generateRandomEmail();
            String testStudentId = generateRandomStudentId();

            // 清理可能存在的旧数据
            cleanTestData(testUsername, testEmail, testStudentId);

            // 使用注册 API 创建测试用户（这样密码会正确加密）
            RegisterDTO registerDTO = new RegisterDTO();
            registerDTO.setUsername(testUsername);
            registerDTO.setEmail(testEmail);
            registerDTO.setPassword(TEST_PASSWORD);
            registerDTO.setStudentId(testStudentId);
            registerDTO.setMajor(TEST_MAJOR);
            registerDTO.setGrade(TEST_GRADE);

            MvcResult registerResult = mockMvc.perform(post("/user/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String registerResponse = registerResult.getResponse().getContentAsString();
            Result<UserVO> registerResultObj = objectMapper.readValue(registerResponse,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, UserVO.class));
            testUserId = registerResultObj.getData().getId();

            // 登录获取Token
            LoginDTO loginDTO = new LoginDTO();
            loginDTO.setUsername(testUsername);
            loginDTO.setPassword(TEST_PASSWORD);

            MvcResult result = mockMvc.perform(post("/user/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String response = result.getResponse().getContentAsString();
            Result<LoginVO> resultObj = objectMapper.readValue(response,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, LoginVO.class));

            // 检查登录是否成功
            if (resultObj.getData() == null) {
                System.out.println("Login failed for user: " + testUsername);
                System.out.println("Login response: " + response);
                throw new RuntimeException("登录失败: " + resultObj.getCode() + " - " + resultObj.getMsg());
            }

            testToken = resultObj.getData().getToken();
        }

        @Test
        @Order(1)
        @DisplayName("发布问题 - 成功")
        void testPublishQuestionSuccess() throws Exception {
            QuestionPublishDTO publishDTO = new QuestionPublishDTO();
            publishDTO.setTitle("如何使用Spring Boot开发REST API？");
            publishDTO.setContent("我想学习Spring Boot，请问如何快速上手开发REST API？");
            publishDTO.setTagIds(Arrays.asList(testTagId1, testTagId2));
            publishDTO.setRewardPoints(50);
            publishDTO.setPublish(true);

            MvcResult result = mockMvc.perform(post("/question/publish")
                            .header("Authorization", "Bearer " + testToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(publishDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value("问题发布成功"))
                    .andExpect(jsonPath("$.data.title").value("如何使用Spring Boot开发REST API？"))
                    .andExpect(jsonPath("$.data.userId").value(testUserId))
                    .andExpect(jsonPath("$.data.rewardPoints").value(50))
                    .andExpect(jsonPath("$.data.status").value(1)) // 已发布
                    .andExpect(jsonPath("$.data.isResolved").value(0)) // 未解决
                    .andReturn();

            // 解析响应获取问题ID
            String response = result.getResponse().getContentAsString();
            Result<QuestionVO> resultObj = objectMapper.readValue(response,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, QuestionVO.class));
            testQuestionId = resultObj.getData().getId();

            // 验证数据库中的数据
            BizQuestion question = questionMapper.selectById(testQuestionId);
            assertNotNull(question);
            assertEquals("如何使用Spring Boot开发REST API？", question.getTitle());
            assertEquals(testUserId, question.getUserId());
            assertEquals(50, question.getRewardPoints());
            assertEquals(1, question.getStatus());
            assertEquals(0, question.getIsSolved());

            // 验证积分已扣除
            SysUser user = userMapper.selectById(testUserId);
            assertEquals(50, user.getPoints()); // 100 - 50 = 50
        }

        @Test
        @Order(2)
        @DisplayName("发布问题 - 草稿")
        void testPublishQuestionAsDraft() throws Exception {
            QuestionPublishDTO publishDTO = new QuestionPublishDTO();
            publishDTO.setTitle("这是一个草稿问题标题");
            publishDTO.setContent("这是一个草稿问题的详细内容描述，需要至少10个字符长度才能通过参数校验。");
            publishDTO.setTagIds(Arrays.asList(testTagId1));
            publishDTO.setPublish(false);

            mockMvc.perform(post("/question/publish")
                            .header("Authorization", "Bearer " + testToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(publishDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.status").value(0)); // 草稿
        }

        @Test
        @Order(3)
        @DisplayName("发布问题 - 积分不足")
        void testPublishQuestionInsufficientPoints() throws Exception {
            // 修改用户积分为10
            SysUser user = userMapper.selectById(testUserId);
            user.setPoints(10);
            userMapper.updateById(user);

            QuestionPublishDTO publishDTO = new QuestionPublishDTO();
            publishDTO.setTitle("积分不足测试标题");
            publishDTO.setContent("这是积分不足测试的内容，需要至少10个字符长度。");
            publishDTO.setTagIds(Arrays.asList(testTagId1));
            publishDTO.setRewardPoints(50); // 悬赏50积分，但只有10积分
            publishDTO.setPublish(true); // 发布正式问题

            mockMvc.perform(post("/question/publish")
                            .header("Authorization", "Bearer " + testToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(publishDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(2006)); // 积分不足
        }

        @Test
        @Order(4)
        @DisplayName("发布问题 - 参数校验失败")
        void testPublishQuestionValidationFailure() throws Exception {
            QuestionPublishDTO publishDTO = new QuestionPublishDTO();
            publishDTO.setTitle("太短"); // 标题太短（少于5个字符）
            publishDTO.setContent("内容太短"); // 内容太短（少于10个字符）
            publishDTO.setTagIds(Arrays.asList()); // 标签为空（少于1个）

            mockMvc.perform(post("/question/publish")
                            .header("Authorization", "Bearer " + testToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(publishDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(400)); // 参数错误
        }

        @Test
        @Order(5)
        @DisplayName("发布问题 - 未登录（失败）")
        void testPublishQuestionWithoutAuth() throws Exception {
            QuestionPublishDTO publishDTO = new QuestionPublishDTO();
            publishDTO.setTitle("未登录测试问题标题");
            publishDTO.setContent("这是未登录测试的问题内容，需要至少10个字符长度。");
            publishDTO.setTagIds(Arrays.asList(testTagId1));

            mockMvc.perform(post("/question/publish")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(publishDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1001)); // 未登录
        }
    }

    @Nested
    @DisplayName("问题查询流程测试")
    class QuestionQueryTest {

        private Long testUserId;
        private String testToken;

        @BeforeEach
        void setUp() throws Exception {
            // 生成随机测试数据
            String testUsername = generateRandomUsername();
            String testEmail = generateRandomEmail();
            String testStudentId = generateRandomStudentId();

            // 清理可能存在的旧数据
            cleanTestData(testUsername, testEmail, testStudentId);

            // 使用注册 API 创建测试用户（这样密码会正确加密）
            RegisterDTO registerDTO = new RegisterDTO();
            registerDTO.setUsername(testUsername);
            registerDTO.setEmail(testEmail);
            registerDTO.setPassword(TEST_PASSWORD);
            registerDTO.setStudentId(testStudentId);
            registerDTO.setMajor(TEST_MAJOR);
            registerDTO.setGrade(TEST_GRADE);

            MvcResult registerResult = mockMvc.perform(post("/user/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String registerResponse = registerResult.getResponse().getContentAsString();
            Result<UserVO> registerResultObj = objectMapper.readValue(registerResponse,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, UserVO.class));
            testUserId = registerResultObj.getData().getId();

            // 登录获取Token
            LoginDTO loginDTO = new LoginDTO();
            loginDTO.setUsername(testUsername);
            loginDTO.setPassword(TEST_PASSWORD);

            MvcResult result = mockMvc.perform(post("/user/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String response = result.getResponse().getContentAsString();
            Result<LoginVO> resultObj = objectMapper.readValue(response,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, LoginVO.class));

            // 检查登录是否成功
            if (resultObj.getData() == null) {
                System.out.println("Login failed for user: " + testUsername);
                System.out.println("Login response: " + response);
                throw new RuntimeException("登录失败: " + resultObj.getCode() + " - " + resultObj.getMsg());
            }

            testToken = resultObj.getData().getToken();
        }

        @Test
        @Order(6)
        @DisplayName("获取问题详情 - 成功")
        void testGetQuestionByIdSuccess() throws Exception {
            // 先创建一个问题
            QuestionPublishDTO publishDTO = new QuestionPublishDTO();
            publishDTO.setTitle("测试问题详情标题");
            publishDTO.setContent("这是测试问题详情的内容描述，需要至少10个字符长度才能通过参数校验。");
            publishDTO.setTagIds(Arrays.asList(testTagId1));
            publishDTO.setRewardPoints(10);

            MvcResult result = mockMvc.perform(post("/question/publish")
                            .header("Authorization", "Bearer " + testToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(publishDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String response = result.getResponse().getContentAsString();
            Result<QuestionVO> resultObj = objectMapper.readValue(response,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, QuestionVO.class));
            Long questionId = resultObj.getData().getId();

            // 查询问题详情
            mockMvc.perform(get("/question/" + questionId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.id").value(questionId))
                    .andExpect(jsonPath("$.data.title").value("测试问题详情标题"))
                    .andExpect(jsonPath("$.data.userId").value(testUserId));
        }

        @Test
        @Order(7)
        @DisplayName("获取问题列表 - 分页")
        void testGetQuestionListPage() throws Exception {
            mockMvc.perform(get("/question/list")
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.list").isArray())
                    .andExpect(jsonPath("$.data.total").exists())
                    .andExpect(jsonPath("$.data.page").value(1))
                    .andExpect(jsonPath("$.data.size").value(10));
        }

        @Test
        @Order(8)
        @DisplayName("获取问题列表 - 按标签筛选")
        void testGetQuestionListByTag() throws Exception {
            mockMvc.perform(get("/question/list")
                            .param("tagId", testTagId1.toString())
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.list").isArray());
        }

        @Test
        @Order(9)
        @DisplayName("获取问题列表 - 按用户筛选")
        void testGetQuestionListByUser() throws Exception {
            mockMvc.perform(get("/question/list")
                            .param("userId", testUserId.toString())
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.list").isArray());
        }

        @Test
        @Order(10)
        @DisplayName("获取热门问题")
        void testGetHotQuestions() throws Exception {
            mockMvc.perform(get("/question/hot")
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.list").isArray());
        }

        @Test
        @Order(11)
        @DisplayName("获取最新问题")
        void testGetLatestQuestions() throws Exception {
            mockMvc.perform(get("/question/latest")
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.list").isArray());
        }

        @Test
        @Order(12)
        @DisplayName("获取待解决问题")
        void testGetUnsolvedQuestions() throws Exception {
            mockMvc.perform(get("/question/unsolved")
                            .param("page", "1")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.list").isArray());
        }

        @Test
        @Order(13)
        @DisplayName("获取我的问题")
        void testGetMyQuestions() throws Exception {
            // 先创建一个问题
            QuestionPublishDTO publishDTO = new QuestionPublishDTO();
            publishDTO.setTitle("我的测试问题标题");
            publishDTO.setContent("这是我的测试问题内容，需要至少10个字符长度才能通过参数校验。");
            publishDTO.setTagIds(Arrays.asList(testTagId1));
            publishDTO.setRewardPoints(10);

            mockMvc.perform(post("/question/publish")
                            .header("Authorization", "Bearer " + testToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(publishDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            // 查询我的问题
            mockMvc.perform(get("/question/my")
                            .header("Authorization", "Bearer " + testToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.list").isArray());
        }

        @Test
        @Order(14)
        @DisplayName("搜索问题")
        void testSearchQuestions() throws Exception {
            mockMvc.perform(get("/question/search")
                            .param("keyword", "Spring Boot")
                            .param("pageNum", "1")
                            .param("pageSize", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.records").isArray());
        }
    }

    @Nested
    @DisplayName("问题更新流程测试")
    class QuestionUpdateTest {

        private Long testUserId;
        private String testToken;

        @BeforeEach
        void setUp() throws Exception {
            // 生成随机测试数据
            String testUsername = generateRandomUsername();
            String testEmail = generateRandomEmail();
            String testStudentId = generateRandomStudentId();

            // 清理可能存在的旧数据
            cleanTestData(testUsername, testEmail, testStudentId);

            // 使用注册 API 创建测试用户（这样密码会正确加密）
            RegisterDTO registerDTO = new RegisterDTO();
            registerDTO.setUsername(testUsername);
            registerDTO.setEmail(testEmail);
            registerDTO.setPassword(TEST_PASSWORD);
            registerDTO.setStudentId(testStudentId);
            registerDTO.setMajor(TEST_MAJOR);
            registerDTO.setGrade(TEST_GRADE);

            MvcResult registerResult = mockMvc.perform(post("/user/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String registerResponse = registerResult.getResponse().getContentAsString();
            Result<UserVO> registerResultObj = objectMapper.readValue(registerResponse,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, UserVO.class));
            testUserId = registerResultObj.getData().getId();

            // 登录获取Token
            LoginDTO loginDTO = new LoginDTO();
            loginDTO.setUsername(testUsername);
            loginDTO.setPassword(TEST_PASSWORD);

            MvcResult result = mockMvc.perform(post("/user/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String response = result.getResponse().getContentAsString();
            Result<LoginVO> resultObj = objectMapper.readValue(response,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, LoginVO.class));

            // 检查登录是否成功
            if (resultObj.getData() == null) {
                System.out.println("Login failed for user: " + testUsername);
                System.out.println("Login response: " + response);
                throw new RuntimeException("登录失败: " + resultObj.getCode() + " - " + resultObj.getMsg());
            }

            testToken = resultObj.getData().getToken();
        }

        @Test
        @Order(15)
        @DisplayName("更新问题 - 成功")
        void testUpdateQuestionSuccess() throws Exception {
            // 先创建一个问题
            QuestionPublishDTO publishDTO = new QuestionPublishDTO();
            publishDTO.setTitle("原始问题标题");
            publishDTO.setContent("这是原始问题的内容描述，需要至少10个字符长度才能通过参数校验。");
            publishDTO.setTagIds(Arrays.asList(testTagId1));
            publishDTO.setRewardPoints(10);

            MvcResult result = mockMvc.perform(post("/question/publish")
                            .header("Authorization", "Bearer " + testToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(publishDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String response = result.getResponse().getContentAsString();
            Result<QuestionVO> resultObj = objectMapper.readValue(response,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, QuestionVO.class));
            Long questionId = resultObj.getData().getId();

            // 更新问题
            QuestionUpdateDTO updateDTO = new QuestionUpdateDTO();
            updateDTO.setTitle("更新后的问题标题");
            updateDTO.setContent("这是更新后的问题内容描述，需要至少10个字符长度才能通过参数校验。");
            updateDTO.setTagIds(Arrays.asList(testTagId1));

            mockMvc.perform(put("/question/" + questionId)
                            .header("Authorization", "Bearer " + testToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value("问题更新成功"))
                    .andExpect(jsonPath("$.data.title").value("更新后的问题标题"))
                    .andExpect(jsonPath("$.data.content").value("这是更新后的问题内容描述，需要至少10个字符长度才能通过参数校验。"));

            // 验证数据库中的数据已更新
            BizQuestion question = questionMapper.selectById(questionId);
            assertEquals("更新后的问题标题", question.getTitle());
            assertEquals("这是更新后的问题内容描述，需要至少10个字符长度才能通过参数校验。", question.getContent());
        }

        @Test
        @Order(16)
        @DisplayName("更新问题 - 无权限（非本人）")
        void testUpdateQuestionNoPermission() throws Exception {
            // 先创建一个问题
            QuestionPublishDTO publishDTO = new QuestionPublishDTO();
            publishDTO.setTitle("权限测试问题标题");
            publishDTO.setContent("这是权限测试问题的内容，需要至少10个字符长度才能通过参数校验。");
            publishDTO.setTagIds(Arrays.asList(testTagId1));
            publishDTO.setRewardPoints(10);

            MvcResult result = mockMvc.perform(post("/question/publish")
                            .header("Authorization", "Bearer " + testToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(publishDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String response = result.getResponse().getContentAsString();
            Result<QuestionVO> resultObj = objectMapper.readValue(response,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, QuestionVO.class));
            Long questionId = resultObj.getData().getId();

            // 创建另一个用户
            String anotherUsername = generateRandomUsername();
            String anotherEmail = generateRandomEmail();
            String anotherStudentId = generateRandomStudentId();
            cleanTestData(anotherUsername, anotherEmail, anotherStudentId);

            // 使用注册 API 创建另一个用户（这样密码会正确加密）
            RegisterDTO anotherRegisterDTO = new RegisterDTO();
            anotherRegisterDTO.setUsername(anotherUsername);
            anotherRegisterDTO.setEmail(anotherEmail);
            anotherRegisterDTO.setPassword(TEST_PASSWORD);
            anotherRegisterDTO.setStudentId(anotherStudentId);
            anotherRegisterDTO.setMajor(TEST_MAJOR);
            anotherRegisterDTO.setGrade(TEST_GRADE);

            MvcResult anotherRegisterResult = mockMvc.perform(post("/user/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(anotherRegisterDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            // 登录另一个用户
            LoginDTO loginDTO = new LoginDTO();
            loginDTO.setUsername(anotherUsername);
            loginDTO.setPassword(TEST_PASSWORD);

            MvcResult loginResult = mockMvc.perform(post("/user/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String loginResponse = loginResult.getResponse().getContentAsString();
            Result<LoginVO> loginResultObj = objectMapper.readValue(loginResponse,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, LoginVO.class));
            String anotherToken = loginResultObj.getData().getToken();

            // 尝试更新问题
            QuestionUpdateDTO updateDTO = new QuestionUpdateDTO();
            updateDTO.setTitle("非法更新的问题标题");

            mockMvc.perform(put("/question/" + questionId)
                            .header("Authorization", "Bearer " + anotherToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1004)); // 无权限
        }

        @Test
        @Order(17)
        @DisplayName("更新问题 - 问题不存在")
        void testUpdateQuestionNotFound() throws Exception {
            QuestionUpdateDTO updateDTO = new QuestionUpdateDTO();
            updateDTO.setTitle("测试更新不存在的问题标题");
            updateDTO.setContent("这是测试更新的问题内容，长度至少需要10个字符才能通过参数校验。");
            updateDTO.setTagIds(Arrays.asList(testTagId1));

            mockMvc.perform(put("/question/999999")
                            .header("Authorization", "Bearer " + testToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(400)); // 问题不存在
        }
    }

    @Nested
    @DisplayName("问题删除流程测试")
    class QuestionDeleteTest {

        private Long testUserId;
        private String testToken;

        @BeforeEach
        void setUp() throws Exception {
            // 生成随机测试数据
            String testUsername = generateRandomUsername();
            String testEmail = generateRandomEmail();
            String testStudentId = generateRandomStudentId();

            // 清理可能存在的旧数据
            cleanTestData(testUsername, testEmail, testStudentId);

            // 使用注册 API 创建测试用户（这样密码会正确加密）
            RegisterDTO registerDTO = new RegisterDTO();
            registerDTO.setUsername(testUsername);
            registerDTO.setEmail(testEmail);
            registerDTO.setPassword(TEST_PASSWORD);
            registerDTO.setStudentId(testStudentId);
            registerDTO.setMajor(TEST_MAJOR);
            registerDTO.setGrade(TEST_GRADE);

            MvcResult registerResult = mockMvc.perform(post("/user/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String registerResponse = registerResult.getResponse().getContentAsString();
            Result<UserVO> registerResultObj = objectMapper.readValue(registerResponse,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, UserVO.class));
            testUserId = registerResultObj.getData().getId();

            // 登录获取Token
            LoginDTO loginDTO = new LoginDTO();
            loginDTO.setUsername(testUsername);
            loginDTO.setPassword(TEST_PASSWORD);

            MvcResult result = mockMvc.perform(post("/user/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String response = result.getResponse().getContentAsString();
            Result<LoginVO> resultObj = objectMapper.readValue(response,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, LoginVO.class));

            // 检查登录是否成功
            if (resultObj.getData() == null) {
                System.out.println("Login failed for user: " + testUsername);
                System.out.println("Login response: " + response);
                throw new RuntimeException("登录失败: " + resultObj.getCode() + " - " + resultObj.getMsg());
            }

            testToken = resultObj.getData().getToken();
        }

        @Test
        @Order(18)
        @DisplayName("删除问题 - 成功")
        void testDeleteQuestionSuccess() throws Exception {
            // 先创建一个新问题用于删除测试
            QuestionPublishDTO publishDTO = new QuestionPublishDTO();
            publishDTO.setTitle("待删除的问题标题");
            publishDTO.setContent("这是一个待删除的问题内容，需要至少10个字符长度才能通过参数校验。");
            publishDTO.setTagIds(Arrays.asList(testTagId1));

            MvcResult result = mockMvc.perform(post("/question/publish")
                            .header("Authorization", "Bearer " + testToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(publishDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String response = result.getResponse().getContentAsString();
            Result<QuestionVO> resultObj = objectMapper.readValue(response,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, QuestionVO.class));
            Long questionToDelete = resultObj.getData().getId();

            // 删除问题
            mockMvc.perform(delete("/question/" + questionToDelete)
                            .header("Authorization", "Bearer " + testToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value("问题删除成功"))
                    .andExpect(jsonPath("$.data").value(true));

            // 验证问题已被逻辑删除
            BizQuestion question = questionMapper.selectById(questionToDelete);
            assertNull(question); // MyBatis-Plus逻辑删除会返回null
        }

        @Test
        @Order(19)
        @DisplayName("删除问题 - 未登录")
        void testDeleteQuestionWithoutAuth() throws Exception {
            mockMvc.perform(delete("/question/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1001)); // 未登录
        }
    }

    @Nested
    @DisplayName("问题其他操作测试")
    class QuestionOtherOperationsTest {

        private Long testUserId;
        private String testToken;

        @BeforeEach
        void setUp() throws Exception {
            // 生成随机测试数据
            String testUsername = generateRandomUsername();
            String testEmail = generateRandomEmail();
            String testStudentId = generateRandomStudentId();

            // 清理可能存在的旧数据
            cleanTestData(testUsername, testEmail, testStudentId);

            // 使用注册 API 创建测试用户（这样密码会正确加密）
            RegisterDTO registerDTO = new RegisterDTO();
            registerDTO.setUsername(testUsername);
            registerDTO.setEmail(testEmail);
            registerDTO.setPassword(TEST_PASSWORD);
            registerDTO.setStudentId(testStudentId);
            registerDTO.setMajor(TEST_MAJOR);
            registerDTO.setGrade(TEST_GRADE);

            MvcResult registerResult = mockMvc.perform(post("/user/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String registerResponse = registerResult.getResponse().getContentAsString();
            Result<UserVO> registerResultObj = objectMapper.readValue(registerResponse,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, UserVO.class));
            testUserId = registerResultObj.getData().getId();

            // 登录获取Token
            LoginDTO loginDTO = new LoginDTO();
            loginDTO.setUsername(testUsername);
            loginDTO.setPassword(TEST_PASSWORD);

            MvcResult result = mockMvc.perform(post("/user/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String response = result.getResponse().getContentAsString();
            Result<LoginVO> resultObj = objectMapper.readValue(response,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, LoginVO.class));

            // 检查登录是否成功
            if (resultObj.getData() == null) {
                System.out.println("Login failed for user: " + testUsername);
                System.out.println("Login response: " + response);
                throw new RuntimeException("登录失败: " + resultObj.getCode() + " - " + resultObj.getMsg());
            }

            testToken = resultObj.getData().getToken();
        }

        @Test
        @Order(20)
        @DisplayName("关闭问题 - 成功")
        void testCloseQuestionSuccess() throws Exception {
            // 先创建一个问题
            QuestionPublishDTO publishDTO = new QuestionPublishDTO();
            publishDTO.setTitle("关闭测试问题标题");
            publishDTO.setContent("这是关闭测试问题的内容，需要至少10个字符长度才能通过参数校验。");
            publishDTO.setTagIds(Arrays.asList(testTagId1));
            publishDTO.setRewardPoints(10);

            MvcResult result = mockMvc.perform(post("/question/publish")
                            .header("Authorization", "Bearer " + testToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(publishDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String response = result.getResponse().getContentAsString();
            Result<QuestionVO> resultObj = objectMapper.readValue(response,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, QuestionVO.class));
            Long questionId = resultObj.getData().getId();

            // 关闭问题
            mockMvc.perform(put("/question/" + questionId + "/close")
                            .header("Authorization", "Bearer " + testToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value("问题已关闭"));

            // 验证问题状态
            BizQuestion question = questionMapper.selectById(questionId);
            assertEquals(2, question.getStatus()); // 已关闭
        }

        @Test
        @Order(21)
        @DisplayName("标记问题已解决 - 成功")
        void testMarkQuestionAsResolvedSuccess() throws Exception {
            // 先创建一个问题
            QuestionPublishDTO publishDTO = new QuestionPublishDTO();
            publishDTO.setTitle("解决测试问题标题");
            publishDTO.setContent("这是解决测试问题的内容，需要至少10个字符长度才能通过参数校验。");
            publishDTO.setTagIds(Arrays.asList(testTagId1));
            publishDTO.setRewardPoints(10);

            MvcResult result = mockMvc.perform(post("/question/publish")
                            .header("Authorization", "Bearer " + testToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(publishDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String response = result.getResponse().getContentAsString();
            Result<QuestionVO> resultObj = objectMapper.readValue(response,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, QuestionVO.class));
            Long questionId = resultObj.getData().getId();

            // 标记问题已解决
            mockMvc.perform(put("/question/" + questionId + "/resolve")
                            .header("Authorization", "Bearer " + testToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value("问题已标记为已解决"));

            // 验证问题解决状态
            BizQuestion question = questionMapper.selectById(questionId);
            assertEquals(1, question.getIsSolved()); // 已解决
        }

        @Test
        @Order(22)
        @DisplayName("统计用户问题数量")
        void testCountUserQuestions() throws Exception {
            // 先创建一个问题
            QuestionPublishDTO publishDTO = new QuestionPublishDTO();
            publishDTO.setTitle("统计测试问题标题");
            publishDTO.setContent("这是统计测试问题的内容，需要至少10个字符长度才能通过参数校验。");
            publishDTO.setTagIds(Arrays.asList(testTagId1));
            publishDTO.setRewardPoints(10);

            mockMvc.perform(post("/question/publish")
                            .header("Authorization", "Bearer " + testToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(publishDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            // 统计用户问题数量
            mockMvc.perform(get("/question/count/user/" + testUserId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(1)); // 已发布1个问题
        }
    }

    @Nested
    @DisplayName("完整业务流程测试")
    class CompleteWorkflowTest {

        private Long testUserId;
        private String testToken;

        @BeforeEach
        void setUp() throws Exception {
            // 生成随机测试数据
            String testUsername = generateRandomUsername();
            String testEmail = generateRandomEmail();
            String testStudentId = generateRandomStudentId();

            // 清理可能存在的旧数据
            cleanTestData(testUsername, testEmail, testStudentId);

            // 使用注册 API 创建测试用户（这样密码会正确加密）
            RegisterDTO registerDTO = new RegisterDTO();
            registerDTO.setUsername(testUsername);
            registerDTO.setEmail(testEmail);
            registerDTO.setPassword(TEST_PASSWORD);
            registerDTO.setStudentId(testStudentId);
            registerDTO.setMajor(TEST_MAJOR);
            registerDTO.setGrade(TEST_GRADE);

            MvcResult registerResult = mockMvc.perform(post("/user/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registerDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String registerResponse = registerResult.getResponse().getContentAsString();
            Result<UserVO> registerResultObj = objectMapper.readValue(registerResponse,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, UserVO.class));
            testUserId = registerResultObj.getData().getId();

            // 登录获取Token
            LoginDTO loginDTO = new LoginDTO();
            loginDTO.setUsername(testUsername);
            loginDTO.setPassword(TEST_PASSWORD);

            MvcResult result = mockMvc.perform(post("/user/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String response = result.getResponse().getContentAsString();
            Result<LoginVO> resultObj = objectMapper.readValue(response,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, LoginVO.class));

            // 检查登录是否成功
            if (resultObj.getData() == null) {
                System.out.println("Login failed for user: " + testUsername);
                System.out.println("Login response: " + response);
                throw new RuntimeException("登录失败: " + resultObj.getCode() + " - " + resultObj.getMsg());
            }

            testToken = resultObj.getData().getToken();
        }

        @Test
        @Order(23)
        @DisplayName("发布问题 -> 查询详情 -> 更新问题 -> 删除问题")
        void testCompleteQuestionWorkflow() throws Exception {
            // 1. 发布问题
            QuestionPublishDTO publishDTO = new QuestionPublishDTO();
            publishDTO.setTitle("完整流程测试问题标题");
            publishDTO.setContent("这是完整流程测试的内容，需要至少10个字符长度才能通过参数校验。");
            publishDTO.setTagIds(Arrays.asList(testTagId1));
            publishDTO.setRewardPoints(20);

            MvcResult publishResult = mockMvc.perform(post("/question/publish")
                            .header("Authorization", "Bearer " + testToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(publishDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String publishResponse = publishResult.getResponse().getContentAsString();
            Result<QuestionVO> publishResultObj = objectMapper.readValue(publishResponse,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, QuestionVO.class));
            Long questionId = publishResultObj.getData().getId();

            // 2. 查询问题详情
            mockMvc.perform(get("/question/" + questionId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.id").value(questionId));

            // 3. 更新问题
            QuestionUpdateDTO updateDTO = new QuestionUpdateDTO();
            updateDTO.setTitle("更新后的完整流程测试问题标题");

            mockMvc.perform(put("/question/" + questionId)
                            .header("Authorization", "Bearer " + testToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.title").value("更新后的完整流程测试问题标题"));

            // 4. 删除问题
            mockMvc.perform(delete("/question/" + questionId)
                            .header("Authorization", "Bearer " + testToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(true));

            // 5. 验证问题已删除
            BizQuestion question = questionMapper.selectById(questionId);
            assertNull(question);
        }
    }
}