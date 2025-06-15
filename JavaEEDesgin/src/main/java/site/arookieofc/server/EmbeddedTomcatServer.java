package site.arookieofc.server;

import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.startup.Tomcat;
import site.arookieofc.annotation.config.Config;
import java.io.File;

@Slf4j
public class EmbeddedTomcatServer {

    @Config("server.port")
    private static int port;

    public static void start() throws Exception {
        if (port == 0) {
            port = 8080;
            System.out.println("端口配置失败，使用默认端口: " + port);
        }
        log.info("端口:{} ", port);
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(port);
        tomcat.setBaseDir("tomcat." + port);
        String webappDirLocation = "src/main/webapp/";
        File webappDir = new File(webappDirLocation);
        tomcat.addWebapp("", webappDir.getAbsolutePath());
        tomcat.start();
        tomcat.getConnector().getLocalPort();
        tomcat.getServer().await();
    }
}