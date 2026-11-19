package com.training.gym.config.web;

import org.springframework.context.annotation.*;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ValidationConfig implements WebMvcConfigurer {

    @Bean
    @Primary
    public LocalValidatorFactoryBean mvcValidator() {
        return new LocalValidatorFactoryBean();
    }

    @Bean
    public Validator getValidator(LocalValidatorFactoryBean mvcValidator) {
        return mvcValidator;
    }
}