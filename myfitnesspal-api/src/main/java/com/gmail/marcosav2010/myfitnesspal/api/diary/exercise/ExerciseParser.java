package com.gmail.marcosav2010.myfitnesspal.api.diary.exercise;

import com.gmail.marcosav2010.json.JSONObject;

import java.util.List;

public class ExerciseParser {

    private static final String CARDIO = "cardio", STRENGTH = "strength";

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
            strengthExercises.add(new StrengthExercise(name, energy, quantity, sets, reps, 0));
        }
    }

    public void updateWeights(List<StrengthExercise> strengthExercises, Float[] weights) {
        for (int i = 0; i < weights.length; i++)
            strengthExercises.get(i).setWeight(weights[i]);
    }
}
