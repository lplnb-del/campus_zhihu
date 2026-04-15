package com.campus.zhihu.service;

import com.campus.zhihu.dto.LoginDTO;
import com.campus.zhihu.dto.RefreshTokenDTO;
import com.campus.zhihu.dto.RegisterDTO;
import com.campus.zhihu.dto.UserUpdateDTO;
import com.campus.zhihu.dto.VerifyEmailDTO;
import com.campus.zhihu.entity.SysUser;
import com.campus.zhihu.vo.LoginVO;
import com.campus.zhihu.vo.RefreshTokenVO;
import com.campus.zhihu.vo.UserVO;

/**
 * 用户服务接口
 *
 * @author CampusZhihu Team
 */
public interface UserService {
    /**
     * 用户注册
     *
     * @param registerDTO 注册信息
     * @return 用户信息
     */
    UserVO register(RegisterDTO registerDTO);

    /**
     * 用户登录
     *
     * @param loginDTO 登录信息
     * @return 登录结果（包含 Token 和用户信息）
     */
    LoginVO login(LoginDTO loginDTO);

    /**
     * 刷新Token
     *
     * @param refreshTokenDTO 刷新Token请求
     * @return 新的Token信息
     */
    RefreshTokenVO refreshToken(String refreshToken);

    /**
     * 验证邮箱
     *
     * @param verifyEmailDTO 验证邮箱请求
     * @return true: 验证成功, false: 验证失败
     */
    Boolean verifyEmail(VerifyEmailDTO verifyEmailDTO);

    /**
     * 重新发送验证邮件
     *
     * @param email 邮箱地址
     * @return true: 发送成功, false: 发送失败
     */
    Boolean resendVerificationEmail(String email);

    /**
     * 根据用户ID获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    UserVO getUserById(Long userId);

    /**
     * 根据用户名获取用户信息
     *
     * @param username 用户名
     * @return 用户信息
     */
    UserVO getUserByUsername(String username);

    /**
     * 更新用户信息
     *
     * @param userUpdateDTO 用户更新信息
     * @return 更新后的用户信息
     */
    UserVO updateUser(UserUpdateDTO userUpdateDTO);

    /**
     * 修改密码
     *
     * @param userId      用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    void changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @return true: 存在, false: 不存在
     */
    boolean checkUsernameExists(String username);

    /**
     * 检查学号是否存在
     *
     * @param studentId 学号
     * @return true: 存在, false: 不存在
     */
    boolean checkStudentIdExists(String studentId);

    /**
     * 检查邮箱是否存在
     *
     * @param email 邮箱
     * @return true: 存在, false: 不存在
     */
    boolean checkEmailExists(String email);

    /**
     * 更新用户积分
     *
     * @param userId 用户ID
     * @param points 新的积分值
     * @return true: 成功, false: 失败
     */
    boolean updateUserPoints(Long userId, Integer points);

    /**
     * 增加用户积分
     *
     * @param userId      用户ID
     * @param pointsDelta 积分变化量（正数为增加，负数为减少）
     * @return true: 成功, false: 失败
     */
    boolean addUserPoints(Long userId, Integer pointsDelta);

    /**
     * 扣除用户积分（带并发控制）
     *
     * @param userId      用户ID
     * @param pointsDelta 积分扣除量（正数）
     * @return true: 成功, false: 失败（积分不足或并发冲突）
     */
    boolean deductUserPoints(Long userId, Integer pointsDelta);

    /**
     * 转换实体为 VO
     *
     * @param user 用户实体
     * @return 用户 VO
     */
    UserVO convertToVO(SysUser user);

    /**
     * 转换实体为 VO（包含统计信息）
     *
     * @param user 用户实体
     * @return 用户 VO（包含提问数、回答数、被采纳数）
     */
    UserVO convertToVOWithStats(SysUser user);

    /**
     * 获取用户统计信息
     *
     * @param userId 用户ID
     * @return 统计信息（问题数、回答数、收藏数、点赞数、积分）
     */
    java.util.Map<String, Integer> getUserStats(Long userId);
}
