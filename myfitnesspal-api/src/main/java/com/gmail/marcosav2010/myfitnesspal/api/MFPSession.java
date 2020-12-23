package com.gmail.marcosav2010.myfitnesspal.api;

import com.gmail.marcosav2010.json.JSONException;
import com.gmail.marcosav2010.json.JSONObject;
import com.gmail.marcosav2010.myfitnesspal.api.food.diary.Diary;
import com.gmail.marcosav2010.myfitnesspal.api.user.UserData;
import com.gmail.marcosav2010.myfitnesspal.api.user.UserFetcher;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MFPSession implements IMFPSession {

    private static final long ESTIMATED_SESSION_EXPIRATION_TIME = 2 * 3600 * 1000;

    private static final String LOGIN_PATH = "account/login";
    private static final String LOGOUT_PATH = "account/logout";
    private static final String USER_AUTH_DATA = "user/auth_token/?refresh=true";

    /*private static final String NUTRIENT_GOALS_DATA = "nutrient-goals?date=%s";
    private static final String MEASUREMENTS_DATA = "incubator/measurements?entry_date=%s";*/

    private BaseFetcher fetcher = new BaseFetcher();

    private final String LOGIN_URL = fetcher.getURL(LOGIN_PATH);

    private UserData userData;
    private Diary diary;

    @SuppressWarnings("unused")
    private String userId;

    @Getter
    private long creationTime;

    public String encode() {
        JSONObject json = new JSONObject();

        json.put("creationTime", creationTime);
        json.put("fetcher", fetcher.toJSONObject());
        json.put("userMetadata", userData.toJSONObject());

        return new String(Base64.getUrlEncoder().encode(json.toString().getBytes(StandardCharsets.UTF_8)));
    }

    private void decode(String encoded) {
        JSONObject json = new JSONObject(new String(Base64.getUrlDecoder().decode(encoded), StandardCharsets.UTF_8));

        creationTime = json.getLong("creationTime");
        userData = new UserData(json.getJSONObject("userMetadata"));
        fetcher = new BaseFetcher(json.getJSONObject("fetcher"));

        init();
    }

    public boolean shouldReLog() {
        return (System.currentTimeMillis() - creationTime) >= ESTIMATED_SESSION_EXPIRATION_TIME;
    }

    private void login(String username, String password) throws IOException {
        String authenticityToken = fetcher.findAuthenticityToken(fetcher.connect(LOGIN_URL).get());

        Map<String, String> credentials = new HashMap<>();

        credentials.put("username", username);
        credentials.put("password", password);
        credentials.put("authenticity_token", authenticityToken);

        fetcher.login(LOGIN_URL, credentials);

        try {
            authenticate();

        } catch (JSONException ex) {
            throw new RuntimeException("There was an error related to JSON, probably wrong user/password", ex);

        } catch (IOException ex) {
            throw new RuntimeException("There was an error while logging in: " + ex.getMessage(), ex);
        }
    }

    private void authenticate() throws IOException {
        JSONObject authToken = fetcher.json(fetcher.getURL(USER_AUTH_DATA));

        creationTime = System.currentTimeMillis();

        userId = authToken.getString("user_id");
        String accessToken = authToken.getString("access_token");
        String tokenType = authToken.getString("token_type");

        fetcher.addHeader("Authorization", tokenType + " " + accessToken);
        fetcher.addHeader("mfp-client-id", "mfp-main-js");
        fetcher.addHeader("Accept", "application/json");
        fetcher.addHeader("mfp-user-id", userId);

        load();
    }

    private void load() throws IOException {
        UserFetcher userFetcher = new UserFetcher(fetcher);
        userData = userFetcher.load(userId);

        init();
    }

    private void init() {
        diary = new Diary(userData, fetcher);
    }

    public IMFPSession setTimeout(int timeout) {
        fetcher.setTimeout(timeout);
        return this;
    }

    public UserData toUser() {
        return userData;
    }

    public Diary toDiary() {
        return diary;
    }

    public void logout() throws IOException {
        fetcher.json(fetcher.getURL(LOGOUT_PATH));
    }

    public static IMFPSession create(String username, String password) throws IOException {
        MFPSession s = new MFPSession();
        s.login(username, password);
        return s;
    }

    public static IMFPSession from(String json) {
        MFPSession s = new MFPSession();
        s.decode(json);
        return s;
    }
}
