package com.gmail.marcosav2010.myfitnesspal.api;

public class LoginException extends Exception {

    public LoginException(String message, Throwable cause) {
        super(message, cause);
    }

    public LoginException(String message) {
        super(message);
    }
}
