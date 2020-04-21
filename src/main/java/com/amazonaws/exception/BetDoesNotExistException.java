package com.amazonaws.exception;

public class BetDoesNotExistException extends IllegalArgumentException {

    public BetDoesNotExistException(String message) {
        super(message);
    }
}
