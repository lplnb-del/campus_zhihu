package com.campus.zhihu.integration;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.zhihu.CampusZhihuApplication;
import com.campus.zhihu.common.Result;
import com.campus.zhihu.dto.AnswerPublishDTO;
import com.campus.zhihu.dto.CommentPublishDTO;
import com.campus.zhihu.dto.LoginDTO;
import com.campus.zhihu.dto.QuestionPublishDTO;
import com.campus.zhihu.dto.RegisterDTO;
import com.campus.zhihu.entity.*;
import com.campus.zhihu.mapper.*;
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
 * 交互模块集成测试
 * 测试点赞、收藏、评论等完整业务流程
 *
 * @author CampusZhihu Team
 */
@SpringBootTest(classes = CampusZhihuApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("交互模块集成测试")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class InteractionIntegrationTest {

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
    private LikeRecordMapper likeRecordMapper;
    
    @Autowired
    private CollectionMapper collectionMapper;
    
    @Autowired
    private CommentMapper commentMapper;
    
    @Autowired
    private TagMapper tagMapper;

    private static final int TARGET_TYPE_QUESTION = 1;
    private static final int TARGET_TYPE_ANSWER = 2;
    private static final int TARGET_TYPE_COMMENT = 3;

    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_MAJOR = "计算机科学与技术";
    private static final String TEST_GRADE = "2024级";

    private static final Random random = new Random();

    // 生成随机用户名（3-20个字符）
    private String generateRandomUsername() {
        String uuidPart = UUID.randomUUID().toString().substring(0, 8);
        return "i" + uuidPart;
    }

    // 生成随机邮箱
    private String generateRandomEmail() {
        String uuidPart = UUID.randomUUID().toString().substring(0, 8);
        return "i" + uuidPart + "@example.com";
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
        // 主类的setUp方法为空，具体的初始化由各个嵌套测试类自己负责
    }

    @Nested
    @DisplayName("点赞功能测试")
    class LikeFunctionTest {

        private Long userId;
        private String userToken;
        private Long questionId;
        private Long answerId;
        private Long tagId;

        @BeforeEach
        void setUp() throws Exception {
            // 生成随机测试数据
            String testUsername = generateRandomUsername();
            String testEmail = generateRandomEmail();
            String testStudentId = generateRandomStudentId();

            // 清理可能存在的旧数据
            cleanTestData(testUsername, testEmail, testStudentId);

            // 使用注册API创建用户
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
            Result<com.campus.zhihu.vo.UserVO> registerResultObj = objectMapper.readValue(registerResponse,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, com.campus.zhihu.vo.UserVO.class));
            userId = registerResultObj.getData().getId();

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
            userToken = resultObj.getData().getToken();

            // 初始化测试标签
            BizTag tag = new BizTag();
            tag.setName("交互测试标签_" + generateRandomUsername());
            tag.setDescription("用于交互测试的标签");
            tagMapper.insert(tag);
            tagId = tag.getId();

            // 发布测试问题
            QuestionPublishDTO questionPublishDTO = new QuestionPublishDTO();
            questionPublishDTO.setTitle("如何进行代码优化？");
            questionPublishDTO.setContent("请问有哪些代码优化的技巧？");
            questionPublishDTO.setTagIds(Arrays.asList(tagId));

            MvcResult questionResult = mockMvc.perform(post("/question/publish")
                            .header("Authorization", "Bearer " + userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(questionPublishDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String questionResponse = questionResult.getResponse().getContentAsString();
            Result<QuestionVO> questionResultObj = objectMapper.readValue(questionResponse,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, QuestionVO.class));
            questionId = questionResultObj.getData().getId();

            // 发布测试回答
            AnswerPublishDTO answerPublishDTO = new AnswerPublishDTO();
            answerPublishDTO.setQuestionId(questionId);
            answerPublishDTO.setContent("代码优化可以从算法优化、数据结构优化、内存优化等方面入手...");
            answerPublishDTO.setPublish(true);

            MvcResult answerResult = mockMvc.perform(post("/answer/publish")
                            .header("Authorization", "Bearer " + userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(answerPublishDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String answerResponse = answerResult.getResponse().getContentAsString();
            Result<com.campus.zhihu.vo.AnswerVO> answerResultObj = objectMapper.readValue(answerResponse,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, com.campus.zhihu.vo.AnswerVO.class));
            answerId = answerResultObj.getData().getId();
        }

        @Test
        @Order(1)
        @DisplayName("点赞问题 - 成功")
        void testLikeQuestionSuccess() throws Exception {
            // 获取点赞前的问题点赞数
            BizQuestion questionBefore = questionMapper.selectById(questionId);
            int likeCountBefore = questionBefore.getLikeCount();

            // 点赞问题
            mockMvc.perform(post("/like")
                            .param("targetType", String.valueOf(TARGET_TYPE_QUESTION))
                            .param("targetId", questionId.toString())
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value("点赞成功"))
                    .andExpect(jsonPath("$.data").value(true));

            // 验证点赞记录已创建
            BizLikeRecord likeRecord = likeRecordMapper.selectOne(
                    new LambdaQueryWrapper<BizLikeRecord>()
                            .eq(BizLikeRecord::getUserId, userId)
                            .eq(BizLikeRecord::getTargetType, TARGET_TYPE_QUESTION)
                            .eq(BizLikeRecord::getTargetId, questionId)
            );
            assertNotNull(likeRecord);

            // 验证问题点赞数已增加
            BizQuestion questionAfter = questionMapper.selectById(questionId);
            assertEquals(likeCountBefore + 1, questionAfter.getLikeCount());
        }

        @Test
        @Order(2)
        @DisplayName("点赞回答 - 成功")
        void testLikeAnswerSuccess() throws Exception {
            // 获取点赞前的回答点赞数
            BizAnswer answerBefore = answerMapper.selectById(answerId);
            int likeCountBefore = answerBefore.getLikeCount();

            // 点赞回答
            mockMvc.perform(post("/like")
                            .param("targetType", String.valueOf(TARGET_TYPE_ANSWER))
                            .param("targetId", answerId.toString())
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value("点赞成功"))
                    .andExpect(jsonPath("$.data").value(true));

            // 验证回答点赞数已增加
            BizAnswer answerAfter = answerMapper.selectById(answerId);
            assertEquals(likeCountBefore + 1, answerAfter.getLikeCount());
        }

        @Test
        @Order(3)
        @DisplayName("取消点赞 - 成功")
        void testUnlikeSuccess() throws Exception {
            // 先点赞
            mockMvc.perform(post("/like")
                            .param("targetType", String.valueOf(TARGET_TYPE_QUESTION))
                            .param("targetId", questionId.toString())
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk());

            // 获取取消点赞前的问题点赞数
            BizQuestion questionBefore = questionMapper.selectById(questionId);
            int likeCountBefore = questionBefore.getLikeCount();

            // 取消点赞
            mockMvc.perform(delete("/like")
                            .param("targetType", String.valueOf(TARGET_TYPE_QUESTION))
                            .param("targetId", questionId.toString())
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value("取消点赞成功"))
                    .andExpect(jsonPath("$.data").value(true));

            // 验证点赞记录已删除
            BizLikeRecord likeRecord = likeRecordMapper.selectOne(
                    new LambdaQueryWrapper<BizLikeRecord>()
                            .eq(BizLikeRecord::getUserId, userId)
                            .eq(BizLikeRecord::getTargetType, TARGET_TYPE_QUESTION)
                            .eq(BizLikeRecord::getTargetId, questionId)
            );
            assertNull(likeRecord);

            // 验证问题点赞数已减少
            BizQuestion questionAfter = questionMapper.selectById(questionId);
            assertEquals(likeCountBefore - 1, questionAfter.getLikeCount());
        }

        @Test
        @Order(4)
        @DisplayName("切换点赞状态 - 从未点赞到已点赞")
        void testToggleLikeToLiked() throws Exception {
            mockMvc.perform(post("/like/toggle")
                            .param("targetType", String.valueOf(TARGET_TYPE_QUESTION))
                            .param("targetId", questionId.toString())
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value("点赞成功"))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @Order(5)
        @DisplayName("切换点赞状态 - 从已点赞到未点赞")
        void testToggleLikeToUnliked() throws Exception {
            // 先点赞
            mockMvc.perform(post("/like")
                            .param("targetType", String.valueOf(TARGET_TYPE_QUESTION))
                            .param("targetId", questionId.toString())
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk());

            // 切换点赞状态
            mockMvc.perform(post("/like/toggle")
                            .param("targetType", String.valueOf(TARGET_TYPE_QUESTION))
                            .param("targetId", questionId.toString())
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value("取消点赞成功"))
                    .andExpect(jsonPath("$.data").value(false));
        }

        @Test
        @Order(6)
        @DisplayName("检查点赞状态 - 已点赞")
        void testCheckUserLikedTrue() throws Exception {
            // 先点赞
            mockMvc.perform(post("/like")
                            .param("targetType", String.valueOf(TARGET_TYPE_QUESTION))
                            .param("targetId", questionId.toString())
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk());

            // 检查点赞状态
            mockMvc.perform(get("/like/check")
                            .param("targetType", String.valueOf(TARGET_TYPE_QUESTION))
                            .param("targetId", questionId.toString())
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @Order(7)
        @DisplayName("检查点赞状态 - 未点赞")
        void testCheckUserLikedFalse() throws Exception {
            mockMvc.perform(get("/like/check")
                            .param("targetType", String.valueOf(TARGET_TYPE_QUESTION))
                            .param("targetId", questionId.toString())
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(false));
        }

        @Test
        @Order(8)
        @DisplayName("获取我的点赞记录")
        void testGetMyLikeRecords() throws Exception {
            // 先点赞
            mockMvc.perform(post("/like")
                            .param("targetType", String.valueOf(TARGET_TYPE_QUESTION))
                            .param("targetId", questionId.toString())
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk());

            // 获取点赞记录
            mockMvc.perform(get("/like/my")
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @Order(9)
        @DisplayName("点赞 - 未登录（失败）")
        void testLikeWithoutAuth() throws Exception {
            mockMvc.perform(post("/like")
                            .param("targetType", String.valueOf(TARGET_TYPE_QUESTION))
                            .param("targetId", questionId.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1001)); // 未登录
        }
    }

    @Nested
    @DisplayName("收藏功能测试")
    class CollectionFunctionTest {

        private Long userId;
        private String userToken;
        private Long questionId;
        private Long answerId;
        private Long tagId;

        @BeforeEach
        void setUp() throws Exception {
            // 生成随机测试数据
            String testUsername = generateRandomUsername();
            String testEmail = generateRandomEmail();
            String testStudentId = generateRandomStudentId();

            // 清理可能存在的旧数据
            cleanTestData(testUsername, testEmail, testStudentId);

            // 使用注册API创建用户
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
            Result<com.campus.zhihu.vo.UserVO> registerResultObj = objectMapper.readValue(registerResponse,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, com.campus.zhihu.vo.UserVO.class));
            userId = registerResultObj.getData().getId();

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
            userToken = resultObj.getData().getToken();

            // 初始化测试标签
            BizTag tag = new BizTag();
            tag.setName("交互测试标签_" + generateRandomUsername());
            tag.setDescription("用于交互测试的标签");
            tagMapper.insert(tag);
            tagId = tag.getId();

            // 发布测试问题
            QuestionPublishDTO questionPublishDTO = new QuestionPublishDTO();
            questionPublishDTO.setTitle("如何进行代码优化？");
            questionPublishDTO.setContent("请问有哪些代码优化的技巧？");
            questionPublishDTO.setTagIds(Arrays.asList(tagId));

            MvcResult questionResult = mockMvc.perform(post("/question/publish")
                            .header("Authorization", "Bearer " + userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(questionPublishDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String questionResponse = questionResult.getResponse().getContentAsString();
            Result<QuestionVO> questionResultObj = objectMapper.readValue(questionResponse,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, QuestionVO.class));
            questionId = questionResultObj.getData().getId();

            // 发布测试回答
            AnswerPublishDTO answerPublishDTO = new AnswerPublishDTO();
            answerPublishDTO.setQuestionId(questionId);
            answerPublishDTO.setContent("代码优化可以从算法优化、数据结构优化、内存优化等方面入手...");
            answerPublishDTO.setPublish(true);

            MvcResult answerResult = mockMvc.perform(post("/answer/publish")
                            .header("Authorization", "Bearer " + userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(answerPublishDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String answerResponse = answerResult.getResponse().getContentAsString();
            Result<com.campus.zhihu.vo.AnswerVO> answerResultObj = objectMapper.readValue(answerResponse,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, com.campus.zhihu.vo.AnswerVO.class));
            answerId = answerResultObj.getData().getId();
        }

        @Test
        @Order(10)
        @DisplayName("收藏问题 - 成功")
        void testCollectQuestionSuccess() throws Exception {
            // 获取收藏前的问题收藏数
            BizQuestion questionBefore = questionMapper.selectById(questionId);
            int collectionCountBefore = questionBefore.getCollectionCount();

            // 收藏问题
            mockMvc.perform(post("/collection")
                            .param("targetType", String.valueOf(TARGET_TYPE_QUESTION))
                            .param("targetId", questionId.toString())
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value("收藏成功"))
                    .andExpect(jsonPath("$.data").value(true));

            // 验证收藏记录已创建
            BizCollection collection = collectionMapper.selectOne(
                    new LambdaQueryWrapper<BizCollection>()
                            .eq(BizCollection::getUserId, userId)
                            .eq(BizCollection::getTargetType, TARGET_TYPE_QUESTION)
                            .eq(BizCollection::getTargetId, questionId)
            );
            assertNotNull(collection);

            // 验证问题收藏数已增加
            BizQuestion questionAfter = questionMapper.selectById(questionId);
            assertEquals(collectionCountBefore + 1, questionAfter.getCollectionCount());
        }

        @Test
        @Order(11)
        @DisplayName("收藏回答 - 成功")
        void testCollectAnswerSuccess() throws Exception {
            mockMvc.perform(post("/collection")
                            .param("targetType", String.valueOf(TARGET_TYPE_ANSWER))
                            .param("targetId", answerId.toString())
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value("收藏成功"))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @Order(12)
        @DisplayName("取消收藏 - 成功")
        void testUncollectSuccess() throws Exception {
            // 先收藏
            mockMvc.perform(post("/collection")
                            .param("targetType", String.valueOf(TARGET_TYPE_QUESTION))
                            .param("targetId", questionId.toString())
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk());

            // 获取取消收藏前的问题收藏数
            BizQuestion questionBefore = questionMapper.selectById(questionId);
            int collectionCountBefore = questionBefore.getCollectionCount();

            // 取消收藏
            mockMvc.perform(delete("/collection")
                            .param("targetType", String.valueOf(TARGET_TYPE_QUESTION))
                            .param("targetId", questionId.toString())
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value("取消收藏成功"))
                    .andExpect(jsonPath("$.data").value(true));

            // 验证收藏记录已删除
            BizCollection collection = collectionMapper.selectOne(
                    new LambdaQueryWrapper<BizCollection>()
                            .eq(BizCollection::getUserId, userId)
                            .eq(BizCollection::getTargetType, TARGET_TYPE_QUESTION)
                            .eq(BizCollection::getTargetId, questionId)
            );
            assertNull(collection);

            // 验证问题收藏数已减少
            BizQuestion questionAfter = questionMapper.selectById(questionId);
            assertEquals(collectionCountBefore - 1, questionAfter.getCollectionCount());
        }

        @Test
        @Order(13)
        @DisplayName("切换收藏状态 - 从未收藏到已收藏")
        void testToggleCollectToCollected() throws Exception {
            mockMvc.perform(post("/collection/toggle")
                            .param("targetType", String.valueOf(TARGET_TYPE_QUESTION))
                            .param("targetId", questionId.toString())
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value("收藏成功"))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @Order(14)
        @DisplayName("切换收藏状态 - 从已收藏到未收藏")
        void testToggleCollectToUncollected() throws Exception {
            // 先收藏
            mockMvc.perform(post("/collection")
                            .param("targetType", String.valueOf(TARGET_TYPE_QUESTION))
                            .param("targetId", questionId.toString())
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk());

            // 切换收藏状态
            mockMvc.perform(post("/collection/toggle")
                            .param("targetType", String.valueOf(TARGET_TYPE_QUESTION))
                            .param("targetId", questionId.toString())
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value("取消收藏成功"))
                    .andExpect(jsonPath("$.data").value(false));
        }

        @Test
        @Order(15)
        @DisplayName("检查收藏状态 - 已收藏")
        void testCheckUserCollectedTrue() throws Exception {
            // 先收藏
            mockMvc.perform(post("/collection")
                            .param("targetType", String.valueOf(TARGET_TYPE_QUESTION))
                            .param("targetId", questionId.toString())
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk());

            // 检查收藏状态
            mockMvc.perform(get("/collection/check")
                            .param("targetType", String.valueOf(TARGET_TYPE_QUESTION))
                            .param("targetId", questionId.toString())
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @Order(16)
        @DisplayName("检查收藏状态 - 未收藏")
        void testCheckUserCollectedFalse() throws Exception {
            mockMvc.perform(get("/collection/check")
                            .param("targetType", String.valueOf(TARGET_TYPE_QUESTION))
                            .param("targetId", questionId.toString())
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(false));
        }

        @Test
        @Order(17)
        @DisplayName("获取我的收藏列表")
        void testGetMyCollections() throws Exception {
            // 先收藏
            mockMvc.perform(post("/collection")
                            .param("targetType", String.valueOf(TARGET_TYPE_QUESTION))
                            .param("targetId", questionId.toString())
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk());

            // 获取收藏列表
            mockMvc.perform(get("/collection/my")
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.list").isArray());
        }

        @Test
        @Order(18)
        @DisplayName("收藏 - 未登录（失败）")
        void testCollectWithoutAuth() throws Exception {
            mockMvc.perform(post("/collection")
                            .param("targetType", String.valueOf(TARGET_TYPE_QUESTION))
                            .param("targetId", questionId.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1001)); // 未登录
        }
    }

    @Nested
    @DisplayName("评论功能测试")
    class CommentFunctionTest {

        private Long userId;
        private String userToken;
        private Long questionId;
        private Long answerId;
        private Long tagId;
        private Long commentId;

        @BeforeEach
        void setUp() throws Exception {
            // 生成随机测试数据
            String testUsername = generateRandomUsername();
            String testEmail = generateRandomEmail();
            String testStudentId = generateRandomStudentId();

            // 清理可能存在的旧数据
            cleanTestData(testUsername, testEmail, testStudentId);

            // 使用注册API创建用户
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
            Result<com.campus.zhihu.vo.UserVO> registerResultObj = objectMapper.readValue(registerResponse,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, com.campus.zhihu.vo.UserVO.class));
            userId = registerResultObj.getData().getId();

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
            userToken = resultObj.getData().getToken();

            // 初始化测试标签
            BizTag tag = new BizTag();
            tag.setName("交互测试标签_" + generateRandomUsername());
            tag.setDescription("用于交互测试的标签");
            tagMapper.insert(tag);
            tagId = tag.getId();

            // 发布测试问题
            QuestionPublishDTO questionPublishDTO = new QuestionPublishDTO();
            questionPublishDTO.setTitle("如何进行代码优化？");
            questionPublishDTO.setContent("请问有哪些代码优化的技巧？");
            questionPublishDTO.setTagIds(Arrays.asList(tagId));

            MvcResult questionResult = mockMvc.perform(post("/question/publish")
                            .header("Authorization", "Bearer " + userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(questionPublishDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String questionResponse = questionResult.getResponse().getContentAsString();
            Result<QuestionVO> questionResultObj = objectMapper.readValue(questionResponse,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, QuestionVO.class));
            questionId = questionResultObj.getData().getId();

            // 发布测试回答
            AnswerPublishDTO answerPublishDTO = new AnswerPublishDTO();
            answerPublishDTO.setQuestionId(questionId);
            answerPublishDTO.setContent("代码优化可以从算法优化、数据结构优化、内存优化等方面入手...");
            answerPublishDTO.setPublish(true);

            MvcResult answerResult = mockMvc.perform(post("/answer/publish")
                            .header("Authorization", "Bearer " + userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(answerPublishDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String answerResponse = answerResult.getResponse().getContentAsString();
            Result<com.campus.zhihu.vo.AnswerVO> answerResultObj = objectMapper.readValue(answerResponse,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, com.campus.zhihu.vo.AnswerVO.class));
            answerId = answerResultObj.getData().getId();
        }

        @Test
        @Order(19)
        @DisplayName("发布评论 - 成功")
        void testPublishCommentSuccess() throws Exception {
            CommentPublishDTO publishDTO = new CommentPublishDTO();
            publishDTO.setTargetType(TARGET_TYPE_ANSWER);
            publishDTO.setTargetId(answerId);
            publishDTO.setContent("这个回答很有帮助，感谢分享！");

            MvcResult result = mockMvc.perform(post("/comment/publish")
                            .header("Authorization", "Bearer " + userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(publishDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value("评论发布成功"))
                    .andExpect(jsonPath("$.data.content").value("这个回答很有帮助，感谢分享！"))
                    .andExpect(jsonPath("$.data.targetId").value(answerId))
                    .andExpect(jsonPath("$.data.userId").value(userId))
                    .andReturn();

            // 解析响应获取评论ID
            String response = result.getResponse().getContentAsString();
            Result<com.campus.zhihu.vo.CommentVO> resultObj = objectMapper.readValue(response,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, com.campus.zhihu.vo.CommentVO.class));
            commentId = resultObj.getData().getId();

            // 验证数据库中的数据
            BizComment comment = commentMapper.selectById(commentId);
            assertNotNull(comment);
            assertEquals("这个回答很有帮助，感谢分享！", comment.getContent());
            assertEquals(TARGET_TYPE_ANSWER, comment.getTargetType());
            assertEquals(answerId, comment.getTargetId());
            assertEquals(userId, comment.getUserId());

            // 验证回答评论数已增加
            BizAnswer answer = answerMapper.selectById(answerId);
            assertEquals(1, answer.getCommentCount());
        }

        @Test
        @Order(20)
        @DisplayName("发布回复评论 - 成功")
        void testPublishReplyCommentSuccess() throws Exception {
            // 先发布父评论
            CommentPublishDTO parentPublishDTO = new CommentPublishDTO();
            parentPublishDTO.setTargetType(TARGET_TYPE_ANSWER);
            parentPublishDTO.setTargetId(answerId);
            parentPublishDTO.setContent("父评论");

            MvcResult parentResult = mockMvc.perform(post("/comment/publish")
                            .header("Authorization", "Bearer " + userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(parentPublishDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String parentResponse = parentResult.getResponse().getContentAsString();
            Result<com.campus.zhihu.vo.CommentVO> parentResultObj = objectMapper.readValue(parentResponse,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, com.campus.zhihu.vo.CommentVO.class));
            Long parentId = parentResultObj.getData().getId();

            // 发布回复评论
            CommentPublishDTO replyPublishDTO = new CommentPublishDTO();
            replyPublishDTO.setTargetType(TARGET_TYPE_ANSWER);
            replyPublishDTO.setTargetId(answerId);
            replyPublishDTO.setContent("回复父评论");
            replyPublishDTO.setParentId(parentId);

            mockMvc.perform(post("/comment/publish")
                            .header("Authorization", "Bearer " + userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(replyPublishDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.parentId").value(parentId));
        }

        @Test
        @Order(21)
        @DisplayName("获取评论详情 - 成功")
        void testGetCommentByIdSuccess() throws Exception {
            // 先发布一个评论用于测试
            CommentPublishDTO publishDTO = new CommentPublishDTO();
            publishDTO.setTargetType(TARGET_TYPE_ANSWER);
            publishDTO.setTargetId(answerId);
            publishDTO.setContent("这个回答很有帮助，感谢分享！");

            MvcResult result = mockMvc.perform(post("/comment/publish")
                            .header("Authorization", "Bearer " + userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(publishDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andReturn();

            String response = result.getResponse().getContentAsString();
            Result<com.campus.zhihu.vo.CommentVO> resultObj = objectMapper.readValue(response,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, com.campus.zhihu.vo.CommentVO.class));
            Long testCommentId = resultObj.getData().getId();

            // 获取评论详情
            mockMvc.perform(get("/comment/" + testCommentId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.id").value(testCommentId))
                    .andExpect(jsonPath("$.data.content").value("这个回答很有帮助，感谢分享！"));
        }

        @Test
        @Order(22)
        @DisplayName("获取目标的评论列表")
        void testGetCommentsByTarget() throws Exception {
            mockMvc.perform(get("/comment/target")
                            .param("targetType", String.valueOf(TARGET_TYPE_ANSWER))
                            .param("targetId", answerId.toString())
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @Order(23)
        @DisplayName("获取顶级评论列表")
        void testGetTopLevelComments() throws Exception {
            mockMvc.perform(get("/comment/target/top-level")
                            .param("targetType", String.valueOf(TARGET_TYPE_ANSWER))
                            .param("targetId", answerId.toString())
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @Order(24)
        @DisplayName("删除评论 - 成功")
        void testDeleteCommentSuccess() throws Exception {
            // 先发布一个新评论用于删除测试
            CommentPublishDTO publishDTO = new CommentPublishDTO();
            publishDTO.setTargetType(TARGET_TYPE_ANSWER);
            publishDTO.setTargetId(answerId);
            publishDTO.setContent("待删除的评论");

            MvcResult result = mockMvc.perform(post("/comment/publish")
                            .header("Authorization", "Bearer " + userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(publishDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String response = result.getResponse().getContentAsString();
            Result<com.campus.zhihu.vo.CommentVO> resultObj = objectMapper.readValue(response,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, com.campus.zhihu.vo.CommentVO.class));
            Long commentToDelete = resultObj.getData().getId();

            // 删除评论
            mockMvc.perform(delete("/comment/" + commentToDelete)
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.msg").value("评论删除成功"))
                    .andExpect(jsonPath("$.data").value(true));

            // 验证评论已被逻辑删除
            BizComment comment = commentMapper.selectById(commentToDelete);
            assertNull(comment); // MyBatis-Plus逻辑删除会返回null
        }

        @Test
        @Order(25)
        @DisplayName("删除评论 - 无权限（非本人）")
        void testDeleteCommentNoPermission() throws Exception {
            // 先发布一个评论用于测试
            CommentPublishDTO publishDTO = new CommentPublishDTO();
            publishDTO.setTargetType(TARGET_TYPE_ANSWER);
            publishDTO.setTargetId(answerId);
            publishDTO.setContent("待删除的评论");

            MvcResult result = mockMvc.perform(post("/comment/publish")
                            .header("Authorization", "Bearer " + userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(publishDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andReturn();

            String response = result.getResponse().getContentAsString();
            Result<com.campus.zhihu.vo.CommentVO> resultObj = objectMapper.readValue(response,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, com.campus.zhihu.vo.CommentVO.class));
            Long testCommentId = resultObj.getData().getId();

            // 生成另一个用户的随机测试数据
            String anotherUsername = generateRandomUsername();
            String anotherEmail = generateRandomEmail();
            String anotherStudentId = generateRandomStudentId();

            // 清理可能存在的旧数据
            cleanTestData(anotherUsername, anotherEmail, anotherStudentId);

            // 使用注册API创建另一个用户
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
            LoginDTO anotherLoginDTO = new LoginDTO();
            anotherLoginDTO.setUsername(anotherUsername);
            anotherLoginDTO.setPassword(TEST_PASSWORD);

            MvcResult anotherLoginResult = mockMvc.perform(post("/user/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(anotherLoginDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String anotherLoginResponse = anotherLoginResult.getResponse().getContentAsString();
            Result<LoginVO> anotherLoginResultObj = objectMapper.readValue(anotherLoginResponse,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, LoginVO.class));
            String anotherToken = anotherLoginResultObj.getData().getToken();

            // 尝试删除评论
            mockMvc.perform(delete("/comment/" + testCommentId)
                            .header("Authorization", "Bearer " + anotherToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1004)); // 无权限
        }

        @Test
        @Order(26)
        @DisplayName("统计目标评论数量")
        void testCountCommentsByTarget() throws Exception {
            // 先发布一个评论用于测试
            CommentPublishDTO publishDTO = new CommentPublishDTO();
            publishDTO.setTargetType(TARGET_TYPE_ANSWER);
            publishDTO.setTargetId(answerId);
            publishDTO.setContent("统计测试评论");

            mockMvc.perform(post("/comment/publish")
                            .header("Authorization", "Bearer " + userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(publishDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));

            // 统计评论数量
            mockMvc.perform(get("/comment/count/target")
                            .param("targetType", String.valueOf(TARGET_TYPE_ANSWER))
                            .param("targetId", answerId.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value(1)); // 1条评论
        }

        @Test
        @Order(27)
        @DisplayName("发布评论 - 参数校验失败")
        void testPublishCommentValidationFailure() throws Exception {
            CommentPublishDTO publishDTO = new CommentPublishDTO();
            publishDTO.setTargetType(null); // 目标类型为空
            publishDTO.setTargetId(null); // 目标ID为空
            publishDTO.setContent(""); // 内容为空

            mockMvc.perform(post("/comment/publish")
                            .header("Authorization", "Bearer " + userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(publishDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(400)); // 参数错误
        }

        @Test
        @Order(28)
        @DisplayName("发布评论 - 未登录（失败）")
        void testPublishCommentWithoutAuth() throws Exception {
            CommentPublishDTO publishDTO = new CommentPublishDTO();
            publishDTO.setTargetType(TARGET_TYPE_ANSWER);
            publishDTO.setTargetId(answerId);
            publishDTO.setContent("测试内容");

            mockMvc.perform(post("/comment/publish")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(publishDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(1001)); // 未登录
        }
    }

    @Nested
    @DisplayName("完整业务流程测试")
    class CompleteWorkflowTest {

        private Long userId;
        private String userToken;
        private Long questionId;
        private Long answerId;
        private Long tagId;

        @BeforeEach
        void setUp() throws Exception {
            // 生成随机测试数据
            String testUsername = generateRandomUsername();
            String testEmail = generateRandomEmail();
            String testStudentId = generateRandomStudentId();

            // 清理可能存在的旧数据
            cleanTestData(testUsername, testEmail, testStudentId);

            // 使用注册API创建用户
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
            Result<com.campus.zhihu.vo.UserVO> registerResultObj = objectMapper.readValue(registerResponse,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, com.campus.zhihu.vo.UserVO.class));
            userId = registerResultObj.getData().getId();

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
            userToken = resultObj.getData().getToken();

            // 初始化测试标签
            BizTag tag = new BizTag();
            tag.setName("交互测试标签_" + generateRandomUsername());
            tag.setDescription("用于交互测试的标签");
            tagMapper.insert(tag);
            tagId = tag.getId();

            // 发布测试问题
            QuestionPublishDTO questionPublishDTO = new QuestionPublishDTO();
            questionPublishDTO.setTitle("如何进行代码优化？");
            questionPublishDTO.setContent("请问有哪些代码优化的技巧？");
            questionPublishDTO.setTagIds(Arrays.asList(tagId));

            MvcResult questionResult = mockMvc.perform(post("/question/publish")
                            .header("Authorization", "Bearer " + userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(questionPublishDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String questionResponse = questionResult.getResponse().getContentAsString();
            Result<QuestionVO> questionResultObj = objectMapper.readValue(questionResponse,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, QuestionVO.class));
            questionId = questionResultObj.getData().getId();

            // 发布测试回答
            AnswerPublishDTO answerPublishDTO = new AnswerPublishDTO();
            answerPublishDTO.setQuestionId(questionId);
            answerPublishDTO.setContent("代码优化可以从算法优化、数据结构优化、内存优化等方面入手...");
            answerPublishDTO.setPublish(true);

            MvcResult answerResult = mockMvc.perform(post("/answer/publish")
                            .header("Authorization", "Bearer " + userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(answerPublishDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String answerResponse = answerResult.getResponse().getContentAsString();
            Result<com.campus.zhihu.vo.AnswerVO> answerResultObj = objectMapper.readValue(answerResponse,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, com.campus.zhihu.vo.AnswerVO.class));
            answerId = answerResultObj.getData().getId();
        }

        @Test
        @Order(29)
        @DisplayName("点赞 -> 收藏 -> 评论 -> 取消点赞 -> 取消收藏 -> 删除评论")
        void testCompleteInteractionWorkflow() throws Exception {
            // 1. 点赞问题
            mockMvc.perform(post("/like")
                            .param("targetType", String.valueOf(TARGET_TYPE_QUESTION))
                            .param("targetId", questionId.toString())
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value(true));

            // 验证点赞状态
            mockMvc.perform(get("/like/check")
                            .param("targetType", String.valueOf(TARGET_TYPE_QUESTION))
                            .param("targetId", questionId.toString())
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value(true));

            // 2. 收藏问题
            mockMvc.perform(post("/collection")
                            .param("targetType", String.valueOf(TARGET_TYPE_QUESTION))
                            .param("targetId", questionId.toString())
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value(true));

            // 验证收藏状态
            mockMvc.perform(get("/collection/check")
                            .param("targetType", String.valueOf(TARGET_TYPE_QUESTION))
                            .param("targetId", questionId.toString())
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value(true));

            // 3. 发布评论
            CommentPublishDTO publishDTO = new CommentPublishDTO();
            publishDTO.setTargetType(TARGET_TYPE_ANSWER);
            publishDTO.setTargetId(answerId);
            publishDTO.setContent("完整流程测试评论");

            MvcResult result = mockMvc.perform(post("/comment/publish")
                            .header("Authorization", "Bearer " + userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(publishDTO)))
                    .andExpect(status().isOk())
                    .andReturn();

            String response = result.getResponse().getContentAsString();
            Result<com.campus.zhihu.vo.CommentVO> resultObj = objectMapper.readValue(response,
                    objectMapper.getTypeFactory().constructParametricType(Result.class, com.campus.zhihu.vo.CommentVO.class));
            Long testCommentId = resultObj.getData().getId();

            // 4. 取消点赞
            mockMvc.perform(delete("/like")
                            .param("targetType", String.valueOf(TARGET_TYPE_QUESTION))
                            .param("targetId", questionId.toString())
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value(true));

            // 验证点赞状态
            mockMvc.perform(get("/like/check")
                            .param("targetType", String.valueOf(TARGET_TYPE_QUESTION))
                            .param("targetId", questionId.toString())
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value(false));

            // 5. 取消收藏
            mockMvc.perform(delete("/collection")
                            .param("targetType", String.valueOf(TARGET_TYPE_QUESTION))
                            .param("targetId", questionId.toString())
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value(true));

            // 验证收藏状态
            mockMvc.perform(get("/collection/check")
                            .param("targetType", String.valueOf(TARGET_TYPE_QUESTION))
                            .param("targetId", questionId.toString())
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value(false));

            // 6. 删除评论
            mockMvc.perform(delete("/comment/" + testCommentId)
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value(true));

            // 验证评论已删除
            BizComment comment = commentMapper.selectById(testCommentId);
            assertNull(comment);
        }
    }
}