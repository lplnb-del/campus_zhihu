package com.campus.zhihu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.zhihu.common.BusinessException;
import com.campus.zhihu.common.ResultCode;
import com.campus.zhihu.dto.LoginDTO;
import com.campus.zhihu.dto.RefreshTokenDTO;
import com.campus.zhihu.dto.RegisterDTO;
import com.campus.zhihu.dto.UserUpdateDTO;
import com.campus.zhihu.dto.VerifyEmailDTO;
import com.campus.zhihu.entity.SysEmailVerification;
import com.campus.zhihu.entity.SysUser;
import com.campus.zhihu.mapper.EmailVerificationMapper;
import com.campus.zhihu.mapper.UserMapper;
import com.campus.zhihu.service.EmailService;
import com.campus.zhihu.service.UserService;
import com.campus.zhihu.util.JwtUtil;
import com.campus.zhihu.vo.LoginVO;
import com.campus.zhihu.vo.RefreshTokenVO;
import com.campus.zhihu.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户服务实现类
 *
 * @author CampusZhihu Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final EmailVerificationMapper emailVerificationMapper;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder =
        new BCryptPasswordEncoder();

    /**
     * 注册赠送积分
     */
    @Value("${campus.points.register:100}")
    private Integer registerPoints;

    /**
     * 最大重试次数（乐观锁）
     */
    private static final int MAX_RETRY_TIMES = 3;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO register(RegisterDTO registerDTO) {
        log.info(
            "用户注册: username={}, email={}, studentId={}",
            registerDTO.getUsername(),
            registerDTO.getEmail(),
            registerDTO.getStudentId()
        );

        // 1. 检查用户名是否已存在
        if (checkUsernameExists(registerDTO.getUsername())) {
            throw new BusinessException(ResultCode.USERNAME_ALREADY_EXISTS);
        }

        // 2. 检查邮箱是否已存在
        if (checkEmailExists(registerDTO.getEmail())) {
            throw new BusinessException(ResultCode.EMAIL_ALREADY_EXISTS);
        }

        // 3. 检查学号是否已存在（如果填写了学号）
        if (
            registerDTO.getStudentId() != null &&
            !registerDTO.getStudentId().isEmpty()
        ) {
            if (checkStudentIdExists(registerDTO.getStudentId())) {
                throw new BusinessException(
                    ResultCode.STUDENT_ID_ALREADY_EXISTS
                );
            }
        }

        // 4. 创建用户实体
        SysUser user = new SysUser();
        user.setUsername(registerDTO.getUsername());
        user.setEmail(registerDTO.getEmail());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword())); // BCrypt 加密
        user.setStudentId(registerDTO.getStudentId());
        // user.setRealName(registerDTO.getRealName()); // 数据库表中无realName字段，暂时注释
        user.setMajor(registerDTO.getMajor());        user.setGrade(registerDTO.getGrade());
        user.setAvatar(registerDTO.getAvatar());
        user.setPoints(registerPoints); // 注册赠送积分

        // 5. 插入数据库
        int rows = userMapper.insert(user);
        if (rows <= 0) {
            throw new BusinessException(
                ResultCode.DATABASE_ERROR.getCode(),
                "用户注册失败"
            );
        }

        log.info(
            "用户注册成功: userId={}, username={}",
            user.getId(),
            user.getUsername()
        );

        // 6. 发送邮箱验证邮件
        try {
            String verifyToken = generateVerifyToken();
            SysEmailVerification verification = new SysEmailVerification();
            verification.setEmail(user.getEmail());
            verification.setToken(verifyToken);
            verification.setType(SysEmailVerification.TYPE_REGISTER);
            verification.setIsVerified(SysEmailVerification.NOT_VERIFIED);
            verification.setExpireTime(java.time.LocalDateTime.now().plusHours(24)); // 24小时后过期
            
            emailVerificationMapper.insert(verification);

            // 使用线程池异步发送邮件（避免阻塞注册流程）
            sendVerificationEmailAsync(user.getEmail(), verifyToken, user.getUsername());

        } catch (Exception e) {
            log.error("创建邮箱验证记录失败: email={}, error={}", user.getEmail(), e.getMessage());
            // 不影响注册流程，只记录日志
        }

        // 7. 返回用户信息
        return convertToVO(user);
    }

    /**
     * 生成验证Token
     */
    private String generateVerifyToken() {
        return java.util.UUID.randomUUID().toString().replace("-", "");
    }

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        log.info("用户登录: username={}", loginDTO.getUsername());

        // 1. 查询用户
        SysUser user = userMapper.selectByUsername(loginDTO.getUsername());
        if (user == null) {
            throw new BusinessException(ResultCode.USERNAME_OR_PASSWORD_ERROR);
        }

        // 2. 验证密码
        if (
            !passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())
        ) {
            throw new BusinessException(ResultCode.USERNAME_OR_PASSWORD_ERROR);
        }

        // 3. 生成 Access Token（短期有效，15分钟）
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername());
        
        // 4. 生成 Refresh Token（长期有效，7天）
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());

        // 5. 获取 Access Token 过期时间（秒）
        Long expiresIn = jwtUtil.getExpiration() / 1000;

        // 6. 转换用户信息
        UserVO userVO = convertToVO(user);

        log.info(
            "用户登录成功: userId={}, username={}",
            user.getId(),
            user.getUsername()
        );

        // 7. 返回登录结果（包含双Token）
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(accessToken);
        loginVO.setTokenType("Bearer");
        loginVO.setExpiresIn(expiresIn);
        loginVO.setUserInfo(userVO);
        loginVO.setRefreshToken(refreshToken);
        
        return loginVO;
    }

    @Override
    public RefreshTokenVO refreshToken(String refreshToken) {
        log.info("刷新Token");

        // 1. 验证 Refresh Token 是否有效
        if (!jwtUtil.validateRefreshToken(refreshToken)) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }

        // 2. 从 Refresh Token 中获取用户信息
        Long userId = jwtUtil.getUserIdFromToken(refreshToken);
        String username = jwtUtil.getUsernameFromToken(refreshToken);

        if (userId == null || username == null) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }

        // 3. 检查用户是否存在
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        // 4. 生成新的 Access Token
        String newAccessToken = jwtUtil.generateAccessToken(userId, username);
        
        // 5. 生成新的 Refresh Token
        String newRefreshToken = jwtUtil.generateRefreshToken(userId, username);

        // 6. 获取 Access Token 过期时间（秒）
        Long expiresIn = jwtUtil.getExpiration() / 1000;

        log.info(
            "Token刷新成功: userId={}, username={}",
            userId,
            username
        );

        // 7. 返回新的Token信息
        return new RefreshTokenVO(
            newAccessToken,
            "Bearer",
            expiresIn,
            newRefreshToken
        );
    }

    @Override
    public Boolean verifyEmail(VerifyEmailDTO verifyEmailDTO) {
        log.info("验证邮箱: token={}", verifyEmailDTO.getToken());

        // 1. 查询验证记录
        LambdaQueryWrapper<SysEmailVerification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysEmailVerification::getToken, verifyEmailDTO.getToken());
        SysEmailVerification verification = emailVerificationMapper.selectOne(wrapper);

        if (verification == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "验证链接无效");
        }

        // 2. 检查是否已验证
        if (verification.getIsVerified() == SysEmailVerification.VERIFIED) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "邮箱已验证");
        }

        // 3. 检查是否过期
        if (verification.getExpireTime().isBefore(java.time.LocalDateTime.now())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "验证链接已过期");
        }

        // 4. 更新验证状态
        verification.setIsVerified(SysEmailVerification.VERIFIED);
        verification.setVerifiedTime(java.time.LocalDateTime.now());
        emailVerificationMapper.updateById(verification);

        // 5. 更新用户邮箱验证状态（如果用户表有此字段）
        SysUser user = userMapper.selectByEmail(verification.getEmail());
        if (user != null) {
            // 假设用户表有email_verified字段
            // user.setEmailVerified(true);
            // userMapper.updateById(user);
            log.info("邮箱验证成功: userId={}, email={}", user.getId(), verification.getEmail());
        }

        return true;
    }

    @Override
    public Boolean resendVerificationEmail(String email) {
        log.info("重新发送验证邮件: email={}", email);

        // 1. 检查用户是否存在
        SysUser user = userMapper.selectByEmail(email);
        if (user == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "用户不存在");
        }

        // 2. 检查是否已验证（可选）
        // LambdaQueryWrapper<SysEmailVerification> wrapper = new LambdaQueryWrapper<>();
        // wrapper.eq(SysEmailVerification::getEmail, email)
        //        .eq(SysEmailVerification::getIsVerified, SysEmailVerification.VERIFIED);
        // if (emailVerificationMapper.selectCount(wrapper) > 0) {
        //     throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "邮箱已验证");
        // }

        // 3. 生成新的验证Token
        String verifyToken = generateVerifyToken();

        // 4. 创建验证记录
        SysEmailVerification verification = new SysEmailVerification();
        verification.setEmail(email);
        verification.setToken(verifyToken);
        verification.setType(SysEmailVerification.TYPE_REGISTER);
        verification.setIsVerified(SysEmailVerification.NOT_VERIFIED);
        verification.setExpireTime(java.time.LocalDateTime.now().plusHours(24)); // 24小时后过期

        emailVerificationMapper.insert(verification);

        // 5. 发送邮件
        emailService.sendVerificationEmail(email, verifyToken, user.getUsername());

        return true;
    }

    @Override
    public UserVO getUserById(Long userId) {
        if (userId == null) {
            throw new BusinessException(
                ResultCode.PARAM_IS_BLANK.getCode(),
                "用户ID不能为空"
            );
        }

        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        return convertToVOWithStats(user);
    }

    @Override
    public UserVO getUserByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new BusinessException(
                ResultCode.PARAM_IS_BLANK.getCode(),
                "用户名不能为空"
            );
        }

        SysUser user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        return convertToVOWithStats(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO updateUser(UserUpdateDTO userUpdateDTO) {
        log.info("更新用户信息: userId={}", userUpdateDTO.getUserId());

        // 1. 查询用户是否存在
        SysUser user = userMapper.selectById(userUpdateDTO.getUserId());
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        // 2. 更新用户信息
        if (userUpdateDTO.getAvatar() != null) {
            user.setAvatar(userUpdateDTO.getAvatar());
        }
        if (userUpdateDTO.getEmail() != null) {
            // 检查邮箱是否已被其他用户使用
            if (!user.getEmail().equals(userUpdateDTO.getEmail())) {
                if (checkEmailExists(userUpdateDTO.getEmail())) {
                    throw new BusinessException(
                        ResultCode.EMAIL_ALREADY_EXISTS.getCode(),
                        "该邮箱已被其他用户使用"
                    );
                }
                user.setEmail(userUpdateDTO.getEmail());
            }
        }
        if (userUpdateDTO.getNickname() != null) {
            user.setNickname(userUpdateDTO.getNickname());
        }
        if (userUpdateDTO.getBio() != null) {
            user.setBio(userUpdateDTO.getBio());
        }
        if (userUpdateDTO.getSchool() != null) {
            user.setSchool(userUpdateDTO.getSchool());
        }
        if (userUpdateDTO.getMajor() != null) {
            user.setMajor(userUpdateDTO.getMajor());
        }
        if (userUpdateDTO.getGrade() != null) {
            user.setGrade(userUpdateDTO.getGrade());
        }
        if (userUpdateDTO.getStudentId() != null) {
            // 检查学号是否已被其他用户使用
            if (!user.getStudentId().equals(userUpdateDTO.getStudentId())) {
                if (checkStudentIdExists(userUpdateDTO.getStudentId())) {
                    throw new BusinessException(
                        ResultCode.PARAM_ERROR.getCode(),
                        "该学号已被其他用户使用"
                    );
                }
                user.setStudentId(userUpdateDTO.getStudentId());
            }
        }

        // 3. 保存到数据库
        int rows = userMapper.updateById(user);
        if (rows <= 0) {
            throw new BusinessException(
                ResultCode.DATABASE_ERROR.getCode(),
                "更新用户信息失败"
            );
        }

        log.info("用户信息更新成功: userId={}", user.getId());

        // 4. 返回更新后的用户信息
        return convertToVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        log.info("修改密码: userId={}", userId);

        // 1. 查询用户是否存在
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        // 2. 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(
                ResultCode.PARAM_ERROR.getCode(),
                "旧密码错误"
            );
        }

        // 3. 验证新密码不能与旧密码相同
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new BusinessException(
                ResultCode.PARAM_ERROR.getCode(),
                "新密码不能与旧密码相同"
            );
        }

        // 4. 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        int rows = userMapper.updateById(user);
        if (rows <= 0) {
            throw new BusinessException(
                ResultCode.DATABASE_ERROR.getCode(),
                "修改密码失败"
            );
        }

        log.info("密码修改成功: userId={}", userId);
    }

    @Override
    public boolean checkUsernameExists(String username) {
        return userMapper.existsByUsername(username);
    }

    @Override
    public boolean checkStudentIdExists(String studentId) {
        return userMapper.existsByStudentId(studentId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUserPoints(Long userId, Integer points) {
        if (userId == null || points == null) {
            throw new BusinessException(
                ResultCode.PARAM_IS_BLANK.getCode(),
                "用户ID和积分不能为空"
            );
        }

        if (points < 0) {
            throw new BusinessException(
                ResultCode.PARAM_ERROR.getCode(),
                "积分不能为负数"
            );
        }

        // 使用乐观锁更新，最多重试 3 次
        for (int i = 0; i < MAX_RETRY_TIMES; i++) {
            SysUser user = userMapper.selectById(userId);
            if (user == null) {
                throw new BusinessException(ResultCode.USER_NOT_EXIST);
            }

            int rows = userMapper.updatePointsWithVersion(
                userId,
                points,
                user.getVersion()
            );
            if (rows > 0) {
                log.info(
                    "更新用户积分成功: userId={}, oldPoints={}, newPoints={}",
                    userId,
                    user.getPoints(),
                    points
                );
                return true;
            }

            log.warn("更新用户积分失败，版本冲突，重试第 {} 次", i + 1);
        }

        log.error("更新用户积分失败，超过最大重试次数: userId={}", userId);
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addUserPoints(Long userId, Integer pointsDelta) {
        if (userId == null || pointsDelta == null) {
            throw new BusinessException(
                ResultCode.PARAM_IS_BLANK.getCode(),
                "用户ID和积分不能为空"
            );
        }

        // 使用乐观锁更新，最多重试 3 次
        for (int i = 0; i < MAX_RETRY_TIMES; i++) {
            SysUser user = userMapper.selectById(userId);
            if (user == null) {
                throw new BusinessException(ResultCode.USER_NOT_EXIST);
            }

            int rows = userMapper.updatePointsByDelta(
                userId,
                pointsDelta,
                user.getVersion()
            );
            if (rows > 0) {
                log.info(
                    "增加用户积分成功: userId={}, oldPoints={}, delta={}, newPoints={}",
                    userId,
                    user.getPoints(),
                    pointsDelta,
                    user.getPoints() + pointsDelta
                );
                return true;
            }

            log.warn("增加用户积分失败，版本冲突，重试第 {} 次", i + 1);
        }

        log.error("增加用户积分失败，超过最大重试次数: userId={}", userId);
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deductUserPoints(Long userId, Integer pointsDelta) {
        if (userId == null || pointsDelta == null) {
            throw new BusinessException(
                ResultCode.PARAM_IS_BLANK.getCode(),
                "用户ID和积分不能为空"
            );
        }

        if (pointsDelta <= 0) {
            throw new BusinessException(
                ResultCode.PARAM_ERROR.getCode(),
                "扣除积分必须大于0"
            );
        }

        // 使用乐观锁更新，最多重试 3 次
        for (int i = 0; i < MAX_RETRY_TIMES; i++) {
            SysUser user = userMapper.selectById(userId);
            if (user == null) {
                throw new BusinessException(ResultCode.USER_NOT_EXIST);
            }

            // 检查积分是否足够
            if (user.getPoints() < pointsDelta) {
                log.warn(
                    "用户积分不足: userId={}, currentPoints={}, requiredPoints={}",
                    userId,
                    user.getPoints(),
                    pointsDelta
                );
                throw new BusinessException(ResultCode.INSUFFICIENT_POINTS);
            }

            // 扣除积分（负数）
            int rows = userMapper.updatePointsByDelta(
                userId,
                -pointsDelta,
                user.getVersion()
            );
            if (rows > 0) {
                log.info(
                    "扣除用户积分成功: userId={}, oldPoints={}, delta=-{}, newPoints={}",
                    userId,
                    user.getPoints(),
                    pointsDelta,
                    user.getPoints() - pointsDelta
                );
                return true;
            }

            log.warn("扣除用户积分失败，版本冲突，重试第 {} 次", i + 1);
        }

        log.error("扣除用户积分失败，超过最大重试次数: userId={}", userId);
        return false;
    }

    @Override
    public UserVO convertToVO(SysUser user) {
        if (user == null) {
            return null;
        }

        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public UserVO convertToVOWithStats(SysUser user) {
        if (user == null) {
            return null;
        }

        UserVO userVO = convertToVO(user);

        // 查询统计信息
        Integer questionCount = userMapper.countQuestionsByUserId(user.getId());
        Integer answerCount = userMapper.countAnswersByUserId(user.getId());
        Integer acceptedCount = userMapper.countAcceptedAnswersByUserId(
            user.getId()
        );

        userVO.setQuestionCount(questionCount);
        userVO.setAnswerCount(answerCount);
        userVO.setAcceptedCount(acceptedCount);

        return userVO;
    }

    @Override
    public boolean checkEmailExists(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return userMapper.existsByEmail(email);
    }

    @Override
    public java.util.Map<String, Integer> getUserStats(Long userId) {
        log.info("查询用户统计信息: userId={}", userId);

        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_EXIST);
        }

        java.util.Map<String, Integer> stats = new java.util.HashMap<>();

        // 问题数
        Integer questionCount = userMapper.countQuestionsByUserId(userId);
        stats.put("questionCount", questionCount != null ? questionCount : 0);

        // 回答数
        Integer answerCount = userMapper.countAnswersByUserId(userId);
        stats.put("answerCount", answerCount != null ? answerCount : 0);

        // 被采纳的回答数
        Integer acceptedCount = userMapper.countAcceptedAnswersByUserId(userId);
        stats.put("acceptedCount", acceptedCount != null ? acceptedCount : 0);

        // 收藏数（暂时使用默认值，需要在mapper中添加对应方法）
        stats.put("collectionCount", 0);

        // 获得的点赞数（暂时使用默认值，需要在mapper中添加对应方法）
        stats.put("likeCount", 0);

        // 当前积分
        stats.put("points", user.getPoints() != null ? user.getPoints() : 0);

        return stats;
    }

    /**
     * 异步发送验证邮件
     * 使用 @Async 注解，邮件发送将在独立的线程池中执行
     *
     * @param email    收件人邮箱
     * @param token    验证Token
     * @param username 用户名
     */
    @Async("emailTaskExecutor")
    public void sendVerificationEmailAsync(String email, String token, String username) {
        try {
            emailService.sendVerificationEmail(email, token, username);
            log.info("验证邮件发送成功: email={}, username={}", email, username);
        } catch (Exception e) {
            log.error("发送验证邮件失败: email={}, username={}, error={}", email, username, e.getMessage(), e);
        }
    }
}
