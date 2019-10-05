package com.gmail.marcosav2010.myfitnesspal.api;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.gmail.marcosav2010.myfitnesspal.api.lister.FoodFormater;
import com.gmail.marcosav2010.myfitnesspal.json.JSONArray;
import com.gmail.marcosav2010.myfitnesspal.json.JSONException;
import com.gmail.marcosav2010.myfitnesspal.json.JSONObject;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MFPSession {

	private static DateFormat DATE_FORMAT = new SimpleDateFormat("yy-MM-dd");

	private static String BASE_URL_SECURE = "https://www.myfitnesspal.com/";
	private static String BASE_API_URL = "https://api.myfitnesspal.com/";

	private static String LOGIN_PATH = "account/login";
	private static String USER_AUTH_DATA = "user/auth_token/?refresh=true";
	private static String USER_METADATA = "v2/users/%s?";
	private static String FOR_DATE = "food/diary/%s?date=%s";

	private static String LOGIN_URL = getURL(LOGIN_PATH);

	private static String[] METADATA_QUERY_FIELDS = { "diary_preferences", "goal_preferences", "unit_preferences", "paid_subscriptions", "account",
			"goal_displays", "location_preferences", "system_data", "profiles", "step_sources" };

	private static FoodFormater DEFAULT_FF = (name, brand, unit, number) -> new Food(name, brand, unit, number);

	private final Map<String, String> cookies = new HashMap<>();

	private JSONObject authToken;
	private JSONObject userMetadata;

	@SuppressWarnings("unused")
	private String authenticityToken, userId, accessToken, tokenType;
	private JSONArray mealNames;
	private Set<Integer> defaultMealIndexes = new HashSet<>();

	private long sessionExpirationTime;

	@Getter
	private String username;

	public String encode() {
		JSONObject json = new JSONObject();
		json.put("username", username);
		json.put("sessionExpirationTime", sessionExpirationTime);
		json.put("cookies", cookies);
		json.put("mealNames", mealNames);
		json.put("defaultMealIndexes", defaultMealIndexes);
		return json.toString().replaceAll("\"", "__");
	}

	private void parse(String jsonString) {
		JSONObject json = new JSONObject(jsonString.replaceAll("__", "\""));
		username = json.getString("username");
		sessionExpirationTime = json.getLong("sessionExpirationTime");
		mealNames = json.getJSONArray("mealNames");
		json.getJSONArray("defaultMealIndexes").toList().forEach(e -> defaultMealIndexes.add((Integer) e));
		cookies.clear();
		json.getJSONObject("cookies").toMap().forEach((k, v) -> cookies.put(k, (String) v));
	}

	private boolean needsRelog() {
		return sessionExpirationTime < System.currentTimeMillis();
	}

	private void login(String username, String password) throws IOException {
		this.username = username;

		Response res = Jsoup.connect(LOGIN_URL).method(Method.GET).execute();
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
			return Stream.of(meals.trim().split("")).map(n -> Integer.parseInt(n)).collect(Collectors.toSet());
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

	public List<Meal> getDayFood(Date date, String meals, FoodFormater fa) throws IOException {
		return getDayFood(date, parseMeals(meals), fa);
	}

	public List<Meal> getDayFood(Date date, FoodFormater fa) throws IOException {
		return getDayFood(date, getDefaultMeals(), fa);
	}

	public List<Meal> getDayFood(Date date, Set<Integer> requestedMeals, FoodFormater fa) throws IOException {
		if (needsRelog())
			throw new IllegalStateException("Session expired");

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

	private void loadUserMetadata() throws IOException {
		authToken = new JSONObject(Jsoup.connect(getURL(USER_AUTH_DATA)).ignoreContentType(true).cookies(cookies).get().body().html());

		sessionExpirationTime = System.currentTimeMillis() + authToken.getLong("expires_in") * 1000;
		userId = authToken.getString("user_id");
		accessToken = authToken.getString("access_token");
		tokenType = authToken.getString("token_type");

		String queryUrl = getApiURL(USER_METADATA, userId) + getFieldQueryURL("fields[]", METADATA_QUERY_FIELDS);

		userMetadata = new JSONObject(Jsoup.connect(queryUrl).ignoreContentType(true).header("Authorization", tokenType + " " + accessToken)
				.header("mfp-client-id", "mfp-main-js").header("Accept", "application/json").header("mfp-user-id", userId).get().body().html());

		mealNames = userMetadata.getJSONObject("item").getJSONObject("diary_preferences").getJSONArray("meal_names");

		for (int i = 0; i < mealNames.length(); i++)
			defaultMealIndexes.add(i);
	}

	private String getURLForDate(Date date) {
		return getURL(FOR_DATE, username, DATE_FORMAT.format(date));
	}

	private static String getFieldQueryURL(String field, String... args) throws UnsupportedEncodingException {
		String queryFieldsUrl = "";
		String fEnc = URLEncoder.encode(field, StandardCharsets.UTF_8.toString());
		for (String f : args)
			queryFieldsUrl += fEnc + "=" + f + "&";
		queryFieldsUrl = queryFieldsUrl.substring(0, queryFieldsUrl.length() - 1);

		return queryFieldsUrl;
	}

	private static String getURL(String path, Object... params) {
		return BASE_URL_SECURE + String.format(path, params);
	}

	private static String getApiURL(String path, Object... params) {
		return BASE_API_URL + String.format(path, params);
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
