package site.arookieofc.server;

import org.apache.catalina.startup.Tomcat;
import site.arookieofc.annotation.config.Config;
import java.io.File;

public class EmbeddedTomcatServer {
    @Config("server.port")
    private static int port;

    public static void start() throws Exception {
        // 添加调试信息
        System.out.println("配置的端口: " + port);
        
        // 如果端口为0，使用默认值
        if (port == 0) {
            port = 8080;
            System.out.println("端口配置失败，使用默认端口: " + port);
        }
        
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(port);
        
        // 设置工作目录
        tomcat.setBaseDir("tomcat." + port);
        
        // 检查webapp目录是否存在
        String webappDirLocation = "src/main/webapp/";
        File webappDir = new File(webappDirLocation);
        if (!webappDir.exists()) {
            System.out.println("创建webapp目录: " + webappDir.getAbsolutePath());
            webappDir.mkdirs();
        }
        
        // 添加webapp
        tomcat.addWebapp("", webappDir.getAbsolutePath());
        
        // 启动Tomcat
        tomcat.start();
        System.out.println("Tomcat successfully started on port: " + port);
        
        // 验证端口是否真正监听
        System.out.println("Tomcat connector port: " + tomcat.getConnector().getPort());
        System.out.println("Tomcat connector local port: " + tomcat.getConnector().getLocalPort());
        
        tomcat.getServer().await();
    }
}