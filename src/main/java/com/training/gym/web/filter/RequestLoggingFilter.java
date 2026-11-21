package com.training.gym.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RequestLoggingFilter extends OncePerRequestFilter {

    public static final String TRANSACTION_ID = "transactionId";
    public static final String HEADER_NAME = "X-Transaction-ID";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {

        String transactionId = resolveTransactionId(request);

        String previousTransactionId = MDC.get(TRANSACTION_ID);
        MDC.put(TRANSACTION_ID, transactionId);

        response.setHeader(HEADER_NAME, transactionId);

        String method = request.getMethod();
        String path = request.getRequestURI();

        long startTime = System.nanoTime();

        try {
            log.info("HTTP request started: method={}, path={}", method, path);

            chain.doFilter(request, response);

        } finally {
            long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);

            int status = response.getStatus();

            log.info("HTTP request completed: method={}, path={}, status={}, outcome={}, durationMs={}",
                    method,
                    path,
                    status,
                    resolveOutcome(status),
                    durationMs
            );

            if (previousTransactionId == null) {
                MDC.remove(TRANSACTION_ID);
            } else {
                MDC.put(TRANSACTION_ID, previousTransactionId);
            }
        }
    }

    private String resolveTransactionId(HttpServletRequest request) {
        String incomingId = request.getHeader(HEADER_NAME);

        if (incomingId != null) {
            try {
                return UUID.fromString(incomingId.trim()).toString();
            } catch (IllegalArgumentException ex) {
                log.debug("Invalid transaction ID received; generating a new ID");
            }
        }

        return UUID.randomUUID().toString();
    }

    private String resolveOutcome(int status) {
        if (status >= 500) {
            return "SERVER_ERROR";
        }

        if (status >= 400) {
            return "CLIENT_ERROR";
        }

        if (status >= 300) {
            return "REDIRECTION";
        }

        return "SUCCESS";
    }
}