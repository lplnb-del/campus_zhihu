package com.campus.zhihu.vo;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 分页响应封装类
 * 用于将 MyBatis-Plus 的 IPage 转换为前端期望的格式
 *
 * @author CampusZhihu Team
 * @param <T> 数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 数据列表
     */
    private List<T> list;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页码
     */
    private Long page;

    /**
     * 每页大小
     */
    private Long size;

    /**
     * 总页数
     */
    private Long pages;

    /**
     * 问题总数（可选字段）
     */
    private Long totalQuestions;

    /**
     * 从 MyBatis-Plus IPage 转换
     */
    public static <T> PageResponse<T> of(IPage<T> page) {
        PageResponse<T> response = new PageResponse<>();
        response.setList(page.getRecords());
        response.setTotal(page.getTotal());
        response.setPage(page.getCurrent());
        response.setSize(page.getSize());
        response.setPages(page.getPages());
        return response;
    }

    /**
     * 从 MyBatis-Plus IPage 转换（带问题总数）
     */
    public static <T> PageResponse<T> of(IPage<T> page, Long totalQuestions) {
        PageResponse<T> response = of(page);
        response.setTotalQuestions(totalQuestions);
        return response;
    }

    /**
     * 从List和分页信息创建
     */
    public static <T> PageResponse<T> of(List<T> list, Long total, Long page, Long size) {
        PageResponse<T> response = new PageResponse<>();
        response.setList(list);
        response.setTotal(total);
        response.setPage(page);
        response.setSize(size);
        response.setPages((total + size - 1) / size);
        return response;
    }
}
