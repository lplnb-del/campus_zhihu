package com.campus.zhihu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.zhihu.entity.BizUserTag;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户关注标签 Mapper 接口
 *
 * @author CampusZhihu Team
 */
@Mapper
public interface UserTagMapper extends BaseMapper<BizUserTag> {
}