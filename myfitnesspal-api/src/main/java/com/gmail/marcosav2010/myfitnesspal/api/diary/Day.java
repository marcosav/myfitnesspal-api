package com.gmail.marcosav2010.myfitnesspal.api.diary;

import com.gmail.marcosav2010.myfitnesspal.api.diary.exercise.CardioExercise;
import com.gmail.marcosav2010.myfitnesspal.api.diary.exercise.StrengthExercise;
import com.gmail.marcosav2010.myfitnesspal.api.diary.food.DiaryMeal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.util.Collections;
import java.util.Date;
import java.util.List;

@ToString
@AllArgsConstructor
public class Day {

    @NonNull
    private final Date date;
    private final List<DiaryMeal> meals;
    private final List<CardioExercise> cardioExercises;
    private final List<StrengthExercise> strengthExercises;
    @Getter
    private final int water;
    @NonNull
    @Getter
    private final String foodNotes, exerciseNote;

    public List<DiaryMeal> getMeals() {
        return Collections.unmodifiableList(meals);
    }

    public List<CardioExercise> getCardioExercises() {
        return Collections.unmodifiableList(cardioExercises);
    }

    public List<StrengthExercise> getStrengthExercises() {
        return Collections.unmodifiableList(strengthExercises);
    }

    public Date getDate() {
        return new Date(date.getTime());
    }
}
