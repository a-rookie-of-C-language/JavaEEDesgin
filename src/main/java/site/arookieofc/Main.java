package site.arookieofc;

import lombok.extern.slf4j.Slf4j;
import site.arookieofc.annotation.config.Application;
import site.arookieofc.annotation.config.ComponentScan;
import site.arookieofc.processor.ComponentScanner;
import site.arookieofc.processor.ConfigProcessor;
import site.arookieofc.server.EmbeddedTomcatServer;
import site.arookieofc.utils.DatabaseUtil;

@Application
@ComponentScan(basePackages = "site.arookieofc.controller")
@Slf4j
public class Main {
    public static void main(String[] args) {
        try {
            System.out.println("=== 应用启动中 ===");

            System.out.println("正在加载配置文件...");
            ConfigProcessor.injectStaticFields(DatabaseUtil.class);
            ConfigProcessor.injectStaticFields(EmbeddedTomcatServer.class);  // 添加这行
            System.out.println("正在测试数据库连接...");
            DatabaseUtil.getConnection();
            System.out.println("数据库连接成功!");
            System.out.println("正在扫描和注册控制器...");
            ComponentScanner.scanAndRegisterControllers(Main.class);
            System.out.println("控制器注册成功!");
            System.out.println("验证路由注册...");
            System.out.println("正在启动嵌入式Tomcat服务器...");
            EmbeddedTomcatServer.start();
        } catch (Exception e) {
            System.err.println("应用启动失败: " + e.getMessage());
            e.printStackTrace();  // 改为打印完整堆栈跟踪
            System.exit(1);
        }
    }
}