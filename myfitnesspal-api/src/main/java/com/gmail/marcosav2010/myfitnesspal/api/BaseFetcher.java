package com.gmail.marcosav2010.myfitnesspal.api;

import com.gmail.marcosav2010.json.JSONObject;
import lombok.AccessLevel;
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
    private int timeout = TIMEOUT;

    private Map<String, String> cookies = new HashMap<>();
    private final Map<String, String> headers = new HashMap<>();

    BaseFetcher() {
    }

    BaseFetcher(JSONObject serializedFetcher) {
        serializedFetcher.getJSONObject(COOKIES_KEY).toMap().forEach((k, v) -> cookies.put(k, (String) v));
        serializedFetcher.getJSONObject(HEADERS_KEY).toMap().forEach((k, v) -> headers.put(k, (String) v));
    }

    String findAuthenticityToken(Document document) {
        return document.selectFirst("input[name='authenticity_token']").val();
    }

    Connection connectNC(String url) {
        return Jsoup.connect(url).timeout(timeout);
    }

    public Connection connect(String url) {
        return connectNC(url).cookies(cookies);
    }

    Connection connect(String url, Map<String, String> headers) {
        return connectNC(url).headers(headers);
    }

    public JSONObject json(String url) throws IOException {
        return new JSONObject(connect(url).headers(headers).ignoreContentType(true).get().text());
    }

    public JSONObject json(String url, Map<String, String> headers) throws IOException {
        return new JSONObject(connect(url, headers).ignoreContentType(true).get().text());
    }

    void login(String url, Map<String, String> credentials) throws IOException {
        cookies = post(url, credentials).cookies();
    }

    private Connection.Response post(String url, Map<String, String> data) throws IOException {
        return connect(url)
                .data(data)
                .method(Connection.Method.POST)
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
