package com.training.gym.exception;

public class IncorrectMethodArgumentException extends RuntimeException{

    public IncorrectMethodArgumentException(String message) {
        super("Incorrect method argument: " + message);
    }
}
