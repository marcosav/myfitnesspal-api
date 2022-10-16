package com.gmail.marcosav2010.myfitnesspal.api;

import com.gmail.marcosav2010.json.JSONObject;
import com.gmail.marcosav2010.myfitnesspal.api.diary.Diary;
import com.gmail.marcosav2010.myfitnesspal.api.user.UserData;
import com.gmail.marcosav2010.myfitnesspal.api.user.UserFetcher;
import lombok.Getter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class MFPSession implements IMFPSession {

    private static final String
            AUTH_HANDLER_KEY = "authHandler",
            FETCHER_KEY = "fetcher",
            USER_METADATA_KEY = "userMetadata",
            CREATION_TIME_KEY = "creationTime";
    private static final String LOGIN_PATH = "account/login";

    /*private static final String NUTRIENT_GOALS_DATA = "nutrient-goals?date=%s";
    private static final String MEASUREMENTS_DATA = "incubator/measurements?entry_date=%s"; //most_recent=true */

    private static final long ESTIMATED_SESSION_DURATION = 28 * 24 * 3600 * 1000L;

    private final BaseFetcher fetcher;
    private final APIAuthHandler authHandler;

    private UserData userData;
    private Diary diary;

    @Getter
    private long creationTime;

    private MFPSession() {
        fetcher = new BaseFetcher();
        authHandler = new APIAuthHandler(fetcher);
    }

    private MFPSession(JSONObject serialized) {
        fetcher = new BaseFetcher(serialized.getJSONObject(FETCHER_KEY));
        authHandler = new APIAuthHandler(serialized.getJSONObject(AUTH_HANDLER_KEY), fetcher);
        creationTime = serialized.getLong(CREATION_TIME_KEY);
        userData = new UserData(serialized.getJSONObject(USER_METADATA_KEY));

        fetcher.setHeaderProvider(authHandler);
    }

    public String encode() {
        return fromJSONObject(toJSONObject());
    }

    public boolean shouldReLog() {
        return (System.currentTimeMillis() - creationTime) >= ESTIMATED_SESSION_DURATION;
    }

    private MFPSession login(String username, String password) throws LoginException {
        creationTime = System.currentTimeMillis();

        fetcher.login(fetcher.getURL(LOGIN_PATH), username, password);

        String userId;

        try {
            userId = authHandler.auth();
        } catch (Exception ex) {
            throw new RuntimeException("There was an error while requesting auth token: " + ex.getMessage(), ex);
        }

        try {
            loadData(userId);
        } catch (IOException ex) {
            throw new RuntimeException("There was an error while loading user data: " + ex.getMessage(), ex);
        }

        return this;
    }

    private void loadData(String userId) throws IOException {
        UserFetcher userFetcher = new UserFetcher(fetcher);
        userData = userFetcher.load(userId);
    }

    public IMFPSession setTimeout(int timeout) {
        fetcher.setTimeout(timeout);
        return this;
    }

    private MFPSession setLoginHandler(LoginHandler loginHandler) {
        fetcher.setLoginHandler(loginHandler);
        return this;
    }

    public UserData toUser() {
        return userData;
    }

    public Diary toDiary() {
        if (diary == null)
            diary = new Diary(userData, fetcher);
        return diary;
    }

    private JSONObject toJSONObject() {
        JSONObject json = new JSONObject();

        json.put(CREATION_TIME_KEY, creationTime);
        json.put(AUTH_HANDLER_KEY, authHandler.toJSONObject());
        json.put(FETCHER_KEY, fetcher.toJSONObject());
        json.put(USER_METADATA_KEY, userData.toJSONObject());

        return json;
    }

    private static JSONObject toJSONObject(String encoded) {
        return new JSONObject(new String(Base64.getUrlDecoder().decode(encoded), StandardCharsets.UTF_8));
    }

    private static String fromJSONObject(JSONObject json) {
        return new String(Base64.getUrlEncoder().encode(json.toString().getBytes(StandardCharsets.UTF_8)));
    }

    public static IMFPSession create(String username, String password, LoginHandler loginHandler) throws LoginException {
        return new MFPSession().setLoginHandler(loginHandler).login(username, password);
    }

    public static IMFPSession create(LoginHandler loginHandler) throws LoginException {
        return new MFPSession().setLoginHandler(loginHandler).login(null, null);
    }

    public static IMFPSession from(String json) {
        return new MFPSession(toJSONObject(json));
    }

    public static IMFPSession from(String json, LoginHandler loginHandler) {
        return new MFPSession(toJSONObject(json)).setLoginHandler(loginHandler);
    }
}
