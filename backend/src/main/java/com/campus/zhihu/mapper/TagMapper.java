package com.campus.zhihu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.zhihu.entity.BizTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 标签 Mapper 接口
 *
 * @author CampusZhihu Team
 */
@Mapper
public interface TagMapper extends BaseMapper<BizTag> {

    /**
     * 批量更新标签使用次数
     *
     * @param tagIds 标签ID列表
     * @param delta  变化量
     * @return 更新行数
     */
    int batchUpdateUseCount(@Param("tagIds") List<Long> tagIds, @Param("delta") int delta);

    /**
     * 根据ID列表查询标签
     *
     * @param tagIds 标签ID列表
     * @return 标签列表
     */
    List<BizTag> selectByIds(@Param("tagIds") List<Long> tagIds);
}