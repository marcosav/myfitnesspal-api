package com.gmail.marcosav2010.myfitnesspal.api;

import com.gmail.marcosav2010.json.JSONArray;
import com.gmail.marcosav2010.json.JSONException;
import com.gmail.marcosav2010.json.JSONObject;
import com.gmail.marcosav2010.myfitnesspal.api.lister.FoodFormatter;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MFPSession {

    private static final int TIMEOUT = 10 * 1000;
    private static final long ESTIMATED_SESSION_EXPIRATION_TIME = 2 * 3600 * 1000;

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yy-MM-dd");

    private static final String BASE_URL_SECURE = "https://www.myfitnesspal.com/";
    private static final String BASE_API_URL = "https://api.myfitnesspal.com/";

    private static final String LOGIN_PATH = "account/login";
    private static final String USER_AUTH_DATA = "user/auth_token/?refresh=true";
    private static final String USER_METADATA = "v2/users/%s?";
    private static final String FOR_DATE = "food/diary/%s?date=%s";

    private static final String LOGIN_URL = getURL(LOGIN_PATH);

    private static final String[] METADATA_QUERY_FIELDS = {"diary_preferences", "goal_preferences", "unit_preferences",
            "paid_subscriptions", "account", "goal_displays", "location_preferences", "system_data", "profiles",
            "step_sources"};

    private static final FoodFormatter DEFAULT_FF = Food::new;

    private final Map<String, String> cookies = new HashMap<>();

    private JSONObject authToken;
    private JSONObject userMetadata;

    @SuppressWarnings("unused")
    private String authenticityToken, userId, accessToken, tokenType;
    private JSONArray mealNames;
    private final Set<Integer> defaultMealIndexes = new HashSet<>();

    @Getter
    private long creationTime;

    @Getter
    private String username;

    public String encode() {
        JSONObject json = new JSONObject();
        json.put("username", username);
        json.put("creationTime", creationTime);
        json.put("cookies", cookies);
        json.put("mealNames", mealNames);
        json.put("defaultMealIndexes", defaultMealIndexes);
        return json.toString().replaceAll("\"", "__");
    }

    private void parse(String jsonString) {
        JSONObject json = new JSONObject(jsonString.replaceAll("__", "\""));
        username = json.getString("username");
        creationTime = json.getLong("creationTime");
        mealNames = json.getJSONArray("mealNames");
        json.getJSONArray("defaultMealIndexes").toList().forEach(e -> defaultMealIndexes.add((Integer) e));
        cookies.clear();
        json.getJSONObject("cookies").toMap().forEach((k, v) -> cookies.put(k, (String) v));
    }

    public boolean shouldRelog() {
        return (System.currentTimeMillis() - creationTime) >= ESTIMATED_SESSION_EXPIRATION_TIME;
    }

    private void login(String username, String password) throws IOException {
        this.username = username;

        Response res = Jsoup.connect(LOGIN_URL)
                .timeout(TIMEOUT)
                .method(Method.GET)
                .execute();

        Document doc = res.parse();
        authenticityToken = doc.selectFirst("input[name='authenticity_token']").val();
        res = Jsoup.connect(LOGIN_URL).data("username", username).data("password", password).method(Method.POST).execute();
        cookies.putAll(res.cookies());

        try {
            loadUserMetadata();

        } catch (JSONException ex) {
            throw new RuntimeException("There was an error related to JSON, probably wrong user/password");

        } catch (IOException ex) {
            throw new RuntimeException("There was an error while logging in: " + ex.getMessage());
        }
    }

    private Set<Integer> getDefaultMeals() {
        return defaultMealIndexes;
    }

    private Set<Integer> parseMeals(String meals) {
        meals = meals.replaceAll("[^-?0-9]+", "");
        try {
            return Stream.of(meals.trim().split(""))
                    .filter(n -> !n.isEmpty())
                    .map(Integer::parseInt)
                    .collect(Collectors.toSet());
        } catch (Exception ex) {
            return getDefaultMeals();
        }
    }

    public List<Meal> getDayFood(Date date) throws IOException {
        return getDayFood(date, getDefaultMeals());
    }

    public List<Meal> getDayFood(Date date, Set<Integer> meals) throws IOException {
        return getDayFood(date, meals, DEFAULT_FF);
    }

    public List<Meal> getDayFood(Date date, String meals) throws IOException {
        return getDayFood(date, meals, DEFAULT_FF);
    }

    public List<Meal> getDayFood(Date date, String meals, FoodFormatter fa) throws IOException {
        return getDayFood(date, parseMeals(meals), fa);
    }

    public List<Meal> getDayFood(Date date, FoodFormatter fa) throws IOException {
        return getDayFood(date, getDefaultMeals(), fa);
    }

    public List<Meal> getDayFood(Date date, Set<Integer> requestedMeals, FoodFormatter fa) throws IOException {
        String foodDateURL = getURLForDate(date);

        Document foodDoc = Jsoup.connect(foodDateURL).cookies(cookies).get();
        Elements mealHeaders = foodDoc.getElementsByClass("meal_header");

        List<Meal> mealsList = new LinkedList<>();
        int mealNo = 0;

        for (Element e : mealHeaders) {
            if (!requestedMeals.contains(mealNo++))
                continue;

            String mealTitle = e.selectFirst("td[class='first alt']").text();
            Element cell = e.nextElementSibling();

            String[] foodContent, info, amount;
            String name, brand, unit;
            float number;

            Meal meal = new Meal(mealTitle);

            while (!cell.classNames().contains("bottom")) {
                Element foodField = cell.selectFirst("a[class='js-show-edit-food']");
                if (foodField == null)
                    break;

                foodContent = foodField.text().trim().split(", ");
                info = foodContent[0].split(" - ");
                name = info.length > 1 ? info[1] : info[0];
                brand = info.length == 1 ? "" : info[0];
                amount = foodContent[1].split("\\s+");
                number = Float.parseFloat(amount[0]);
                unit = amount[1];

                Food food = fa.get(name, brand, unit, number);

                meal.getFood().add(food);

                cell = cell.nextElementSibling();
            }

            mealsList.add(meal);
        }

        return mealsList;
    }

    public List<Meal> getDayRangeFood(Set<Date> dates, String requestedMeals, FoodFormatter fa) {
        return getDayRangeFood(dates, parseMeals(requestedMeals), fa);
    }

    public List<Meal> getDayRangeFood(Set<Date> dates, Set<Integer> requestedMeals, FoodFormatter fa) {
        return dates.stream().flatMap(d -> {
            try {
                return getDayFood(d, requestedMeals, fa).stream();
            } catch (IOException e) {
                e.printStackTrace();
                return Stream.empty();
            }
        }).collect(Collectors.toList());
    }

    private void loadUserMetadata() throws IOException {
        Connection c = Jsoup.connect(getURL(USER_AUTH_DATA))
                .timeout(TIMEOUT)
                .ignoreContentType(true)
                .cookies(cookies)
                .method(Method.GET);

        String content = c
                .execute()
                .parse()
                .body()
                .html();

        authToken = new JSONObject(content);

        creationTime = System.currentTimeMillis();
        userId = authToken.getString("user_id");
        accessToken = authToken.getString("access_token");
        tokenType = authToken.getString("token_type");

        String queryUrl = getApiURL(userId) + getFieldQueryURL(METADATA_QUERY_FIELDS);

        c = Jsoup.connect(queryUrl)
                .timeout(TIMEOUT)
                .ignoreContentType(true)
                .header("Authorization", tokenType + " " + accessToken)
                .header("mfp-client-id", "mfp-main-js")
                .header("Accept", "application/json")
                .header("mfp-user-id", userId)
                .method(Method.GET);

        content = c
                .execute()
                .parse()
                .body()
                .html();

        userMetadata = new JSONObject(content);

        mealNames = userMetadata
                .getJSONObject("item")
                .getJSONObject("diary_preferences")
                .getJSONArray("meal_names");

        for (int i = 0; i < mealNames.length(); i++)
            defaultMealIndexes.add(i);
    }

    private String getURLForDate(Date date) {
        return getURL(FOR_DATE, username, DATE_FORMAT.format(date));
    }

    private static String getFieldQueryURL(String... args) throws UnsupportedEncodingException {
        StringBuilder queryFieldsUrl = new StringBuilder();
        String fEnc = URLEncoder.encode("fields[]", StandardCharsets.UTF_8.toString());

        for (String f : args)
            queryFieldsUrl.append(fEnc).append("=").append(f).append("&");

        queryFieldsUrl = new StringBuilder(queryFieldsUrl.substring(0, queryFieldsUrl.length() - 1));

        return queryFieldsUrl.toString();
    }

    private static String getURL(String path, Object... params) {
        return BASE_URL_SECURE + String.format(path, params);
    }

    private static String getApiURL(Object... params) {
        return BASE_API_URL + String.format(MFPSession.USER_METADATA, params);
    }

    public static MFPSession create(String username, String password) throws IOException {
        MFPSession s = new MFPSession();
        s.login(username, password);
        return s;
    }

    public static MFPSession from(String json) {
        MFPSession s = new MFPSession();
        s.parse(json);
        return s;
    }
}
