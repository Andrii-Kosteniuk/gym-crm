package com.training.gym.config.web;

import com.training.gym.config.app.AppConfig;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.apache.catalina.startup.Tomcat;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@NoArgsConstructor
public final class EmbeddedTomcatServer {

    private static final int SERVER_PORT = 8080;
    private static final String SERVLET_NAME = "dispatcher";
    private static final String SERVLET_MAPPING = "/";

    public static void start() {
        Tomcat tomcat = createTomcat();

        try {
            Context context = configureContext(tomcat);
            configureDispatcherServlet(context);
        } catch (Exception e) {
            log.error("Failed to configure tomcat context {}", e.getMessage(), e);
        }
        try {
            tomcat.start();
            tomcat.getServer().await();
        } catch (Exception e) {
            log.error("Failed to start tomcat {}", e.getMessage(), e);
        }

    }

    private static Tomcat createTomcat() {
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(SERVER_PORT);
        tomcat.getConnector();

        return tomcat;
    }

    private static Context configureContext(Tomcat tomcat) throws IOException {

        Path docBase = Path.of("target", "tomcat-docbase")
                .toAbsolutePath()
                .normalize();

        Files.createDirectories(docBase);

        return tomcat.addContext("", docBase.toString());
    }

    private static void configureDispatcherServlet(Context context) {

        var applicationContext = new AnnotationConfigWebApplicationContext();

        applicationContext.getEnvironment().setActiveProfiles("dev");

        applicationContext.register(AppConfig.class, ValidationConfig.class);

        var dispatcherServlet = new DispatcherServlet(applicationContext);

        Wrapper wrapper = Tomcat.addServlet(context, SERVLET_NAME, dispatcherServlet);

        wrapper.setLoadOnStartup(1);
        wrapper.addMapping(SERVLET_MAPPING);
    }
}