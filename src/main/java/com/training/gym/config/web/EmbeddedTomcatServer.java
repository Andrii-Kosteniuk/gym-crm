package com.training.gym.config.web;

import com.training.gym.config.app.AppConfig;
import com.training.gym.web.filter.RequestLoggingFilter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Wrapper;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
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

            FilterDef filterDef = new FilterDef();
            filterDef.setFilterName("requestLoggingFilter");
            filterDef.setFilter(new RequestLoggingFilter());

            context.addFilterDef(filterDef);

            FilterMap filterMap = new FilterMap();
            filterMap.setFilterName("requestLoggingFilter");
            filterMap.addURLPattern("/*");

            context.addFilterMap(filterMap);

            tomcat.start();


            if (!tomcat.getServer().getState().isAvailable()) {
                throw new IllegalStateException("Tomcat server is not available after startup");
            }

            log.info("Gym CRM server started on port {}", SERVER_PORT);

            tomcat.getServer().await();

        } catch (Exception e) {
            log.error("Gym CRM server failed", e);

            throw new IllegalStateException("Failed to start Gym CRM web server", e);

        } finally {
            stopTomcat(tomcat);
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

        applicationContext.register(AppConfig.class);

        var dispatcherServlet = new DispatcherServlet(applicationContext);

        Wrapper wrapper = Tomcat.addServlet(context, SERVLET_NAME, dispatcherServlet);

        wrapper.setLoadOnStartup(1);
        wrapper.addMapping(SERVLET_MAPPING);
    }

    private static void stopTomcat(Tomcat tomcat) {
        try {
            tomcat.stop();
        } catch (LifecycleException exception) {
            log.warn("Failed to stop Tomcat", exception);
        }

        try {
            tomcat.destroy();
        } catch (LifecycleException exception) {
            log.warn("Failed to destroy Tomcat", exception);
        }
    }
}