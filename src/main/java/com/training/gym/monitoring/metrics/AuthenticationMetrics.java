package com.training.gym.monitoring.metrics;

import com.training.gym.exception.AuthenticationFailedException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(0)
public class AuthenticationMetrics {

    private final Counter authenticationSuccess;
    private final Counter authenticationFailure;


    public AuthenticationMetrics(MeterRegistry registry) {

        authenticationSuccess = counter(
                registry, "success");

        authenticationFailure = counter(
                registry, "failure");
    }

    private Counter counter(MeterRegistry registry, String outcome) {

        return Counter.builder("gym.authentication.attempts")
                .tag("outcome", outcome)
                .register(registry);
    }

    @Around("""
            execution(* com.training.gym.service.auth.AuthenticationServiceImpl.authenticate(..))
            """)
    public Object authentication(ProceedingJoinPoint joinPoint) throws Throwable {

        try {
            Object result = joinPoint.proceed();

            authenticationSuccess.increment();

            return result;

        } catch (AuthenticationFailedException ex) {
            authenticationFailure.increment();
            throw ex;
        }
    }
}
