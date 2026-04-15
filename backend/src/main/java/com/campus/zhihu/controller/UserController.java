package com.campus.zhihu.controller;

import com.campus.zhihu.annotation.RateLimiter;
import com.campus.zhihu.common.BusinessException;
import com.campus.zhihu.common.Result;
import com.campus.zhihu.common.ResultCode;
import com.campus.zhihu.dto.ChangePasswordDTO;
import com.campus.zhihu.dto.LoginDTO;
import com.campus.zhihu.dto.RefreshTokenDTO;
import com.campus.zhihu.dto.RegisterDTO;
import com.campus.zhihu.dto.UserUpdateDTO;
import com.campus.zhihu.dto.VerifyEmailDTO;
import com.campus.zhihu.service.UserService;
import com.campus.zhihu.util.JwtUtil;
import com.campus.zhihu.vo.LoginVO;
import com.campus.zhihu.vo.RefreshTokenVO;
import com.campus.zhihu.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 用户控制器
 * 处理用户相关的 HTTP 请求
 *
 * @author CampusZhihu Team
 */
@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户注册、登录、信息管理")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    /**
     * 用户注册
     *
     * @param registerDTO 注册信息
     * @return 注册成功的用户信息
     */
    @Operation(summary = "用户注册", description = "新用户注册，注册后会自动发送邮箱验证邮件")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "注册成功"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "2003", description = "用户名已存在"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "参数错误")
    })
    @PostMapping("/register")
    @RateLimiter(limit = 3, timeout = 60, message = "注册请求过于频繁，请稍后再试")
    public Result<UserVO> register(@Validated @RequestBody RegisterDTO registerDTO) {
        log.info("收到注册请求: username={}", registerDTO.getUsername());
        UserVO userVO = userService.register(registerDTO);
        return Result.success("注册成功", userVO);
    }

    /**
     * 用户登录
     *
     * @param loginDTO 登录信息
     * @return 登录结果（包含 Token 和用户信息）
     */
    @Operation(summary = "用户登录", description = "用户登录，返回Access Token和Refresh Token")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "登录成功"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "2002", description = "用户名或密码错误")
    })
    @PostMapping("/login")
    @RateLimiter(limit = 5, timeout = 60, message = "登录请求过于频繁，请稍后再试")
    public Result<LoginVO> login(@Validated @RequestBody LoginDTO loginDTO) {
        log.info("收到登录请求: username={}", loginDTO.getUsername());
        LoginVO loginVO = userService.login(loginDTO);
        return Result.success("登录成功", loginVO);
    }

    /**
     * 刷新Token
     *
     * @param refreshTokenDTO 刷新Token请求
     * @return 新的Token信息
     */
    @PostMapping("/refresh-token")
    public Result<RefreshTokenVO> refreshToken(@Validated @RequestBody RefreshTokenDTO refreshTokenDTO) {
        log.info("收到刷新Token请求");
        RefreshTokenVO result = userService.refreshToken(refreshTokenDTO.getRefreshToken());
        return Result.success("Token刷新成功", result);
    }

    /**
     * 验证邮箱
     *
     * @param verifyEmailDTO 验证邮箱请求
     * @return 验证结果
     */
    @PostMapping("/verify-email")
    public Result<Boolean> verifyEmail(@Validated @RequestBody VerifyEmailDTO verifyEmailDTO) {
        log.info("收到验证邮箱请求");
        Boolean result = userService.verifyEmail(verifyEmailDTO);
        return Result.success("邮箱验证成功", result);
    }

    /**
     * 重新发送验证邮件
     *
     * @param email 邮箱地址
     * @return 发送结果
     */
    @PostMapping("/resend-verification-email")
    public Result<Boolean> resendVerificationEmail(@RequestParam String email) {
        log.info("收到重新发送验证邮件请求: email={}", email);
        Boolean result = userService.resendVerificationEmail(email);
        return Result.success("验证邮件已发送，请查收", result);
    }

    /**
     * 获取当前登录用户信息
     *
     * @param request HTTP 请求
     * @return 用户信息
     */
    @GetMapping("/info")
    public Result<UserVO> getCurrentUserInfo(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        UserVO userVO = userService.getUserById(userId);
        return Result.success(userVO);
    }

    /**
     * 根据用户ID获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    @Operation(summary = "获取用户信息", description = "根据用户ID获取用户详细信息")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "获取成功"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "2001", description = "用户不存在")
    })
    @GetMapping("/{userId}")
    public Result<UserVO> getUserById(
            @Parameter(description = "用户ID", required = true, example = "1")
            @PathVariable Long userId) {
        log.info("获取用户信息: userId={}", userId);
        UserVO userVO = userService.getUserById(userId);
        return Result.success(userVO);
    }

    /**
     * 根据用户名获取用户信息
     *
     * @param username 用户名
     * @return 用户信息
     */
    @GetMapping("/username/{username}")
    public Result<UserVO> getUserByUsername(@PathVariable String username) {
        log.info("获取用户信息: username={}", username);
        UserVO userVO = userService.getUserByUsername(username);
        return Result.success(userVO);
    }

    /**
     * 更新当前登录用户信息
     *
     * @param userUpdateDTO 用户更新信息
     * @param request       HTTP 请求
     * @return 更新后的用户信息
     */
    @Operation(summary = "更新用户信息", description = "更新当前登录用户的个人信息")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "更新成功"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "1001", description = "未登录")
    })
    @PutMapping("/update")
    public Result<UserVO> updateUser(@Validated @RequestBody UserUpdateDTO userUpdateDTO,
                                     HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        userUpdateDTO.setUserId(userId);
        log.info("更新用户信息: userId={}", userId);
        UserVO userVO = userService.updateUser(userUpdateDTO);
        return Result.success("更新成功", userVO);
    }

    /**
     * 修改密码
     *
     * @param changePasswordDTO 修改密码请求
     * @param request           HTTP 请求
     * @return 成功信息
     */
    @Operation(summary = "修改密码", description = "修改当前登录用户的密码")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "修改成功"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "1001", description = "未登录"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "1002", description = "旧密码错误")
    })
    @PutMapping("/password")
    public Result<String> changePassword(@Validated @RequestBody ChangePasswordDTO changePasswordDTO,
                                         HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        log.info("修改密码: userId={}", userId);
        userService.changePassword(userId, changePasswordDTO.getOldPassword(), changePasswordDTO.getNewPassword());
        return Result.success("密码修改成功", "密码已修改");
    }

    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @return true: 存在, false: 不存在
     */
    @GetMapping("/check/username")
    public Result<Boolean> checkUsername(@RequestParam String username) {
        boolean exists = userService.checkUsernameExists(username);
        return Result.success(exists);
    }

    /**
     * 检查学号是否存在
     *
     * @param studentId 学号
     * @return true: 存在, false: 不存在
     */
    @GetMapping("/check/studentId")
    public Result<Boolean> checkStudentId(@RequestParam String studentId) {
        boolean exists = userService.checkStudentIdExists(studentId);
        return Result.success(exists);
    }

    /**
     * 用户登出
     *
     * @param request HTTP 请求
     * @return 成功信息
     */
    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request);
        log.info("用户登出: userId={}", userId);
        // 实际上 JWT 是无状态的，登出操作由前端删除 Token 即可
        // 这里可以记录登出日志或做其他业务处理
        return Result.success("登出成功", "logout");
    }

    /**
     * 获取用户统计信息
     *
     * @param userId 用户ID
     * @return 用户统计信息（问题数、回答数、收藏数、点赞数、积分）
     */
    @Operation(summary = "获取用户统计信息", description = "获取用户的问题数、回答数、收藏数、点赞数、积分等统计信息")
    @GetMapping("/{userId}/stats")
    public Result<java.util.Map<String, Integer>> getUserStats(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        log.info("查询用户统计信息: userId={}", userId);
        java.util.Map<String, Integer> stats = userService.getUserStats(userId);
        return Result.success(stats);
    }

    /**
     * 从请求中获取当前登录用户的 ID
     *
     * @param request HTTP 请求
     * @return 用户ID
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader(jwtUtil.getHeader());
        String token = jwtUtil.getTokenFromHeader(authHeader);

        if (token == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            throw new BusinessException(ResultCode.TOKEN_INVALID);
        }

        return userId;
    }
}
