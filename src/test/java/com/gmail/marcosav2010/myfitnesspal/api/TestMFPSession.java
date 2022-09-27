package com.gmail.marcosav2010.myfitnesspal.api;

import com.gmail.marcosav2010.myfitnesspal.api.diary.Day;
import com.gmail.marcosav2010.myfitnesspal.api.diary.Diary;
import com.gmail.marcosav2010.myfitnesspal.api.diary.exercise.CardioExercise;
import com.gmail.marcosav2010.myfitnesspal.api.diary.exercise.StrengthExercise;
import com.gmail.marcosav2010.myfitnesspal.api.diary.food.DiaryFood;
import com.gmail.marcosav2010.myfitnesspal.api.diary.food.DiaryMeal;
import com.gmail.marcosav2010.myfitnesspal.api.diary.food.FoodValues;
import com.gmail.marcosav2010.myfitnesspal.api.user.UserData;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestMFPSession {

    static {
        SeleniumLoginHandler.setDriverProperties();
    }

    private static final float DELTA = 0.2f;

    private static final String USER = System.getenv("MFP_USERNAME");
    private static final String PASSWORD = System.getenv("MFP_PASSWORD");

    private final LoginHandler loginHandler = new SeleniumLoginHandler(false);

    @Test
    void test() throws IOException, LoginException {
        IMFPSession session2 = MFPSession.create(USER, PASSWORD, loginHandler);
        String encoded = session2.encode();

        IMFPSession session = MFPSession.from(encoded, loginHandler);

        UserData userData = session.toUser();
        Diary diary = session.toDiary();

        assertEquals("marcosav2010@gmail.com", userData.getEmail());
        assertEquals(USER, userData.getUsername());
        assertEquals("calories", userData.getUnit(Unit.ENERGY));
        assertEquals("kilometers", userData.getUnit(Unit.DISTANCE));

        List<String> meals = userData.getMealNames();
        assertEquals(4, meals.size());
        assertEquals("Comida", meals.get(1));

        List<String> trackedNutrients = userData.getTrackedNutrients();
        assertEquals(6, trackedNutrients.size());
        assertEquals("carbohydrates", trackedNutrients.get(1));
        assertEquals("fat", trackedNutrients.get(2));
        assertEquals("sodium", trackedNutrients.get(4));

        Calendar c = Calendar.getInstance();
        c.set(2020, Calendar.DECEMBER, 20);
        Date date = c.getTime();

        Day result = diary.getFullDay(date);

        assertEquals("", result.getExerciseNote());
        assertEquals("hi test", result.getFoodNotes());

        assertEquals(4, result.getMeals().size());

        DiaryMeal last = result.getMeals().get(3);

        assertEquals(3, last.getFood().size());
        assertEquals(3, last.getIndex());
        assertEquals("Merienda/Otros", last.getName());

        DiaryFood food = last.getFood().get(2);
        assertEquals("Kiwi", food.getName());
        assertEquals("Hacendado", food.getBrand());
        assertEquals("g", food.getUnit());
        assertEquals(180, food.getAmount(), DELTA);
        assertEquals(110, food.getEnergy(), DELTA);

        assertEquals(0.9, food.get(FoodValues.FAT), DELTA);
        assertEquals(0.5, food.get(FoodValues.POLYUNSATURATED_FAT), DELTA);
        assertEquals(0, food.get(FoodValues.CHOLESTEROL), DELTA);
        assertEquals(5.4, food.get(FoodValues.SODIUM), DELTA);
        assertEquals(561.6, food.get(FoodValues.POTASSIUM), DELTA);
        assertEquals(27, food.get(FoodValues.CARBOHYDRATES), DELTA);
        assertEquals(5.4, food.get(FoodValues.FIBER), DELTA);
        assertEquals(16.2, food.get(FoodValues.SUGAR), DELTA);
        assertEquals(2, food.get(FoodValues.PROTEIN), DELTA);

        assertEquals(2, result.getCardioExercises().size());
        assertEquals(1, result.getStrengthExercises().size());

        c.set(2020, Calendar.DECEMBER, 22);
        date = c.getTime();
        result = diary.getDay(date, Diary.FOOD, Diary.FOOD_NOTES, Diary.WATER);

        assertEquals("", result.getExerciseNote());
        assertEquals("hi test", result.getFoodNotes());
        assertEquals(48, result.getWater());

        String foodNotes = diary.getFoodNotes(date);
        int water = diary.getWater(date);

        assertEquals("hi test", foodNotes);
        assertEquals(48, water);

        assertEquals(4, result.getMeals().size());

        DiaryMeal dinner = result.getMeals().get(2);

        assertEquals(3, dinner.getFood().size());
        assertEquals(2, dinner.getIndex());
        assertEquals("Cena", dinner.getName());

        assertEquals(1104.4, dinner.get(FoodValues.ENERGY), DELTA);
        assertEquals(18, dinner.get(FoodValues.FAT), DELTA);
        assertEquals(9, dinner.get(FoodValues.SATURATED_FAT), DELTA);
        assertEquals(0, dinner.get(FoodValues.VITAMIN_A), DELTA);
        assertEquals(1195.5, dinner.get(FoodValues.SODIUM), DELTA);
        assertEquals(0, dinner.get(FoodValues.CALCIUM), DELTA);
        assertEquals(198.5, dinner.get(FoodValues.CARBOHYDRATES), DELTA);
        assertEquals(0, dinner.get(FoodValues.FIBER), DELTA);
        assertEquals(10.1, dinner.get(FoodValues.SUGAR), DELTA);
        assertEquals(34, dinner.get(FoodValues.PROTEIN), DELTA);

        assertEquals(0, result.getCardioExercises().size());
        assertEquals(0, result.getStrengthExercises().size());

        c.set(2020, Calendar.DECEMBER, 28);
        date = c.getTime();
        result = diary.getDay(date, Diary.EXERCISE, Diary.EXERCISE_NOTES);

        assertEquals("", result.getFoodNotes());
        assertEquals("ads", result.getExerciseNote());
        assertEquals(0, result.getWater());

        assertEquals(0, result.getMeals().size());

        assertEquals(3, result.getCardioExercises().size());
        assertEquals(2, result.getStrengthExercises().size());

        CardioExercise cardioExercise = result.getCardioExercises().get(2);
        assertEquals(13 * 60, cardioExercise.getDuration());
        assertEquals(2, cardioExercise.getEnergy());
        assertEquals("Running (jogging), up stairs", cardioExercise.getName());

        StrengthExercise strengthExercise = result.getStrengthExercises().get(1);
        assertEquals(8, strengthExercise.getRepetitions());
        assertEquals(4, strengthExercise.getSets());
        assertEquals(32, strengthExercise.getQuantity());
        assertEquals(81.2, strengthExercise.getWeight(), DELTA);
        assertEquals("Bench Press, Barbell", strengthExercise.getName());
    }
}
