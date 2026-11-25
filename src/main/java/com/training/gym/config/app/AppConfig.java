package com.training.gym.config.app;

import com.training.gym.config.persistence.DataSourceConfig;
import com.training.gym.config.persistence.FlywayConfig;
import com.training.gym.config.persistence.JpaConfig;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@ComponentScan(basePackages = "com.training.gym")
@Configuration(proxyBeanMethods = false)
@EnableWebMvc
@Import({
        ApplicationPropertiesConfig.class,
        DataSourceConfig.class,
        FlywayConfig.class,
        JpaConfig.class
})
public class AppConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        registry.addResourceHandler("/openapi.yaml")
                .addResourceLocations("classpath:/static/");

    }

}
