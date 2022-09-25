package com.gmail.marcosav2010.myfitnesspal.api;

public class LoginException extends Exception {

    LoginException(String message, Throwable cause) {
        super(message, cause);
    }

    LoginException(String message) {
        super(message);
    }
}
