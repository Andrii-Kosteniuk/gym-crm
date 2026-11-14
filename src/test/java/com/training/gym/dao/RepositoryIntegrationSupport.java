package com.training.gym.dao;

import com.training.gym.model.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Properties;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = RepositoryIntegrationSupport.TestConfig.class)
@Transactional
abstract class RepositoryIntegrationSupport {
    @PersistenceContext
    protected EntityManager em;

    @Configuration
    @EnableTransactionManagement
    @ComponentScan(basePackageClasses = JpaUserRepository.class, useDefaultFilters = false,
            includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {JpaUserRepository.class, JpaTraineeRepository.class, JpaTrainerRepository.class, JpaTrainingRepository.class, JpaTrainingTypeRepository.class}))
    static class TestConfig {
        @Bean
        DataSource dataSource() {
            DriverManagerDataSource ds = new DriverManagerDataSource();
            ds.setDriverClassName("org.h2.Driver");
            ds.setUrl("jdbc:h2:mem:fresh_aaa_tests;DB_CLOSE_DELAY=-1");
            ds.setUsername("sa");
            return ds;
        }

        @Bean
        LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
            var bean = new LocalContainerEntityManagerFactoryBean();
            bean.setDataSource(dataSource);
            bean.setPackagesToScan("com.training.gym.model");
            bean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
            Properties p = new Properties();
            p.setProperty("hibernate.hbm2ddl.auto", "create-drop");
            p.setProperty("hibernate.show_sql", "false");
            bean.setJpaProperties(p);
            return bean;
        }

        @Bean
        PlatformTransactionManager transactionManager(jakarta.persistence.EntityManagerFactory factory) {
            return new JpaTransactionManager(factory);
        }
    }

    protected User user(String first, String last, String username) {
        return User.builder().firstName(first).lastName(last).userName(username).password("encoded-password").active(true).build();
    }

    protected TrainingType type(String name) {
        TrainingType type = TrainingType.builder().name(name).build();
        em.persist(type);
        em.flush();
        return type;
    }

    protected Trainee trainee(String username) {
        Trainee t = Trainee.builder().user(user("James", "Bond", username)).dateOfBirth(LocalDate.of(1990, 4, 13)).build();
        em.persist(t);
        em.flush();
        return t;
    }

    protected Trainer trainer(String username, TrainingType type) {
        Trainer t = Trainer.builder().user(user("Sara", "Connor", username)).specialization(type).build();
        em.persist(t);
        em.flush();
        return t;
    }

    protected Training training(Trainee trainee, Trainer trainer, TrainingType type) {
        Training t = Training.builder().trainee(trainee).trainer(trainer).trainingType(type).trainingName("Fitness session").trainingDate(LocalDate.of(2026, 11, 15)).trainingDuration(Duration.ofMinutes(45)).build();
        em.persist(t);
        em.flush();
        return t;
    }

    protected void reload() {
        em.flush();
        em.clear();
    }
}
