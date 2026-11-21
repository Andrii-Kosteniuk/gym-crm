package com.training.gym.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    public static final String TRANSACTION_ID = "transactionId";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.error("Request validation error occurred", ex);

        List<Map<String, String>> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> Map.of(
                        "field", error.getField(),
                        "message", Objects.requireNonNull(error.getDefaultMessage())
                ))
                .toList();

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "One or more request fields are invalid.");

        problem.setTitle("Request validation failed");
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("errors", errors);
        problem.setProperty(TRANSACTION_ID, request.getHeader(TRANSACTION_ID));

        setTransactionId(problem);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    @ExceptionHandler(IncorrectMethodArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIncorrectMethodArgumentException(HttpServletRequest request) {
        log.error("Invalid method argument error occurred");

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "One or more request parameters are invalid."
        );

        problem.setTitle("Invalid method argument parameters");
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty(TRANSACTION_ID, request.getHeader(TRANSACTION_ID));

        setTransactionId(problem);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleIHttpMessageNotReadableException(HttpServletRequest request) {

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "JSON parse error"
        );

        problem.setTitle("Invalid json parameters");
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty(TRANSACTION_ID, request.getHeader(TRANSACTION_ID));

        setTransactionId(problem);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ProblemDetail> handleBadCredentialsException(HttpServletRequest request) {
        log.error("Invalid credentials error occurred");

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN,
                "Access denied"
        );

        problem.setTitle("Invalid credentials");
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty(TRANSACTION_ID, request.getHeader(TRANSACTION_ID));

        setTransactionId(problem);

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIIllegalArgumentException(HttpServletRequest request, IllegalArgumentException ex) {
        log.error("Invalid input error occurred");

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );

        problem.setTitle("Invalid input");
        problem.setInstance(URI.create(request.getRequestURI()));

        problem.setProperty(TRANSACTION_ID, request.getHeader(TRANSACTION_ID));
        problem.setProperty(TRANSACTION_ID, request.getHeader(TRANSACTION_ID));

        setTransactionId(problem);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleIEntityNotFoundException(HttpServletRequest request, EntityNotFoundException ex) {
        log.error("Not found error occurred ", ex);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );

        problem.setTitle("Not found");
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty(TRANSACTION_ID, request.getHeader(TRANSACTION_ID));

        setTransactionId(problem);

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<ProblemDetail> handleAuthenticationFailedException(HttpServletRequest request) {
        log.error("Error during authentication occurred");

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                "Authentication failed"
        );

        problem.setTitle("You are not authenticated");
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty(TRANSACTION_ID, request.getHeader(TRANSACTION_ID));

        setTransactionId(problem);

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ProblemDetail> handleNullPointerException(HttpServletRequest request) {

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error"
        );

        problem.setTitle("Unexpected server error occurred");
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty(TRANSACTION_ID, request.getHeader(TRANSACTION_ID));

        setTransactionId(problem);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDeniedException(HttpServletRequest request) {
        log.error("Access denied error occurred");

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN,
                "Access denied"
        );

        problem.setTitle("You are not authorized to perform action");
        problem.setType(URI.create("/problems/access-denied"));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty(TRANSACTION_ID, request.getHeader(TRANSACTION_ID));

        setTransactionId(problem);

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ProblemDetail> handleUserAlreadyExistsException(HttpServletRequest request, UserAlreadyExistsException ex) {
        log.error("Access denied error occurred", ex);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                ex.getMessage()
        );

        problem.setTitle("Resource conflict");
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty(TRANSACTION_ID, request.getHeader(TRANSACTION_ID));

        setTransactionId(problem);

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    @ExceptionHandler(BusinessRuleViolationException.class)
    public ResponseEntity<ProblemDetail> handleBusinessRuleViolationException(HttpServletRequest request, BusinessRuleViolationException ex) {
        log.error("Business rule violation error occurred", ex);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );

        problem.setTitle("Business rule violation");
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty(TRANSACTION_ID, request.getHeader(TRANSACTION_ID));

        setTransactionId(problem);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    @ExceptionHandler({
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class,
            MissingRequestHeaderException.class
    })
    public ResponseEntity<ProblemDetail> handleInvalidRequest(HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Required request data is missing or invalid."
        );

        problem.setTitle("Invalid request");
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty(TRANSACTION_ID, request.getHeader(TRANSACTION_ID));

        setTransactionId(problem);

        return ResponseEntity.badRequest()
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpectedException(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error during request", ex);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected server error occurred."
        );

        problem.setTitle("Internal server error");
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty(TRANSACTION_ID, request.getHeader(TRANSACTION_ID));

        setTransactionId(problem);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }


    private static void setTransactionId(ProblemDetail problem) {
        String transactionId = MDC.get(TRANSACTION_ID);

        if (transactionId != null) {
            problem.setProperty(TRANSACTION_ID, transactionId);
        }
    }
}



