package com.campus.zhihu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.zhihu.entity.SysEmailVerification;
import org.apache.ibatis.annotations.Mapper;

/**
 * 邮箱验证Mapper接口
 *
 * @author CampusZhihu Team
 */
@Mapper
public interface EmailVerificationMapper extends BaseMapper<SysEmailVerification> {
}