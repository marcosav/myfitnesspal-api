package com.gmail.marcosav2010.myfitnesspal.api.diary.exercise;

import com.gmail.marcosav2010.json.JSONObject;
import com.gmail.marcosav2010.myfitnesspal.api.Unit;
import com.gmail.marcosav2010.myfitnesspal.api.user.UserData;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ExerciseParser {

    private static final String CARDIO = "cardio", STRENGTH = "strength", POUNDS = "pounds", KILOGRAMS = "kilograms";

    private static final double POUNDS_PER_KILOGRAM = 0.4535924;

    private final UserData userData;

    public void parseExercise(List<CardioExercise> cardioExercises,
                              List<StrengthExercise> strengthExercises,
                              JSONObject e) {

        float energy = e.getJSONObject("energy").getFloat("value");

        JSONObject exercise = e.getJSONObject("exercise");
        String name = exercise.getString("description");

        String type = exercise.getString("type");

        if (CARDIO.equals(type)) {
            int duration = e.getInt("duration");
            cardioExercises.add(new CardioExercise(name, energy, duration));

        } else if (STRENGTH.equals(type)) {
            int quantity = e.getInt("quantity");
            int sets = e.getInt("sets");
            int reps = e.getInt("reps_per_set");
            float weight = 0;
            if (e.has("weight_per_set")) {
                JSONObject weightObj = e.getJSONObject("weight_per_set");
                String unit = weightObj.getString("unit");
                double value = weightObj.getDouble("value");
                weight = (float) getUserFriendlyWeight(unit, value);
            }
            strengthExercises.add(new StrengthExercise(name, energy, quantity, sets, reps, weight));
        }
    }

    private double getUserFriendlyWeight(String unit, double value) {
        String weightUnit = userData.getUnit(Unit.WEIGHT);
        if (unit.equals(weightUnit))
            return value;

        // TODO: Improve this conversions and support more
        if (unit.equals(POUNDS) && weightUnit.equals(KILOGRAMS))
            return value * POUNDS_PER_KILOGRAM;

        return value;
    }

    @Deprecated
    public void updateWeights(List<StrengthExercise> strengthExercises, Float[] weights) {
        for (int i = 0; i < weights.length; i++)
            strengthExercises.get(i).setWeight(weights[i]);
    }
}
