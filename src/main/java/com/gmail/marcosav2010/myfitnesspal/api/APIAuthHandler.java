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

    private static final String HEADERS_KEY = "headers", AUTH_TIME_KEY = "authTime", AUTO_REAUTHENTICATE_KEY = "autoReauthenticate";
    private static final String USER_AUTH_DATA = "user/auth_token/?refresh=true";

    private static final long ESTIMATED_REAUTH_TIME = 2 * 3600 * 1000;

    private final BaseFetcher fetcher;

    private final Map<String, String> headers = new HashMap<>();
    @Getter
    @Setter
    private boolean autoReauthenticate = true;

    private long authTime;

    APIAuthHandler(JSONObject serialized, BaseFetcher fetcher) {
        this(fetcher);

        serialized.getJSONObject(HEADERS_KEY).toMap().forEach((k, v) -> headers.put(k, (String) v));
        authTime = serialized.getLong(AUTH_TIME_KEY);
        autoReauthenticate = serialized.getBoolean(AUTO_REAUTHENTICATE_KEY);
    }

    String auth() throws IOException {
        JSONObject authToken = fetcher.json(fetcher.getURL(USER_AUTH_DATA));

        authTime = System.currentTimeMillis();

        var userId = authToken.getString("user_id");
        String accessToken = authToken.getString("access_token");
        String tokenType = authToken.getString("token_type");

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
        return (System.currentTimeMillis() - authTime) >= ESTIMATED_REAUTH_TIME;
    }

    JSONObject toJSONObject() {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put(AUTO_REAUTHENTICATE_KEY, autoReauthenticate);
        jsonObject.put(AUTH_TIME_KEY, authTime);
        jsonObject.put(HEADERS_KEY, headers);

        return jsonObject;
    }
}
