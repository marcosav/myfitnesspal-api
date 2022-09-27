package com.gmail.marcosav2010.myfitnesspal.api.diary;

import com.gmail.marcosav2010.json.JSONArray;
import com.gmail.marcosav2010.json.JSONObject;
import com.gmail.marcosav2010.myfitnesspal.api.BaseFetcher;
import com.gmail.marcosav2010.myfitnesspal.api.Unit;
import com.gmail.marcosav2010.myfitnesspal.api.diary.exercise.CardioExercise;
import com.gmail.marcosav2010.myfitnesspal.api.diary.exercise.ExerciseParser;
import com.gmail.marcosav2010.myfitnesspal.api.diary.exercise.StrengthExercise;
import com.gmail.marcosav2010.myfitnesspal.api.diary.food.DiaryMeal;
import com.gmail.marcosav2010.myfitnesspal.api.diary.food.FoodParser;
import com.gmail.marcosav2010.myfitnesspal.api.user.UserData;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Diary {

    static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private static final String DIARY_DATA = "diary?%s=%s&entry_date=%s&types=%s&fields=all";
    private static final String FOOD_NOTE_FOR_DATE = "food/note/%s?date=%s";
    private static final String FOOD_WATER_FOR_DATE = "food/water/%s?date=%s";

    public static final byte WATER = 0,
            FOOD_NOTES = 1,
            EXERCISE_NOTES = 2,
            EXERCISE = 3,
            FOOD = 4;

    private static final String
            FOOD_ENTRY = "food_entry",
            EXERCISE_ENTRY = "exercise_entry",
            DIARY_MEAL = "diary_meal",
            STEPS_AGGREGATE = "steps_aggregate";

    private final BaseFetcher fetcher;
    private final UserData userData;

    private final ReportDataFetcher reportDataFetcher;

    private final FoodParser foodParser;
    private final ExerciseParser exerciseParser;

    public Diary(UserData userData, BaseFetcher fetcher) {
        this.userData = userData;
        this.fetcher = fetcher;
        foodParser = new FoodParser(userData);
        exerciseParser = new ExerciseParser(userData);
        reportDataFetcher = new ReportDataFetcher(fetcher);
    }

    /**
     * Obtains all available data for the given date, equivalent of {@link #getDay(Date, Byte, Byte...)}
     * with all parameters
     *
     * @param date where data is retrieved from
     * @return a Day populated with all available data
     * @throws IOException when there was any kind of error connecting to myfitnesspal, such as expired session
     */
    public Day getFullDay(Date date) throws IOException {
        return getDay(date, null);
    }

    /**
     * @param date   where data is retrieved from
     * @param params which indicate the data is going to be fetched, possible values are
     *               Diary.FOOD, Diary.EXERCISE, Diary.FOOD_NOTES, Diary.EXERCISE_NOTES, Diary.WATER
     * @return a Day populated with the requested data
     * @throws IOException when there was any kind of error connecting to myfitnesspal, such as expired session
     */
    public synchronized Day getDay(Date date, Byte param, Byte... params) throws IOException {
        Set<Byte> ps = Stream.of(params).collect(Collectors.toSet());
        if (param != null)
            ps.add(param);

        boolean all = ps.isEmpty();
        boolean exercise = all || ps.contains(EXERCISE);

        /*if (params.length == 0)
            queryFields.add(STEPS_AGGREGATE);*/

        Map<Integer, DiaryMeal> meals = new LinkedHashMap<>();
        List<CardioExercise> cardioExercises = new ArrayList<>();
        List<StrengthExercise> strengthExercises = new ArrayList<>();

        if (exercise || ps.contains(FOOD)) {
            String requestParam = exercise ? "exercise" : "all";

            List<String> queryFields = new ArrayList<>();
            if (all || ps.contains(FOOD)) {
                queryFields.add(FOOD_ENTRY);
                queryFields.add(DIARY_MEAL);
            }

            if (exercise)
                queryFields.add(EXERCISE_ENTRY);

            String url = getDiaryQuery(requestParam, DATE_FORMAT.format(date), queryFields.toArray(new String[0]));
            JSONArray content = fetcher.json(url).getJSONArray(BaseFetcher.ITEMS_WRAPPER);

            for (Object _o : content) {
                JSONObject e = (JSONObject) _o;
                String type = e.getString("type");

                if (FOOD_ENTRY.equals(type)) {
                    foodParser.parseFood(meals, e);

                } else if (DIARY_MEAL.equals(type)) {
                    foodParser.parseMeal(meals, e);

                } else if (EXERCISE_ENTRY.equals(type)) {
                    exerciseParser.parseExercise(cardioExercises, strengthExercises, e);
                }
            }
        }

        boolean reportNeeded = all ||
                ps.contains(EXERCISE_NOTES) ||
                (exercise && !strengthExercises.isEmpty());

        if (reportNeeded)
            reportDataFetcher.fetch(date);

        String foodNotes = "", exerciseNotes = "";
        int water = 0;

        if (all || ps.contains(WATER))
            water = getWater(date);

        if (all || ps.contains(FOOD_NOTES)) {
            if (reportNeeded) // TODO: Fix report page load
                foodNotes = reportDataFetcher.getNotes()[0];
            else
                foodNotes = getFoodNotes(date);
        }

        if (all || ps.contains(EXERCISE_NOTES))
            exerciseNotes = reportDataFetcher.getNotes()[1];

        reportDataFetcher.reset();

        return new Day(date, new ArrayList<>(meals.values()), cardioExercises,
                strengthExercises, water, foodNotes, exerciseNotes);
    }

    /**
     * Obtains all available data for the given dates, equivalent of {@link #getDayRange(Set, Byte, Byte...)}
     * with all parameters
     *
     * @param dates where data is retrieved from
     * @return a list of Day(s) populated with all available data
     */
    public List<Day> getFullDayRange(Set<Date> dates) {
        return getDayRange(dates, null);
    }

    /**
     * @param dates  where data is retrieved from
     * @param params which indicate the data is going to be fetched for given days, possible values are
     *               Diary.FOOD, Diary.EXERCISE, Diary.FOOD_NOTES, Diary.EXERCISE_NOTES, Diary.WATER
     * @return a list of Day(s) populated with the requested data
     */
    public List<Day> getDayRange(Set<Date> dates, Byte param, Byte... params) {
        return dates.stream().map(d -> {
            try {
                return getDay(d, param, params);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * @param date which contains the water amount
     * @return an int value representing the amount of water for the given day
     * @throws IOException when there was any kind of error connecting to myfitnesspal, such as expired session
     */
    public int getWater(Date date) throws IOException {
        JSONObject res = fetcher.json(getURLForDate(FOOD_WATER_FOR_DATE, date))
                .getJSONObject(BaseFetcher.ITEM_WRAPPER);

        return res.getInt(userData.getUnit(Unit.WATER));
    }

    /**
     * @param date which contains the note
     * @return a String containing the requested note
     * @throws IOException when there was any kind of error connecting to myfitnesspal, such as expired session
     */
    public String getFoodNotes(Date date) throws IOException {
        JSONObject res = fetcher.json(getURLForDate(FOOD_NOTE_FOR_DATE, date))
                .getJSONObject(BaseFetcher.ITEM_WRAPPER);

        return res.getString("body");
    }

    private String getDiaryQuery(String param, String date, String... args) {
        return fetcher.getApiURL(
                String.format(DIARY_DATA,
                        BaseFetcher.ENCODED_FIELDS,
                        param,
                        date,
                        String.join(BaseFetcher.ENCODED_DELIMITER, args)
                ));
    }

    private String getURLForDate(String url, Date date) {
        return fetcher.getURL(url, userData.getUsername(), DATE_FORMAT.format(date));
    }
}
