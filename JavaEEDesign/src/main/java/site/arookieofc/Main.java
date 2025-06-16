package site.arookieofc;

import lombok.extern.slf4j.Slf4j;
import site.arookieofc.annotation.config.Application;
import site.arookieofc.processor.ioc.AnnotationApplicationContext;
import site.arookieofc.processor.ioc.ApplicationContextHolder;
import site.arookieofc.server.EmbeddedTomcatServer;
import site.arookieofc.server.McpServer;

@Application
@Slf4j
public class Main {
    public static void main(String[] args) {
        try {
            init();
            startMCP();
            startWebServer();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            System.exit(1);
        }
    }

    private static void init() {
        log.info("正在初始化IOC容器...");
        AnnotationApplicationContext applicationContext = new AnnotationApplicationContext();
        ApplicationContextHolder.setApplicationContext(applicationContext);
        log.info("IOC容器初始化完成");
    }

    private static void startMCP() {
        try {
            log.info("正在启动MCP服务器...");
            McpServer.initializeAndStart();
            log.info("MCP服务器启动完成");
            
            // 等待MCP服务器完全启动
            Thread.sleep(2000);
            
        } catch (Exception e) {
            log.warn("MCP服务器启动失败，将在普通模式下运行: {}", e.getMessage());
            // MCP启动失败不影响主应用启动
        }
    }

    private static void startWebServer() throws Exception {
        log.info("正在启动主服务器...");
        EmbeddedTomcatServer.start();
        log.info("所有服务启动完成");
    }
}