package com.campus.zhihu.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 标签 VO
 *
 * @author CampusZhihu Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 标签ID
     */
    private Long id;

    /**
     * 标签名称
     */
    private String name;

    /**
     * 标签描述
     */
    private String description;

    /**
     * 标签图标
     */
    private String icon;

    /**
     * 标签颜色
     */
    private String color;

    /**
     * 分类
     */
    private String category;

    /**
     * 问题数量
     */
    private Integer questionCount;

    /**
     * 关注数量
     */
    private Integer followCount;

    /**
     * 是否已关注
     */
    private Boolean isFollowed;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 更新时间
     */
    private String updateTime;
}