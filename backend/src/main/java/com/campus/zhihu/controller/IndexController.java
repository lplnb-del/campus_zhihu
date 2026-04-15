package com.campus.zhihu.controller;

import com.campus.zhihu.common.Result;
import com.campus.zhihu.common.ResultCode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 根路径控制器
 * 处理根路径请求，返回友好的欢迎信息
 *
 * @author CampusZhihu Team
 */
@RestController
public class IndexController {

    /**
     * 根路径欢迎信息
     */
    @GetMapping("/")
    public Result<?> index() {
        return Result.success("CampusZhihu API 服务已启动");
    }
}
