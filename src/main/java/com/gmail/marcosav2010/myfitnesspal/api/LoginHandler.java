package com.gmail.marcosav2010.myfitnesspal.api;

import java.util.Map;

public interface LoginHandler {

    Map<String, String> login(String url, String username, String password) throws LoginException;
}
