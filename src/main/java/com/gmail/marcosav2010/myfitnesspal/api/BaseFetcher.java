package com.gmail.marcosav2010.myfitnesspal.api;

import com.gmail.marcosav2010.json.JSONObject;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.Setter;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class BaseFetcher {

    private static final String COOKIES_KEY = "cookies", HEADERS_KEY = "headers";

    private static final int TIMEOUT = 10 * 1000;

    private static final String BASE_URL_SECURE = "https://www.myfitnesspal.com/";
    private static final String BASE_API_URL = "https://api.myfitnesspal.com/v2/";

    public static String ENCODED_FIELDS, ENCODED_DELIMITER;

    public static final String ITEM_WRAPPER = "item", ITEMS_WRAPPER = "items";

    static {
        try {
            ENCODED_FIELDS = URLEncoder.encode("fields[]", StandardCharsets.UTF_8.toString());
            ENCODED_DELIMITER = URLEncoder.encode(",", StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ignored) {
        }
    }

    @Setter(AccessLevel.PACKAGE)
    private LoginHandler loginHandler;

    @Setter(AccessLevel.PACKAGE)
    private int timeout = TIMEOUT;

    private final Map<String, String> cookies = new HashMap<>();
    private final Map<String, String> headers = new HashMap<>();

    BaseFetcher() {
    }

    BaseFetcher(JSONObject serializedFetcher) {
        this();
        serializedFetcher.getJSONObject(COOKIES_KEY).toMap().forEach((k, v) -> cookies.put(k, (String) v));
        serializedFetcher.getJSONObject(HEADERS_KEY).toMap().forEach((k, v) -> headers.put(k, (String) v));
    }

    private Connection connect(String url) {
        return Jsoup.connect(url).timeout(timeout).cookies(cookies).headers(headers);
    }

    public Document getDocumentNC(String url) throws IOException {
        Connection.Response resp = Jsoup.connect(url).timeout(timeout).headers(headers).execute();
        return resp.parse();
    }

    public JSONObject json(String url) throws IOException {
        Connection.Response resp = get(url);
        cookies.putAll(resp.cookies());
        return new JSONObject(resp.body());
    }

    void login(@NonNull String url, String username, String password) throws LoginException {
        if (loginHandler == null)
            throw new IllegalStateException("There's no LoginHandler defined");

        Map<String, String> cookies = loginHandler.login(url, username, password);
        this.cookies.putAll(cookies);
    }

    private Connection.Response get(String url) throws IOException {
        return connect(url)
                .ignoreContentType(true)
                .execute();
    }

    JSONObject toJSONObject() {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put(COOKIES_KEY, cookies);
        jsonObject.put(HEADERS_KEY, headers);

        return jsonObject;
    }

    void addHeader(String k, String v) {
        headers.put(k, v);
    }

    public String getURL(String path, Object... params) {
        return BASE_URL_SECURE + String.format(path, params);
    }

    public String getApiURL(String path) {
        return BASE_API_URL + path;
    }
}
