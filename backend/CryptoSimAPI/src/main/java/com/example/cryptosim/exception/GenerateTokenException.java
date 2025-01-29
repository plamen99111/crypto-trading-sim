package com.example.cryptosim.exception;

public class GenerateTokenException extends RuntimeException {
    public GenerateTokenException(String message) {
        super(message);
    }

    public GenerateTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}

