package site.arookieofc;

import lombok.extern.slf4j.Slf4j;
import site.arookieofc.annotation.config.Application;
import site.arookieofc.processor.ioc.AnnotationApplicationContext;
import site.arookieofc.processor.ioc.ApplicationContextHolder;
import site.arookieofc.server.EmbeddedTomcatServer;

@Application
@Slf4j
public class Main {
    public static void main(String[] args) {
        try {
            AnnotationApplicationContext applicationContext = 
                new AnnotationApplicationContext("site.arookieofc");
            ApplicationContextHolder.setApplicationContext(applicationContext);
            log.info("IOC容器初始化完成");
            EmbeddedTomcatServer.start();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            System.exit(1);
        }
    }
}