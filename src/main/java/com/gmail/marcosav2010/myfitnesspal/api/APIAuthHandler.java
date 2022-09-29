package com.gmail.marcosav2010.myfitnesspal.api;

import com.gmail.marcosav2010.json.JSONObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
class APIAuthHandler implements APIHeaderProvider {

    private static final String
            HEADERS_KEY = "headers",
            AUTH_TIME_KEY = "authTime",
            AUTO_REAUTHENTICATE_KEY = "autoReauthenticate",
            EXPIRES_IN_KEY = "expires_in";
    private static final String USER_AUTH_DATA = "user/auth_token/?refresh=true";

    private final BaseFetcher fetcher;

    private final Map<String, String> headers = new HashMap<>();
    @Getter
    @Setter
    private boolean autoReauthenticate = true;

    private long authTime;
    private long expiresIn;

    APIAuthHandler(JSONObject serialized, BaseFetcher fetcher) {
        this(fetcher);

        serialized.getJSONObject(HEADERS_KEY).toMap().forEach((k, v) -> headers.put(k, (String) v));
        authTime = serialized.getLong(AUTH_TIME_KEY);
        expiresIn = serialized.getLong(EXPIRES_IN_KEY);
        autoReauthenticate = serialized.getBoolean(AUTO_REAUTHENTICATE_KEY);
    }

    String auth() throws IOException {
        JSONObject authResponse = fetcher.json(fetcher.getURL(USER_AUTH_DATA));

        authTime = System.currentTimeMillis();

        var userId = authResponse.getString("user_id");
        String accessToken = authResponse.getString("access_token");
        String tokenType = authResponse.getString("token_type");
        expiresIn = authResponse.getLong("expires_in") * 1000;

        headers.put("Authorization", tokenType + " " + accessToken);
        headers.put("mfp-client-id", "mfp-main-js");
        headers.put("Accept", "application/json");
        headers.put("mfp-user-id", userId);

        return userId;
    }

    public Map<String, String> getHeaders() throws IOException {
        if (autoReauthenticate && shouldReauthenticate())
            auth();
        return headers;
    }

    private boolean shouldReauthenticate() {
        return (System.currentTimeMillis() - authTime) >= expiresIn;
    }

    JSONObject toJSONObject() {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put(AUTO_REAUTHENTICATE_KEY, autoReauthenticate);
        jsonObject.put(AUTH_TIME_KEY, authTime);
        jsonObject.put(EXPIRES_IN_KEY, expiresIn);
        jsonObject.put(HEADERS_KEY, headers);

        return jsonObject;
    }
}
