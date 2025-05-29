package site.arookieofc;

import lombok.extern.slf4j.Slf4j;
import site.arookieofc.annotation.config.Application;
import site.arookieofc.server.EmbeddedTomcatServer;

@Application
@Slf4j
public class Main {
    public static void main(String[] args) {
        try {

            EmbeddedTomcatServer.start();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            System.exit(1);
        }
    }
}