package com.campus.zhihu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.zhihu.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 用户 Mapper 接口
 * 继承 MyBatis-Plus 的 BaseMapper，提供基础 CRUD 操作
 *
 * @author CampusZhihu Team
 */
@Mapper
public interface UserMapper extends BaseMapper<SysUser> {
    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户实体
     */
    @Select(
        "SELECT * FROM sys_user WHERE username = #{username} AND is_deleted = 0"
    )
    SysUser selectByUsername(@Param("username") String username);

    /**
     * 根据学号查询用户
     *
     * @param studentId 学号
     * @return 用户实体
     */
    @Select(
        "SELECT * FROM sys_user WHERE student_id = #{studentId} AND is_deleted = 0"
    )
    SysUser selectByStudentId(@Param("studentId") String studentId);

    /**
     * 根据邮箱查询用户
     *
     * @param email 邮箱
     * @return 用户实体
     */
    @Select(
        "SELECT * FROM sys_user WHERE email = #{email} AND is_deleted = 0"
    )
    SysUser selectByEmail(@Param("email") String email);

    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @return 存在返回 true，不存在返回 false
     */
    @Select(
        "SELECT COUNT(*) > 0 FROM sys_user WHERE username = #{username} AND is_deleted = 0"
    )
    boolean existsByUsername(@Param("username") String username);

    /**
     * 检查学号是否存在
     *
     * @param studentId 学号
     * @return 存在返回 true，不存在返回 false
     */
    @Select(
        "SELECT COUNT(*) > 0 FROM sys_user WHERE student_id = #{studentId} AND is_deleted = 0"
    )
    boolean existsByStudentId(@Param("studentId") String studentId);

    /**
     * 检查邮箱是否存在
     *
     * @param email 邮箱
     * @return 存在返回 true，不存在返回 false
     */
    @Select(
        "SELECT COUNT(*) > 0 FROM sys_user WHERE email = #{email} AND is_deleted = 0"
    )
    boolean existsByEmail(@Param("email") String email);

    /**
     * 根据用户名删除用户（测试数据清理用）
     *
     * @param username 用户名
     * @return 影响的行数
     */
    @org.apache.ibatis.annotations.Delete(
        "UPDATE sys_user SET is_deleted = 1 WHERE username = #{username}"
    )
    int deleteByUsername(@Param("username") String username);

    /**
     * 根据邮箱删除用户（测试数据清理用）
     *
     * @param email 邮箱
     * @return 影响的行数
     */
    @org.apache.ibatis.annotations.Delete(
        "UPDATE sys_user SET is_deleted = 1 WHERE email = #{email}"
    )
    int deleteByEmail(@Param("email") String email);

    /**
     * 根据学号删除用户（测试数据清理用）
     *
     * @param studentId 学号
     * @return 影响的行数
     */
    @org.apache.ibatis.annotations.Delete(
        "UPDATE sys_user SET is_deleted = 1 WHERE student_id = #{studentId}"
    )
    int deleteByStudentId(@Param("studentId") String studentId);

    /**
     * 更新用户积分（使用乐观锁）
     *
     * @param userId  用户ID
     * @param points  新的积分值
     * @param version 版本号
     * @return 影响的行数（1: 成功, 0: 失败-版本冲突）
     */
    @Update(
        "UPDATE sys_user SET points = #{points}, version = version + 1 " +
            "WHERE id = #{userId} AND version = #{version} AND is_deleted = 0"
    )
    int updatePointsWithVersion(
        @Param("userId") Long userId,
        @Param("points") Integer points,
        @Param("version") Integer version
    );

    /**
     * 增加用户积分（使用乐观锁）
     *
     * @param userId      用户ID
     * @param pointsDelta 积分变化量（正数为增加，负数为减少）
     * @param version     版本号
     * @return 影响的行数（1: 成功, 0: 失败-版本冲突）
     */
    @Update(
        "UPDATE sys_user SET points = points + #{pointsDelta}, version = version + 1 " +
            "WHERE id = #{userId} AND version = #{version} AND is_deleted = 0"
    )
    int updatePointsByDelta(
        @Param("userId") Long userId,
        @Param("pointsDelta") Integer pointsDelta,
        @Param("version") Integer version
    );

    /**
     * 获取用户的提问数量
     *
     * @param userId 用户ID
     * @return 提问数量
     */
    @Select(
        "SELECT COUNT(*) FROM biz_question WHERE user_id = #{userId} AND is_deleted = 0"
    )
    Integer countQuestionsByUserId(@Param("userId") Long userId);

    /**
     * 获取用户的回答数量
     *
     * @param userId 用户ID
     * @return 回答数量
     */
    @Select(
        "SELECT COUNT(*) FROM biz_answer WHERE user_id = #{userId} AND is_deleted = 0"
    )
    Integer countAnswersByUserId(@Param("userId") Long userId);

    /**
     * 获取用户被采纳的回答数量
     *
     * @param userId 用户ID
     * @return 被采纳的回答数量
     */
    @Select(
        "SELECT COUNT(*) FROM biz_answer WHERE user_id = #{userId} AND is_accepted = 1 AND is_deleted = 0"
    )
    Integer countAcceptedAnswersByUserId(@Param("userId") Long userId);

    /**
     * 扣除用户积分（使用乐观锁，防止并发问题）
     *
     * @param userId 用户ID
     * @param points 要扣除的积分
     * @return 影响的行数（1: 成功, 0: 失败-积分不足或版本冲突）
     */
    @Update(
        "UPDATE sys_user SET points = points - #{points}, version = version + 1 " +
            "WHERE id = #{userId} AND points >= #{points} AND is_deleted = 0"
    )
    int deductPoints(@Param("userId") Long userId, @Param("points") Integer points);

    /**
     * 增加用户积分
     *
     * @param userId 用户ID
     * @param points 要增加的积分
     * @return 影响的行数
     */
    @Update(
        "UPDATE sys_user SET points = points + #{points}, version = version + 1 " +
            "WHERE id = #{userId} AND is_deleted = 0"
    )
    int addPoints(@Param("userId") Long userId, @Param("points") Integer points);
}
