package com.gmail.marcosav2010.myfitnesspal.api.food.diary;

import com.gmail.marcosav2010.json.JSONArray;
import com.gmail.marcosav2010.json.JSONObject;
import com.gmail.marcosav2010.myfitnesspal.api.BaseFetcher;
import com.gmail.marcosav2010.myfitnesspal.api.Unit;
import com.gmail.marcosav2010.myfitnesspal.api.food.FoodValues;
import com.gmail.marcosav2010.myfitnesspal.api.user.UserData;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Diary {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private static final String DIARY_DATA = "diary?%s=all&entry_date=%s&types=%s";
    //private static final String FOOD_FOR_DATE = "food/diary/%s?date=%s";
    private static final String FOOD_NOTE_FOR_DATE = "food/note/%s?date=%s";
    private static final String FOOD_WATER_FOR_DATE = "food/water/%s?date=%s";

    public static final String WATER = "water", NOTES = "notes";

    //private static final Set<String> MACROS = new HashSet<>(Arrays.asList("carbohydrates", "fat", "protein"));

    private static final String FOOD_ENTRY = "food_entry", DIARY_MEAL = "diary_meal";
    private static final String[] DIARY_QUERY_TYPES = {
            FOOD_ENTRY,
            "exercise_entry",
            "steps_aggregate",
            "diary_meal"
    };

    private final BaseFetcher fetcher;
    private final UserData userData;

    private final Set<Integer> defaultMealIndexes = new HashSet<>();

    public Diary(UserData userData, BaseFetcher fetcher) {
        this.userData = userData;
        this.fetcher = fetcher;

        for (int i = 0; i < userData.getMealNames().size(); i++)
            defaultMealIndexes.add(i);
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

    public FoodDay getDay(Date date) throws IOException {
        return getDay(date, getDefaultMeals());
    }

    public FoodDay getDay(Date date, String meals, String... params) throws IOException {
        return getDay(date, parseMeals(meals), params);
    }

    public FoodDay getDay(Date date, Set<Integer> requestedMeals, String... params) throws IOException {
        String url = getDiaryQuery(DATE_FORMAT.format(date), DIARY_QUERY_TYPES);
        JSONArray content = fetcher.json(url).getJSONArray(BaseFetcher.ITEMS_WRAPPER);

        Map<Integer, DiaryMeal> meals = new LinkedHashMap<>();

        for (Object _o : content) {
            JSONObject e = (JSONObject) _o;
            String type = e.getString("type");
            String mealName;
            DiaryMeal meal;
            int mealIndex;

            if (FOOD_ENTRY.equals(type)) {
                mealName = e.getString("meal_name");
                mealIndex = e.getInt("meal_position");

                if (!requestedMeals.contains(mealIndex))
                    continue;

                if (meals.get(mealIndex) == null)
                    meals.put(mealIndex, new DiaryMeal(mealIndex, mealName));

                meal = meals.get(mealIndex);

                JSONObject fe = e.getJSONObject("food");
                String id = fe.getString("id");
                String[] desc = fe.getString("description").split(" - ");
                String brand = fe.getString("brand_name");

                String name = desc.length > 1 ? desc[1] : desc[0];

                float servings = e.getFloat("servings");
                JSONObject servingSize = e.getJSONObject("serving_size");
                float servingValue = servingSize.getFloat("value");
                String unit = servingSize.getString("unit");

                float amount = servings * servingValue;

                JSONObject ne = e.getJSONObject("nutritional_contents");
                Map<String, Float> values = getValues(ne);

                DiaryFood food = new DiaryFood(id, name, brand, unit, amount, values.get(FoodValues.ENERGY), values);

                meal._getFood().add(food);

            } else if (DIARY_MEAL.equals(type)) {
                mealName = e.getString("diary_meal");
                mealIndex = userData.getMealNames().indexOf(mealName);
                if (mealIndex == -1)
                    continue;

                if (!requestedMeals.contains(mealIndex))
                    continue;

                JSONObject ne = e.getJSONObject("nutritional_contents");

                if (meals.get(mealIndex) == null)
                    meals.put(mealIndex, new DiaryMeal(mealIndex, mealName));

                meal = meals.get(mealIndex);

                meal.setValues(getValues(ne));
            }
        }

        /*String url = getURLForDate(FOOD_FOR_DATE, date);

        Document foodDoc = fetcher.connect(url).get();
        Elements mealHeaders = foodDoc.getElementsByClass("meal_header");

        String notes = "";
        int water = 0;
        List<DiaryMeal> mealsList = new LinkedList<>();
        int mealNo = 0;

        for (Element e : mealHeaders) {
            if (!requestedMeals.contains(mealNo++))
                continue;

            String mealTitle = e.selectFirst("td[class='first alt']").text();
            Element cell = e.nextElementSibling();

            String[] foodContent, info, amount;
            String name, brand, unit;
            float number;
            long entryId;
            int calories = 0;

            DiaryMeal meal = new DiaryMeal(mealNo, mealTitle);

            while (!cell.classNames().contains("bottom")) {
                Element foodField = cell.selectFirst("a[class='js-show-edit-food']");
                if (foodField == null)
                    break;

                entryId = Long.parseLong(foodField.attr("data-food-entry-id"));
                foodContent = foodField.text().trim().split(", ");
                info = foodContent[0].split(" - ");
                name = info.length > 1 ? info[1] : info[0];
                brand = info.length == 1 ? "" : info[0];
                amount = foodContent[1].split("\\s+");
                number = Float.parseFloat(amount[0]);
                unit = amount[1];

                Map<String, Integer> values = new LinkedHashMap<>();
                Element valueField;
                int i = 2;
                for (Object n : userData.getTrackedNutrients()) {
                    valueField = cell.selectFirst("td:nth-child(" + i++ + ")" +
                            (isMacro(n.toString()) ? " span.macro-value" : ""));

                    int value = Integer.parseInt(valueField.text().replaceAll(",", ""));

                    if (i == 3) {
                        calories = value;
                        continue;
                    }

                    values.put(n.toString(), value);
                }

                DiaryFood food = new DiaryFood(entryId, name, brand, unit, number, calories, values);

                meal.getFood().add(food);

                cell = cell.nextElementSibling();
            }

            mealsList.add(meal);
        }*/

        String notes = "";
        int water = 0;

        List<String> ps = Arrays.asList(params);
        if (ps.contains(WATER))
            water = getWater(date);

        if (ps.contains(NOTES))
            notes = getNotes(date);

        return new FoodDay(date, new ArrayList<>(meals.values()), water, notes);
    }

    /*private boolean isMacro(String nutrient) {
        return MACROS.contains(nutrient.toLowerCase());
    }*/

    private Map<String, Float> getValues(JSONObject ne) {
        Map<String, Float> values = new LinkedHashMap<>();

        float energy = ne.getJSONObject(FoodValues.ENERGY).getFloat("value");
        values.put(FoodValues.ENERGY, energy);

        ne.toMap().forEach((k, v) -> {
            if (FoodValues.ENERGY.equals(k) || v == null)
                return;

            values.put(k, Float.parseFloat(v.toString()));
        });

        return values;
    }

    public List<FoodDay> getDayRange(Set<Date> dates) {
        return getDayRange(dates, getDefaultMeals());
    }

    public List<FoodDay> getDayRange(Set<Date> dates, String requestedMeals) {
        return getDayRange(dates, parseMeals(requestedMeals));
    }

    public List<FoodDay> getDayRange(Set<Date> dates, Set<Integer> requestedMeals) {
        return dates.stream().map(d -> {
            try {
                return getDay(d, requestedMeals);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public int getWater(Date date) throws IOException {
        JSONObject res = fetcher.json(getURLForDate(FOOD_WATER_FOR_DATE, date))
                .getJSONObject(BaseFetcher.ITEM_WRAPPER);

        return res.getInt(userData.getUnit(Unit.WATER));
    }

    public String getNotes(Date date) throws IOException {
        JSONObject res = fetcher.json(getURLForDate(FOOD_NOTE_FOR_DATE, date))
                .getJSONObject(BaseFetcher.ITEM_WRAPPER);

        return res.getString("body");
    }

    public String getDiaryQuery(String date, String... args) {
        return fetcher.getApiURL(
                String.format(DIARY_DATA,
                        BaseFetcher.ENCODED_FIELDS,
                        date,
                        String.join(BaseFetcher.ENCODED_DELIMITER, args)
                ));
    }

    private String getURLForDate(String url, Date date) {
        return fetcher.getURL(url, userData.getUsername(), DATE_FORMAT.format(date));
    }
}
