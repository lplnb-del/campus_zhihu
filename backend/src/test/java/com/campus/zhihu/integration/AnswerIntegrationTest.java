package com.campus.zhihu.integration;

import com.campus.zhihu.CampusZhihuApplication;
import com.campus.zhihu.common.Result;
import com.campus.zhihu.dto.AnswerPublishDTO;
import com.campus.zhihu.dto.AnswerUpdateDTO;
import com.campus.zhihu.dto.LoginDTO;
import com.campus.zhihu.dto.QuestionPublishDTO;
import com.campus.zhihu.dto.RegisterDTO;
import com.campus.zhihu.entity.BizAnswer;
import com.campus.zhihu.entity.BizQuestion;
import com.campus.zhihu.entity.BizTag;
import com.campus.zhihu.entity.SysUser;
import com.campus.zhihu.mapper.AnswerMapper;
import com.campus.zhihu.mapper.QuestionMapper;
import com.campus.zhihu.mapper.TagMapper;
import com.campus.zhihu.mapper.UserMapper;
import com.campus.zhihu.vo.AnswerVO;
import com.campus.zhihu.vo.LoginVO;
import com.campus.zhihu.vo.QuestionVO;
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
import java.util.Random;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 回答模块集成测试
 * 测试回答的发布、查询、更新、删除、采纳等完整业务流程
 *
 * @author CampusZhihu Team
 */
