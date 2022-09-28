package com.gmail.marcosav2010.myfitnesspal.api;

import com.gmail.marcosav2010.json.JSONObject;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
public class BaseFetcher {

    private static final String COOKIES_KEY = "cookies";

    private static final int TIMEOUT = 10 * 1000;

    private static final String BASE_URL_SECURE = "https://www.myfitnesspal.com/";
    private static final String BASE_API_URL = "https://api.myfitnesspal.com/v2/";

    private static final String SESSION_TOKEN_COOKIE = "__Secure-next-auth.session-token";
    private static final String MFP_SESSION_COOKIE = "_mfp_session";

    public static String ENCODED_FIELDS = URLEncoder.encode("fields[]", StandardCharsets.UTF_8);
    public static String ENCODED_DELIMITER = URLEncoder.encode(",", StandardCharsets.UTF_8);

    @Setter(AccessLevel.PACKAGE)
    private LoginHandler loginHandler;
    @Setter(AccessLevel.PACKAGE)
    private APIHeaderProvider headerProvider;

    @Setter(AccessLevel.PACKAGE)
    private int timeout = TIMEOUT;

    private final Map<String, String> cookies = new HashMap<>();

    BaseFetcher(JSONObject serialized) {
        serialized.getJSONObject(COOKIES_KEY).toMap().forEach((k, v) -> cookies.put(k, (String) v));
    }

    private Connection connect(String url) throws IOException {
        return Jsoup.connect(url).timeout(timeout).cookies(cookies).headers(getHeaders());
    }

    private Map<String, String> getHeaders() throws IOException {
        return headerProvider == null ? Map.of() : headerProvider.getHeaders();
    }

    public Document getDocumentNoHeaders(String url) throws IOException {
        var resp = Jsoup.connect(url).timeout(timeout).cookies(cookies).execute();
        return resp.parse();
    }

    public JSONObject json(String url) throws IOException {
        var resp = get(url);

        var respCookies = resp.cookies();
        if (respCookies.containsKey(MFP_SESSION_COOKIE))
            cookies.put(MFP_SESSION_COOKIE, respCookies.get(MFP_SESSION_COOKIE));

        return new JSONObject(resp.body());
    }

    void login(@NonNull String url, String username, String password) throws LoginException {
        if (loginHandler == null)
            throw new IllegalStateException("There's no LoginHandler defined");

        var loginCookies = loginHandler.login(url, username, password);
        var sessionToken = loginCookies.get(SESSION_TOKEN_COOKIE);

        cookies.put(SESSION_TOKEN_COOKIE, sessionToken);
    }

    private Connection.Response get(String url) throws IOException {
        return connect(url)
                .ignoreContentType(true)
                .execute();
    }

    JSONObject toJSONObject() {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put(COOKIES_KEY, cookies);

        return jsonObject;
    }

    public String getURL(String path, Object... params) {
        return BASE_URL_SECURE + String.format(path, params);
    }

    public String getApiURL(String path) {
        return BASE_API_URL + path;
    }
}
