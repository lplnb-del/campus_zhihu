package com.campus.zhihu;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * CampusZhihu 主启动类
 * 校园知乎 - 高校知识共享平台
 *
 * @author CampusZhihu Team
 */
@SpringBootApplication
@EnableTransactionManagement
@MapperScan("com.campus.zhihu.mapper")
public class CampusZhihuApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusZhihuApplication.class, args);
        System.out.println("\n" +
                "   ____                                  _______ _     _ _           \n" +
                "  / ___|__ _ _ __ ___  _ __  _   _ ___  |__  / || |__ (_) |__  _   _ \n" +
                " | |   / _` | '_ ` _ \\| '_ \\| | | / __|   / /| || '_ \\| | '_ \\| | | |\n" +
                " | |__| (_| | | | | | | |_) | |_| \\__ \\  / /_| || | | | | | | | |_| |\n" +
                "  \\____\\__,_|_| |_| |_| .__/ \\__,_|___/ /____|_||_| |_|_|_| |_|\\__,_|\n" +
                "                      |_|                                             \n");
        System.out.println("========================================");
        System.out.println("CampusZhihu 启动成功！");
        System.out.println("接口文档地址: http://localhost:8080/api/doc.html");
        System.out.println("Druid 监控地址: http://localhost:8080/api/druid");
        System.out.println("========================================\n");
    }
}