@SpringBootTest(classes = CampusZhihuApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("回答模块集成测试")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AnswerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private QuestionMapper questionMapper;
    
    @Autowired
    private AnswerMapper answerMapper;
    
    @Autowired
    private TagMapper tagMapper;

    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_MAJOR = "计算机科学与技术";
    private static final String TEST_GRADE = "2024级";

    private static final Random random = new Random();

    // 生成随机用户名（3-20个字符）
    private String generateRandomUsername() {
        String uuidPart = UUID.randomUUID().toString().substring(0, 8);
        return "a" + uuidPart;
    }

    // 生成随机邮箱
    private String generateRandomEmail() {
        String uuidPart = UUID.randomUUID().toString().substring(0, 8);
        return "a" + uuidPart + "@example.com";
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
        // 主类的setUp方法只负责初始化测试标签（如果需要）
        // 具体的用户和问题初始化由各个嵌套测试类自己负责
    }

    @Nested
    @DisplayName("回答发布流程测试")
    class AnswerPublishTest {

        private Long questionerId;
        private Long answererId;
        private String questionerToken;
        private String answererToken;
        private Long testQuestionId;
        private Long testAnswerId;
        private Long testTagId;

        @BeforeEach
        void setUp() throws Exception {
            // 生成随机测试数据
            String questionerUsername = generateRandomUsername();
            String questionerEmail = generateRandomEmail();
            String questionerStudentId = generateRandomStudentId();
            String answererUsername = generateRandomUsername();
            String answererEmail = generateRandomEmail();
            String answererStudentId = generateRandomStudentId();

            // 清理可能存在的旧数据
            cleanTestData(questionerUsername, questionerEmail, questionerStudentId);
            cleanTestData(answererUsername, answererEmail, answererStudentId);

            // 使用注册API创建提问者用户
            RegisterDTO questionerRegisterDTO = new RegisterDTO();
            questionerRegisterDTO.setUsername(questionerUsername);
            questionerRegisterDTO.setEmail(questionerEmail);
            questionerRegisterDTO.setPassword(TEST_PASSWORD);
            questionerRegisterDTO.setStudentId(questionerStudentId);
            questionerRegisterDTO.setMajor(TEST_MAJOR);
            questionerRegisterDTO.setGrade(TEST_GRADE);

            MvcResult questionerRegisterResult = mockMvc.perform(post("/user/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(questionerRegisterDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String questionerRegisterResponse = questionerRegisterResult.getResponse().getContentAsString();
            Result<com.campus.zhihu.vo.UserVO> questionerRegisterResultObj = objectMapper.readValue(questionerRegisterResponse,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, com.campus.zhihu.vo.UserVO.class));
            questionerId = questionerRegisterResultObj.getData().getId();

            // 提问者登录
            LoginDTO questionerLoginDTO = new LoginDTO();
            questionerLoginDTO.setUsername(questionerUsername);
            questionerLoginDTO.setPassword(TEST_PASSWORD);

            MvcResult questionerResult = mockMvc.perform(post("/user/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(questionerLoginDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String questionerResponse = questionerResult.getResponse().getContentAsString();
            Result<LoginVO> questionerResultObj = objectMapper.readValue(questionerResponse,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, LoginVO.class));
            questionerToken = questionerResultObj.getData().getToken();

            // 使用注册API创建回答者用户
            RegisterDTO answererRegisterDTO = new RegisterDTO();
            answererRegisterDTO.setUsername(answererUsername);
            answererRegisterDTO.setEmail(answererEmail);
            answererRegisterDTO.setPassword(TEST_PASSWORD);
            answererRegisterDTO.setStudentId(answererStudentId);
            answererRegisterDTO.setMajor(TEST_MAJOR);
            answererRegisterDTO.setGrade(TEST_GRADE);

            MvcResult answererRegisterResult = mockMvc.perform(post("/user/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(answererRegisterDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String answererRegisterResponse = answererRegisterResult.getResponse().getContentAsString();
            Result<com.campus.zhihu.vo.UserVO> answererRegisterResultObj = objectMapper.readValue(answererRegisterResponse,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, com.campus.zhihu.vo.UserVO.class));
            answererId = answererRegisterResultObj.getData().getId();

            // 回答者登录
            LoginDTO answererLoginDTO = new LoginDTO();
            answererLoginDTO.setUsername(answererUsername);
            answererLoginDTO.setPassword(TEST_PASSWORD);

            MvcResult answererResult = mockMvc.perform(post("/user/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(answererLoginDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String answererResponse = answererResult.getResponse().getContentAsString();
            Result<LoginVO> answererResultObj = objectMapper.readValue(answererResponse,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, LoginVO.class));
            answererToken = answererResultObj.getData().getToken();

            // 初始化测试标签
            BizTag tag = new BizTag();
            tag.setName("测试标签_" + generateRandomUsername());
            tag.setDescription("用于测试的标签");
            tagMapper.insert(tag);
            testTagId = tag.getId();

            // 提问者发布问题
            QuestionPublishDTO questionPublishDTO = new QuestionPublishDTO();
            questionPublishDTO.setTitle("如何学习Java？");
            questionPublishDTO.setContent("请问如何快速学习Java编程语言？");
            questionPublishDTO.setTagIds(Arrays.asList(testTagId));
            questionPublishDTO.setRewardPoints(50);

            MvcResult questionResult = mockMvc.perform(post("/question/publish")
                            .header("Authorization", "Bearer " + questionerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(questionPublishDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String questionResponse = questionResult.getResponse().getContentAsString();
            Result<QuestionVO> questionResultObj = objectMapper.readValue(questionResponse,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, QuestionVO.class));
            testQuestionId = questionResultObj.getData().getId();
        }

        @Test
        @Order(1)
        @DisplayName("发布回答 - 成功")
        void testPublishAnswerSuccess() throws Exception {
            AnswerPublishDTO publishDTO = new AnswerPublishDTO();
            publishDTO.setQuestionId(testQuestionId);
            publishDTO.setContent("建议从Java基础语法开始学习，然后掌握面向对象编程思想...");
            publishDTO.setPublish(true);

            MvcResult result = mockMvc.perform(post("/answer/publish")
                            .header("Authorization", "Bearer " + answererToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(publishDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value("回答发布成功"))
                    .andExpect(jsonPath("$.data.content").value("建议从Java基础语法开始学习，然后掌握面向对象编程思想..."))
                    .andExpect(jsonPath("$.data.questionId").value(testQuestionId))
                    .andExpect(jsonPath("$.data.userId").value(answererId))
                    .andExpect(jsonPath("$.data.status").value(1)) // 已发布
                    .andReturn();

            // 解析响应获取回答ID
            String response = result.getResponse().getContentAsString();
            Result<AnswerVO> resultObj = objectMapper.readValue(response,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, AnswerVO.class));
            testAnswerId = resultObj.getData().getId();

            // 验证数据库中的数据
            BizAnswer answer = answerMapper.selectById(testAnswerId);
            assertNotNull(answer);
            assertEquals("建议从Java基础语法开始学习，然后掌握面向对象编程思想...", answer.getContent());
            assertEquals(testQuestionId, answer.getQuestionId());
            assertEquals(answererId, answer.getUserId());
            assertEquals(1, answer.getStatus());

            // 验证问题回答数已增加
            BizQuestion question = questionMapper.selectById(testQuestionId);
            assertEquals(1, question.getAnswerCount());
        }

        @Test
        @Order(2)
        @DisplayName("发布回答 - 草稿")
        void testPublishAnswerAsDraft() throws Exception {
            AnswerPublishDTO publishDTO = new AnswerPublishDTO();
            publishDTO.setQuestionId(testQuestionId);
            publishDTO.setContent("这是草稿回答，长度至少需要10个字符才能通过参数校验。");
            publishDTO.setPublish(false);

            mockMvc.perform(post("/answer/publish")
                            .header("Authorization", "Bearer " + answererToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(publishDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.status").value(0)); // 草稿
        }

        @Test
        @Order(3)
        @DisplayName("发布回答 - 问题不存在")
        void testPublishAnswerQuestionNotFound() throws Exception {
            AnswerPublishDTO publishDTO = new AnswerPublishDTO();
            publishDTO.setQuestionId(999999L);
            publishDTO.setContent("这是测试内容，长度至少需要10个字符才能通过参数校验。");
            publishDTO.setPublish(true);

            mockMvc.perform(post("/answer/publish")
                            .header("Authorization", "Bearer " + answererToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(publishDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(3001)); // 问题不存在
        }

        @Test
        @Order(4)
        @DisplayName("发布回答 - 参数校验失败")
        void testPublishAnswerValidationFailure() throws Exception {
            AnswerPublishDTO publishDTO = new AnswerPublishDTO();
            publishDTO.setQuestionId(null); // 问题ID为空
            publishDTO.setContent("太短"); // 内容太短
            publishDTO.setPublish(true);

            mockMvc.perform(post("/answer/publish")
                            .header("Authorization", "Bearer " + answererToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(publishDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(400)); // 参数错误
        }

        @Test
        @Order(5)
        @DisplayName("发布回答 - 未登录（失败）")
        void testPublishAnswerWithoutAuth() throws Exception {
            AnswerPublishDTO publishDTO = new AnswerPublishDTO();
            publishDTO.setQuestionId(testQuestionId);
            publishDTO.setContent("测试内容");
            publishDTO.setPublish(true);

            mockMvc.perform(post("/answer/publish")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(publishDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1001)); // 未登录
        }
    }

    @Nested
    @DisplayName("回答查询流程测试")
    class AnswerQueryTest {

        private Long questionerId;
        private Long answererId;
        private String questionerToken;
        private String answererToken;
        private Long testQuestionId;
        private Long testAnswerId;
        private Long testTagId;

        @BeforeEach
        void setUp() throws Exception {
            // 生成随机测试数据
            String questionerUsername = generateRandomUsername();
            String questionerEmail = generateRandomEmail();
            String questionerStudentId = generateRandomStudentId();
            String answererUsername = generateRandomUsername();
            String answererEmail = generateRandomEmail();
            String answererStudentId = generateRandomStudentId();

            // 清理可能存在的旧数据
            cleanTestData(questionerUsername, questionerEmail, questionerStudentId);
            cleanTestData(answererUsername, answererEmail, answererStudentId);

            // 使用注册API创建提问者用户
            RegisterDTO questionerRegisterDTO = new RegisterDTO();
            questionerRegisterDTO.setUsername(questionerUsername);
            questionerRegisterDTO.setEmail(questionerEmail);
            questionerRegisterDTO.setPassword(TEST_PASSWORD);
            questionerRegisterDTO.setStudentId(questionerStudentId);
            questionerRegisterDTO.setMajor(TEST_MAJOR);
            questionerRegisterDTO.setGrade(TEST_GRADE);

            MvcResult questionerRegisterResult = mockMvc.perform(post("/user/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(questionerRegisterDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String questionerRegisterResponse = questionerRegisterResult.getResponse().getContentAsString();
            Result<com.campus.zhihu.vo.UserVO> questionerRegisterResultObj = objectMapper.readValue(questionerRegisterResponse,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, com.campus.zhihu.vo.UserVO.class));
            questionerId = questionerRegisterResultObj.getData().getId();

            // 提问者登录
            LoginDTO questionerLoginDTO = new LoginDTO();
            questionerLoginDTO.setUsername(questionerUsername);
            questionerLoginDTO.setPassword(TEST_PASSWORD);

            MvcResult questionerResult = mockMvc.perform(post("/user/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(questionerLoginDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String questionerResponse = questionerResult.getResponse().getContentAsString();
            Result<LoginVO> questionerResultObj = objectMapper.readValue(questionerResponse,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, LoginVO.class));
            questionerToken = questionerResultObj.getData().getToken();

            // 使用注册API创建回答者用户
            RegisterDTO answererRegisterDTO = new RegisterDTO();
            answererRegisterDTO.setUsername(answererUsername);
            answererRegisterDTO.setEmail(answererEmail);
            answererRegisterDTO.setPassword(TEST_PASSWORD);
            answererRegisterDTO.setStudentId(answererStudentId);
            answererRegisterDTO.setMajor(TEST_MAJOR);
            answererRegisterDTO.setGrade(TEST_GRADE);

            MvcResult answererRegisterResult = mockMvc.perform(post("/user/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(answererRegisterDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String answererRegisterResponse = answererRegisterResult.getResponse().getContentAsString();
            Result<com.campus.zhihu.vo.UserVO> answererRegisterResultObj = objectMapper.readValue(answererRegisterResponse,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, com.campus.zhihu.vo.UserVO.class));
            answererId = answererRegisterResultObj.getData().getId();

            // 回答者登录
            LoginDTO answererLoginDTO = new LoginDTO();
            answererLoginDTO.setUsername(answererUsername);
            answererLoginDTO.setPassword(TEST_PASSWORD);

            MvcResult answererResult = mockMvc.perform(post("/user/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(answererLoginDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String answererResponse = answererResult.getResponse().getContentAsString();
            Result<LoginVO> answererResultObj = objectMapper.readValue(answererResponse,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, LoginVO.class));
            answererToken = answererResultObj.getData().getToken();

            // 初始化测试标签
            BizTag tag = new BizTag();
            tag.setName("测试标签_" + generateRandomUsername());
            tag.setDescription("用于测试的标签");
            tagMapper.insert(tag);
            testTagId = tag.getId();

            // 提问者发布问题
            QuestionPublishDTO questionPublishDTO = new QuestionPublishDTO();
            questionPublishDTO.setTitle("如何学习Java？");
            questionPublishDTO.setContent("请问如何快速学习Java编程语言？");
            questionPublishDTO.setTagIds(Arrays.asList(testTagId));
            questionPublishDTO.setRewardPoints(50);

            MvcResult questionResult = mockMvc.perform(post("/question/publish")
                            .header("Authorization", "Bearer " + questionerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(questionPublishDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String questionResponse = questionResult.getResponse().getContentAsString();
            Result<QuestionVO> questionResultObj = objectMapper.readValue(questionResponse,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, QuestionVO.class));
            testQuestionId = questionResultObj.getData().getId();

            // 回答者发布回答
            AnswerPublishDTO answerPublishDTO = new AnswerPublishDTO();
            answerPublishDTO.setQuestionId(testQuestionId);
            answerPublishDTO.setContent("建议从Java基础语法开始学习，然后掌握面向对象编程思想...");
            answerPublishDTO.setPublish(true);

            MvcResult answerResult = mockMvc.perform(post("/answer/publish")
                            .header("Authorization", "Bearer " + answererToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(answerPublishDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String answerResponse = answerResult.getResponse().getContentAsString();
            Result<AnswerVO> answerResultObj = objectMapper.readValue(answerResponse,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, AnswerVO.class));
            testAnswerId = answerResultObj.getData().getId();
        }

        @Test
        @Order(6)
        @DisplayName("获取回答详情 - 成功")
        void testGetAnswerByIdSuccess() throws Exception {
            mockMvc.perform(get("/answer/" + testAnswerId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.id").value(testAnswerId))
                    .andExpect(jsonPath("$.data.questionId").value(testQuestionId))
                    .andExpect(jsonPath("$.data.userId").value(answererId));
        }

        @Test
        @Order(7)
        @DisplayName("获取问题的回答列表")
        void testGetAnswersByQuestion() throws Exception {
            mockMvc.perform(get("/answer/question/" + testQuestionId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.list").isArray());
        }

        @Test
        @Order(8)
        @DisplayName("获取用户的回答列表")
        void testGetUserAnswers() throws Exception {
            mockMvc.perform(get("/answer/user/" + answererId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @Order(9)
        @DisplayName("获取我的回答")
        void testGetMyAnswers() throws Exception {
            mockMvc.perform(get("/answer/my")
                            .header("Authorization", "Bearer " + answererToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.list").isArray());
        }

        @Test
        @Order(10)
        @DisplayName("检查用户是否已回答")
        void testCheckUserAnswered() throws Exception {
            mockMvc.perform(get("/answer/check-answered")
                            .param("questionId", testQuestionId.toString())
                            .header("Authorization", "Bearer " + answererToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(true)); // 已回答
        }

        @Test
        @Order(11)
        @DisplayName("统计用户回答数量")
        void testCountUserAnswers() throws Exception {
            mockMvc.perform(get("/answer/count/user/" + answererId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(1)); // 1个回答
        }

        @Test
        @Order(12)
        @DisplayName("统计问题回答数量")
        void testCountQuestionAnswers() throws Exception {
            mockMvc.perform(get("/answer/count/question/" + testQuestionId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(1)); // 1个回答
        }
    }

    @Nested
    @DisplayName("回答更新流程测试")
    class AnswerUpdateTest {

        private Long questionerId;
        private Long answererId;
        private String questionerToken;
        private String answererToken;
        private Long testQuestionId;
        private Long testAnswerId;
        private Long testTagId;

        @BeforeEach
        void setUp() throws Exception {
            // 生成随机测试数据
            String questionerUsername = generateRandomUsername();
            String questionerEmail = generateRandomEmail();
            String questionerStudentId = generateRandomStudentId();
            String answererUsername = generateRandomUsername();
            String answererEmail = generateRandomEmail();
            String answererStudentId = generateRandomStudentId();

            // 清理可能存在的旧数据
            cleanTestData(questionerUsername, questionerEmail, questionerStudentId);
            cleanTestData(answererUsername, answererEmail, answererStudentId);

            // 使用注册API创建提问者用户
            RegisterDTO questionerRegisterDTO = new RegisterDTO();
            questionerRegisterDTO.setUsername(questionerUsername);
            questionerRegisterDTO.setEmail(questionerEmail);
            questionerRegisterDTO.setPassword(TEST_PASSWORD);
            questionerRegisterDTO.setStudentId(questionerStudentId);
            questionerRegisterDTO.setMajor(TEST_MAJOR);
            questionerRegisterDTO.setGrade(TEST_GRADE);

            MvcResult questionerRegisterResult = mockMvc.perform(post("/user/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(questionerRegisterDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String questionerRegisterResponse = questionerRegisterResult.getResponse().getContentAsString();
            Result<com.campus.zhihu.vo.UserVO> questionerRegisterResultObj = objectMapper.readValue(questionerRegisterResponse,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, com.campus.zhihu.vo.UserVO.class));
            questionerId = questionerRegisterResultObj.getData().getId();

            // 提问者登录
            LoginDTO questionerLoginDTO = new LoginDTO();
            questionerLoginDTO.setUsername(questionerUsername);
            questionerLoginDTO.setPassword(TEST_PASSWORD);

            MvcResult questionerResult = mockMvc.perform(post("/user/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(questionerLoginDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String questionerResponse = questionerResult.getResponse().getContentAsString();
            Result<LoginVO> questionerResultObj = objectMapper.readValue(questionerResponse,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, LoginVO.class));
            questionerToken = questionerResultObj.getData().getToken();

            // 使用注册API创建回答者用户
            RegisterDTO answererRegisterDTO = new RegisterDTO();
            answererRegisterDTO.setUsername(answererUsername);
            answererRegisterDTO.setEmail(answererEmail);
            answererRegisterDTO.setPassword(TEST_PASSWORD);
            answererRegisterDTO.setStudentId(answererStudentId);
            answererRegisterDTO.setMajor(TEST_MAJOR);
            answererRegisterDTO.setGrade(TEST_GRADE);

            MvcResult answererRegisterResult = mockMvc.perform(post("/user/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(answererRegisterDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String answererRegisterResponse = answererRegisterResult.getResponse().getContentAsString();
            Result<com.campus.zhihu.vo.UserVO> answererRegisterResultObj = objectMapper.readValue(answererRegisterResponse,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, com.campus.zhihu.vo.UserVO.class));
            answererId = answererRegisterResultObj.getData().getId();

            // 回答者登录
            LoginDTO answererLoginDTO = new LoginDTO();
            answererLoginDTO.setUsername(answererUsername);
            answererLoginDTO.setPassword(TEST_PASSWORD);

            MvcResult answererResult = mockMvc.perform(post("/user/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(answererLoginDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String answererResponse = answererResult.getResponse().getContentAsString();
            Result<LoginVO> answererResultObj = objectMapper.readValue(answererResponse,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, LoginVO.class));
            answererToken = answererResultObj.getData().getToken();

            // 初始化测试标签
            BizTag tag = new BizTag();
            tag.setName("测试标签_" + generateRandomUsername());
            tag.setDescription("用于测试的标签");
            tagMapper.insert(tag);
            testTagId = tag.getId();

            // 提问者发布问题
            QuestionPublishDTO questionPublishDTO = new QuestionPublishDTO();
            questionPublishDTO.setTitle("如何学习Java？");
            questionPublishDTO.setContent("请问如何快速学习Java编程语言？");
            questionPublishDTO.setTagIds(Arrays.asList(testTagId));
            questionPublishDTO.setRewardPoints(50);

            MvcResult questionResult = mockMvc.perform(post("/question/publish")
                            .header("Authorization", "Bearer " + questionerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(questionPublishDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String questionResponse = questionResult.getResponse().getContentAsString();
            Result<QuestionVO> questionResultObj = objectMapper.readValue(questionResponse,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, QuestionVO.class));
            testQuestionId = questionResultObj.getData().getId();

            // 回答者发布回答
            AnswerPublishDTO answerPublishDTO = new AnswerPublishDTO();
            answerPublishDTO.setQuestionId(testQuestionId);
            answerPublishDTO.setContent("建议从Java基础语法开始学习，然后掌握面向对象编程思想...");
            answerPublishDTO.setPublish(true);

            MvcResult answerResult = mockMvc.perform(post("/answer/publish")
                            .header("Authorization", "Bearer " + answererToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(answerPublishDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String answerResponse = answerResult.getResponse().getContentAsString();
            Result<AnswerVO> answerResultObj = objectMapper.readValue(answerResponse,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, AnswerVO.class));
            testAnswerId = answerResultObj.getData().getId();
        }

        @Test
        @Order(13)
        @DisplayName("更新回答 - 成功")
        void testUpdateAnswerSuccess() throws Exception {
            AnswerUpdateDTO updateDTO = new AnswerUpdateDTO();
            updateDTO.setContent("更新后的回答内容：建议从Java基础语法开始学习，然后掌握面向对象编程思想，最后通过项目实践巩固知识。");

            mockMvc.perform(put("/answer/" + testAnswerId)
                            .header("Authorization", "Bearer " + answererToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value("回答更新成功"))
                    .andExpect(jsonPath("$.data.content").value("更新后的回答内容：建议从Java基础语法开始学习，然后掌握面向对象编程思想，最后通过项目实践巩固知识。"));

            // 验证数据库中的数据已更新
            BizAnswer answer = answerMapper.selectById(testAnswerId);
            assertEquals("更新后的回答内容：建议从Java基础语法开始学习，然后掌握面向对象编程思想，最后通过项目实践巩固知识。", answer.getContent());
        }

        @Test
        @Order(14)
        @DisplayName("更新回答 - 无权限（非本人）")
        void testUpdateAnswerNoPermission() throws Exception {
            AnswerUpdateDTO updateDTO = new AnswerUpdateDTO();
            updateDTO.setContent("这是非法更新的内容，长度至少需要10个字符才能通过参数校验。");

            mockMvc.perform(put("/answer/" + testAnswerId)
                            .header("Authorization", "Bearer " + questionerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1004)); // 无权限
        }

        @Test
        @Order(15)
        @DisplayName("更新回答 - 回答不存在")
        void testUpdateAnswerNotFound() throws Exception {
            AnswerUpdateDTO updateDTO = new AnswerUpdateDTO();
            updateDTO.setContent("这是测试更新的内容，长度至少需要10个字符才能通过参数校验。");

            mockMvc.perform(put("/answer/999999")
                            .header("Authorization", "Bearer " + answererToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(4001)); // 回答不存在
        }
    }

    @Nested
    @DisplayName("回答删除流程测试")
    class AnswerDeleteTest {

        private Long questionerId;
        private Long answererId;
        private String questionerToken;
        private String answererToken;
        private Long testQuestionId;
        private Long testAnswerId;
        private Long testTagId;

        @BeforeEach
        void setUp() throws Exception {
            // 生成随机测试数据
            String questionerUsername = generateRandomUsername();
            String questionerEmail = generateRandomEmail();
            String questionerStudentId = generateRandomStudentId();
            String answererUsername = generateRandomUsername();
            String answererEmail = generateRandomEmail();
            String answererStudentId = generateRandomStudentId();

            // 清理可能存在的旧数据
            cleanTestData(questionerUsername, questionerEmail, questionerStudentId);
            cleanTestData(answererUsername, answererEmail, answererStudentId);

            // 使用注册API创建提问者用户
            RegisterDTO questionerRegisterDTO = new RegisterDTO();
            questionerRegisterDTO.setUsername(questionerUsername);
            questionerRegisterDTO.setEmail(questionerEmail);
            questionerRegisterDTO.setPassword(TEST_PASSWORD);
            questionerRegisterDTO.setStudentId(questionerStudentId);
            questionerRegisterDTO.setMajor(TEST_MAJOR);
            questionerRegisterDTO.setGrade(TEST_GRADE);

            MvcResult questionerRegisterResult = mockMvc.perform(post("/user/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(questionerRegisterDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String questionerRegisterResponse = questionerRegisterResult.getResponse().getContentAsString();
            Result<com.campus.zhihu.vo.UserVO> questionerRegisterResultObj = objectMapper.readValue(questionerRegisterResponse,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, com.campus.zhihu.vo.UserVO.class));
            questionerId = questionerRegisterResultObj.getData().getId();

            // 提问者登录
            LoginDTO questionerLoginDTO = new LoginDTO();
            questionerLoginDTO.setUsername(questionerUsername);
            questionerLoginDTO.setPassword(TEST_PASSWORD);

            MvcResult questionerResult = mockMvc.perform(post("/user/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(questionerLoginDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String questionerResponse = questionerResult.getResponse().getContentAsString();
            Result<LoginVO> questionerResultObj = objectMapper.readValue(questionerResponse,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, LoginVO.class));
            questionerToken = questionerResultObj.getData().getToken();

            // 使用注册API创建回答者用户
            RegisterDTO answererRegisterDTO = new RegisterDTO();
            answererRegisterDTO.setUsername(answererUsername);
            answererRegisterDTO.setEmail(answererEmail);
            answererRegisterDTO.setPassword(TEST_PASSWORD);
            answererRegisterDTO.setStudentId(answererStudentId);
            answererRegisterDTO.setMajor(TEST_MAJOR);
            answererRegisterDTO.setGrade(TEST_GRADE);

            MvcResult answererRegisterResult = mockMvc.perform(post("/user/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(answererRegisterDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String answererRegisterResponse = answererRegisterResult.getResponse().getContentAsString();
            Result<com.campus.zhihu.vo.UserVO> answererRegisterResultObj = objectMapper.readValue(answererRegisterResponse,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, com.campus.zhihu.vo.UserVO.class));
            answererId = answererRegisterResultObj.getData().getId();

            // 回答者登录
            LoginDTO answererLoginDTO = new LoginDTO();
            answererLoginDTO.setUsername(answererUsername);
            answererLoginDTO.setPassword(TEST_PASSWORD);

            MvcResult answererResult = mockMvc.perform(post("/user/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(answererLoginDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String answererResponse = answererResult.getResponse().getContentAsString();
            Result<LoginVO> answererResultObj = objectMapper.readValue(answererResponse,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, LoginVO.class));
            answererToken = answererResultObj.getData().getToken();

            // 初始化测试标签
            BizTag tag = new BizTag();
            tag.setName("测试标签_" + generateRandomUsername());
            tag.setDescription("用于测试的标签");
            tagMapper.insert(tag);
            testTagId = tag.getId();

            // 提问者发布问题
            QuestionPublishDTO questionPublishDTO = new QuestionPublishDTO();
            questionPublishDTO.setTitle("如何学习Java？");
            questionPublishDTO.setContent("请问如何快速学习Java编程语言？");
            questionPublishDTO.setTagIds(Arrays.asList(testTagId));
            questionPublishDTO.setRewardPoints(50);

            MvcResult questionResult = mockMvc.perform(post("/question/publish")
                            .header("Authorization", "Bearer " + questionerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(questionPublishDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String questionResponse = questionResult.getResponse().getContentAsString();
            Result<QuestionVO> questionResultObj = objectMapper.readValue(questionResponse,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, QuestionVO.class));
            testQuestionId = questionResultObj.getData().getId();

            // 回答者发布回答
            AnswerPublishDTO answerPublishDTO = new AnswerPublishDTO();
            answerPublishDTO.setQuestionId(testQuestionId);
            answerPublishDTO.setContent("建议从Java基础语法开始学习，然后掌握面向对象编程思想...");
            answerPublishDTO.setPublish(true);

            MvcResult answerResult = mockMvc.perform(post("/answer/publish")
                            .header("Authorization", "Bearer " + answererToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(answerPublishDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String answerResponse = answerResult.getResponse().getContentAsString();
            Result<AnswerVO> answerResultObj = objectMapper.readValue(answerResponse,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, AnswerVO.class));
            testAnswerId = answerResultObj.getData().getId();
        }

        @Test
        @Order(16)
        @DisplayName("删除回答 - 成功")
        void testDeleteAnswerSuccess() throws Exception {
            // 先创建一个新回答用于删除测试
            AnswerPublishDTO publishDTO = new AnswerPublishDTO();
            publishDTO.setQuestionId(testQuestionId);
            publishDTO.setContent("待删除的回答，长度至少需要10个字符才能通过参数校验。");
            publishDTO.setPublish(true);

            MvcResult result = mockMvc.perform(post("/answer/publish")
                            .header("Authorization", "Bearer " + answererToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(publishDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String response = result.getResponse().getContentAsString();
            Result<AnswerVO> resultObj = objectMapper.readValue(response,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, AnswerVO.class));
            Long answerToDelete = resultObj.getData().getId();

            // 删除回答
            mockMvc.perform(delete("/answer/" + answerToDelete)
                            .header("Authorization", "Bearer " + answererToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value("回答删除成功"))
                    .andExpect(jsonPath("$.data").value(true));

            // 验证回答已被逻辑删除
            BizAnswer answer = answerMapper.selectById(answerToDelete);
            assertNull(answer); // MyBatis-Plus逻辑删除会返回null
        }

        @Test
        @Order(17)
        @DisplayName("删除回答 - 无权限（非本人）")
        void testDeleteAnswerNoPermission() throws Exception {
            mockMvc.perform(delete("/answer/" + testAnswerId)
                            .header("Authorization", "Bearer " + questionerToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1004)); // 无权限
        }
    }

    @Nested
    @DisplayName("回答采纳流程测试")
    class AnswerAcceptTest {

        private Long questionerId;
        private Long answererId;
        private String questionerToken;
        private String answererToken;
        private Long testQuestionId;
        private Long testAnswerId;
        private Long testTagId;

        @BeforeEach
        void setUp() throws Exception {
            // 生成随机测试数据
            String questionerUsername = generateRandomUsername();
            String questionerEmail = generateRandomEmail();
            String questionerStudentId = generateRandomStudentId();
            String answererUsername = generateRandomUsername();
            String answererEmail = generateRandomEmail();
            String answererStudentId = generateRandomStudentId();

            // 清理可能存在的旧数据
            cleanTestData(questionerUsername, questionerEmail, questionerStudentId);
            cleanTestData(answererUsername, answererEmail, answererStudentId);

            // 使用注册API创建提问者用户
            RegisterDTO questionerRegisterDTO = new RegisterDTO();
            questionerRegisterDTO.setUsername(questionerUsername);
            questionerRegisterDTO.setEmail(questionerEmail);
            questionerRegisterDTO.setPassword(TEST_PASSWORD);
            questionerRegisterDTO.setStudentId(questionerStudentId);
            questionerRegisterDTO.setMajor(TEST_MAJOR);
            questionerRegisterDTO.setGrade(TEST_GRADE);

            MvcResult questionerRegisterResult = mockMvc.perform(post("/user/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(questionerRegisterDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String questionerRegisterResponse = questionerRegisterResult.getResponse().getContentAsString();
            Result<com.campus.zhihu.vo.UserVO> questionerRegisterResultObj = objectMapper.readValue(questionerRegisterResponse,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, com.campus.zhihu.vo.UserVO.class));
            questionerId = questionerRegisterResultObj.getData().getId();

            // 提问者登录
            LoginDTO questionerLoginDTO = new LoginDTO();
            questionerLoginDTO.setUsername(questionerUsername);
            questionerLoginDTO.setPassword(TEST_PASSWORD);

            MvcResult questionerResult = mockMvc.perform(post("/user/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(questionerLoginDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String questionerResponse = questionerResult.getResponse().getContentAsString();
            Result<LoginVO> questionerResultObj = objectMapper.readValue(questionerResponse,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, LoginVO.class));
            questionerToken = questionerResultObj.getData().getToken();

            // 使用注册API创建回答者用户
            RegisterDTO answererRegisterDTO = new RegisterDTO();
            answererRegisterDTO.setUsername(answererUsername);
            answererRegisterDTO.setEmail(answererEmail);
            answererRegisterDTO.setPassword(TEST_PASSWORD);
            answererRegisterDTO.setStudentId(answererStudentId);
            answererRegisterDTO.setMajor(TEST_MAJOR);
            answererRegisterDTO.setGrade(TEST_GRADE);

            MvcResult answererRegisterResult = mockMvc.perform(post("/user/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(answererRegisterDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String answererRegisterResponse = answererRegisterResult.getResponse().getContentAsString();
            Result<com.campus.zhihu.vo.UserVO> answererRegisterResultObj = objectMapper.readValue(answererRegisterResponse,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, com.campus.zhihu.vo.UserVO.class));
            answererId = answererRegisterResultObj.getData().getId();

            // 回答者登录
            LoginDTO answererLoginDTO = new LoginDTO();
            answererLoginDTO.setUsername(answererUsername);
            answererLoginDTO.setPassword(TEST_PASSWORD);

            MvcResult answererResult = mockMvc.perform(post("/user/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(answererLoginDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String answererResponse = answererResult.getResponse().getContentAsString();
            Result<LoginVO> answererResultObj = objectMapper.readValue(answererResponse,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, LoginVO.class));
            answererToken = answererResultObj.getData().getToken();

            // 初始化测试标签
            BizTag tag = new BizTag();
            tag.setName("测试标签_" + generateRandomUsername());
            tag.setDescription("用于测试的标签");
            tagMapper.insert(tag);
            testTagId = tag.getId();

            // 提问者发布问题
            QuestionPublishDTO questionPublishDTO = new QuestionPublishDTO();
            questionPublishDTO.setTitle("如何学习Java？");
            questionPublishDTO.setContent("请问如何快速学习Java编程语言？");
            questionPublishDTO.setTagIds(Arrays.asList(testTagId));
            questionPublishDTO.setRewardPoints(50);

            MvcResult questionResult = mockMvc.perform(post("/question/publish")
                            .header("Authorization", "Bearer " + questionerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(questionPublishDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String questionResponse = questionResult.getResponse().getContentAsString();
            Result<QuestionVO> questionResultObj = objectMapper.readValue(questionResponse,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, QuestionVO.class));
            testQuestionId = questionResultObj.getData().getId();

            // 回答者发布回答
            AnswerPublishDTO answerPublishDTO = new AnswerPublishDTO();
            answerPublishDTO.setQuestionId(testQuestionId);
            answerPublishDTO.setContent("建议从Java基础语法开始学习，然后掌握面向对象编程思想...");
            answerPublishDTO.setPublish(true);

            MvcResult answerResult = mockMvc.perform(post("/answer/publish")
                            .header("Authorization", "Bearer " + answererToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(answerPublishDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String answerResponse = answerResult.getResponse().getContentAsString();
            Result<AnswerVO> answerResultObj = objectMapper.readValue(answerResponse,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, AnswerVO.class));
            testAnswerId = answerResultObj.getData().getId();
        }

        @Test
        @Order(18)
        @DisplayName("采纳回答 - 成功")
        void testAcceptAnswerSuccess() throws Exception {
            // 获取采纳前的积分
            SysUser questionerBefore = userMapper.selectById(questionerId);
            SysUser answererBefore = userMapper.selectById(answererId);
            int questionerPointsBefore = questionerBefore.getPoints();
            int answererPointsBefore = answererBefore.getPoints();

            // 采纳回答
            mockMvc.perform(put("/answer/" + testAnswerId + "/accept")
                            .param("questionId", testQuestionId.toString())
                            .header("Authorization", "Bearer " + questionerToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value("回答已采纳"))
                    .andExpect(jsonPath("$.data").value(true));

            // 验证回答的采纳状态
            BizAnswer answer = answerMapper.selectById(testAnswerId);
            assertEquals(1, answer.getIsAccepted()); // 已采纳

            // 验证问题的采纳回答ID
            BizQuestion question = questionMapper.selectById(testQuestionId);
            assertEquals(testAnswerId, question.getAcceptedAnswerId());
            assertEquals(1, question.getIsSolved()); // 问题已解决

            // 验证积分变化
            SysUser questionerAfter = userMapper.selectById(questionerId);
            SysUser answererAfter = userMapper.selectById(answererId);
            
            assertNotNull(questionerAfter, "提问者用户查询失败");
            assertNotNull(answererAfter, "回答者用户查询失败");
            assertNotNull(questionerAfter.getPoints(), "提问者积分字段为null");
            assertNotNull(answererAfter.getPoints(), "回答者积分字段为null");
            
            int questionerPointsAfter = questionerAfter.getPoints();
            int answererPointsAfter = answererAfter.getPoints();

            // 提问者扣除悬赏分（50）
            assertEquals(questionerPointsBefore - 50, questionerPointsAfter);
            // 回答者获得悬赏分（50）+ 额外奖励（10）= 60
            assertEquals(answererPointsBefore + 60, answererPointsAfter);
        }

        @Test
        @Order(19)
        @DisplayName("采纳回答 - 无权限（非提问者）")
        void testAcceptAnswerNoPermission() throws Exception {
            mockMvc.perform(put("/answer/" + testAnswerId + "/accept")
                            .param("questionId", testQuestionId.toString())
                            .header("Authorization", "Bearer " + answererToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1004)); // 无权限
        }

        @Test
        @Order(20)
        @DisplayName("取消采纳回答 - 成功")
        void testCancelAcceptAnswerSuccess() throws Exception {
            // 先采纳回答
            mockMvc.perform(put("/answer/" + testAnswerId + "/accept")
                            .param("questionId", testQuestionId.toString())
                            .header("Authorization", "Bearer " + questionerToken))
                    .andExpect(status().isOk());

            // 取消采纳
            mockMvc.perform(put("/answer/" + testAnswerId + "/cancel-accept")
                            .param("questionId", testQuestionId.toString())
                            .header("Authorization", "Bearer " + questionerToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value("已取消采纳"))
                    .andExpect(jsonPath("$.data").value(true));

            // 验证回答的采纳状态
            BizAnswer answer = answerMapper.selectById(testAnswerId);
            assertEquals(0, answer.getIsAccepted()); // 未采纳

            // 验证问题的采纳回答ID
            BizQuestion question = questionMapper.selectById(testQuestionId);
            assertNull(question.getAcceptedAnswerId());
        }

        @Test
        @Order(21)
        @DisplayName("获取问题的被采纳回答")
        void testGetAcceptedAnswer() throws Exception {
            // 先采纳回答
            mockMvc.perform(put("/answer/" + testAnswerId + "/accept")
                            .param("questionId", testQuestionId.toString())
                            .header("Authorization", "Bearer " + questionerToken))
                    .andExpect(status().isOk());

            // 获取被采纳的回答
            mockMvc.perform(get("/answer/question/" + testQuestionId + "/accepted"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.id").value(testAnswerId))
                    .andExpect(jsonPath("$.data.isAccepted").value(1));
        }
    }

    @Nested
    @DisplayName("完整业务流程测试")
    class CompleteWorkflowTest {

        private Long questionerId;
        private Long answererId;
        private String questionerToken;
        private String answererToken;
        private Long testQuestionId;
        private Long testAnswerId;
        private Long testTagId;

        @BeforeEach
        void setUp() throws Exception {
            // 生成随机测试数据
            String questionerUsername = generateRandomUsername();
            String questionerEmail = generateRandomEmail();
            String questionerStudentId = generateRandomStudentId();
            String answererUsername = generateRandomUsername();
            String answererEmail = generateRandomEmail();
            String answererStudentId = generateRandomStudentId();

            // 清理可能存在的旧数据
            cleanTestData(questionerUsername, questionerEmail, questionerStudentId);
            cleanTestData(answererUsername, answererEmail, answererStudentId);

            // 使用注册API创建提问者用户
            RegisterDTO questionerRegisterDTO = new RegisterDTO();
            questionerRegisterDTO.setUsername(questionerUsername);
            questionerRegisterDTO.setEmail(questionerEmail);
            questionerRegisterDTO.setPassword(TEST_PASSWORD);
            questionerRegisterDTO.setStudentId(questionerStudentId);
            questionerRegisterDTO.setMajor(TEST_MAJOR);
            questionerRegisterDTO.setGrade(TEST_GRADE);

            MvcResult questionerRegisterResult = mockMvc.perform(post("/user/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(questionerRegisterDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String questionerRegisterResponse = questionerRegisterResult.getResponse().getContentAsString();
            Result<com.campus.zhihu.vo.UserVO> questionerRegisterResultObj = objectMapper.readValue(questionerRegisterResponse,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, com.campus.zhihu.vo.UserVO.class));
            questionerId = questionerRegisterResultObj.getData().getId();

            // 提问者登录
            LoginDTO questionerLoginDTO = new LoginDTO();
            questionerLoginDTO.setUsername(questionerUsername);
            questionerLoginDTO.setPassword(TEST_PASSWORD);

            MvcResult questionerResult = mockMvc.perform(post("/user/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(questionerLoginDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String questionerResponse = questionerResult.getResponse().getContentAsString();
            Result<LoginVO> questionerResultObj = objectMapper.readValue(questionerResponse,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, LoginVO.class));
            questionerToken = questionerResultObj.getData().getToken();

            // 使用注册API创建回答者用户
            RegisterDTO answererRegisterDTO = new RegisterDTO();
            answererRegisterDTO.setUsername(answererUsername);
            answererRegisterDTO.setEmail(answererEmail);
            answererRegisterDTO.setPassword(TEST_PASSWORD);
            answererRegisterDTO.setStudentId(answererStudentId);
            answererRegisterDTO.setMajor(TEST_MAJOR);
            answererRegisterDTO.setGrade(TEST_GRADE);

            MvcResult answererRegisterResult = mockMvc.perform(post("/user/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(answererRegisterDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String answererRegisterResponse = answererRegisterResult.getResponse().getContentAsString();
            Result<com.campus.zhihu.vo.UserVO> answererRegisterResultObj = objectMapper.readValue(answererRegisterResponse,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, com.campus.zhihu.vo.UserVO.class));
            answererId = answererRegisterResultObj.getData().getId();

            // 回答者登录
            LoginDTO answererLoginDTO = new LoginDTO();
            answererLoginDTO.setUsername(answererUsername);
            answererLoginDTO.setPassword(TEST_PASSWORD);

            MvcResult answererResult = mockMvc.perform(post("/user/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(answererLoginDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String answererResponse = answererResult.getResponse().getContentAsString();
            Result<LoginVO> answererResultObj = objectMapper.readValue(answererResponse,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, LoginVO.class));
            answererToken = answererResultObj.getData().getToken();

            // 初始化测试标签
            BizTag tag = new BizTag();
            tag.setName("测试标签_" + generateRandomUsername());
            tag.setDescription("用于测试的标签");
            tagMapper.insert(tag);
            testTagId = tag.getId();

            // 提问者发布问题
            QuestionPublishDTO questionPublishDTO = new QuestionPublishDTO();
            questionPublishDTO.setTitle("如何学习Java？");
            questionPublishDTO.setContent("请问如何快速学习Java编程语言？");
            questionPublishDTO.setTagIds(Arrays.asList(testTagId));
            questionPublishDTO.setRewardPoints(50);

            MvcResult questionResult = mockMvc.perform(post("/question/publish")
                            .header("Authorization", "Bearer " + questionerToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(questionPublishDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String questionResponse = questionResult.getResponse().getContentAsString();
            Result<QuestionVO> questionResultObj = objectMapper.readValue(questionResponse,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, QuestionVO.class));
            testQuestionId = questionResultObj.getData().getId();
        }

        @Test
        @Order(22)
        @DisplayName("发布回答 -> 查询详情 -> 更新回答 -> 采纳回答")
        void testCompleteAnswerWorkflow() throws Exception {
            // 1. 发布回答
            AnswerPublishDTO publishDTO = new AnswerPublishDTO();
            publishDTO.setQuestionId(testQuestionId);
            publishDTO.setContent("完整流程测试回答，长度至少需要10个字符才能通过参数校验。");
            publishDTO.setPublish(true);

            MvcResult publishResult = mockMvc.perform(post("/answer/publish")
                            .header("Authorization", "Bearer " + answererToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(publishDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String publishResponse = publishResult.getResponse().getContentAsString();
            Result<AnswerVO> publishResultObj = objectMapper.readValue(publishResponse,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, AnswerVO.class));
            Long answerId = publishResultObj.getData().getId();

            // 2. 查询回答详情
            mockMvc.perform(get("/answer/" + answerId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.id").value(answerId));

            // 3. 更新回答
            AnswerUpdateDTO updateDTO = new AnswerUpdateDTO();
            updateDTO.setContent("更新后的完整流程测试回答");

            mockMvc.perform(put("/answer/" + answerId)
                            .header("Authorization", "Bearer " + answererToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.content").value("更新后的完整流程测试回答"));

            // 4. 采纳回答
            mockMvc.perform(put("/answer/" + answerId + "/accept")
                            .param("questionId", testQuestionId.toString())
                            .header("Authorization", "Bearer " + questionerToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(true));

            // 5. 验证回答已被采纳
            BizAnswer answer = answerMapper.selectById(answerId);
            assertEquals(1, answer.getIsAccepted());
        }
    }
}