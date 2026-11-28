package com.training.gym.exception;

import lombok.Getter;

import java.time.Duration;

@Getter
public class AccountTemporarilyLockedException extends RuntimeException {

    private final Duration remainingLockDuration;

    public AccountTemporarilyLockedException(Duration remainingLockDuration) {

        super("Account is temporarily locked");

        this.remainingLockDuration = remainingLockDuration;
    }
}