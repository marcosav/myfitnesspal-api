package com.gmail.marcosav2010.myfitnesspal.api.diary.exercise;

import lombok.Getter;

public class CardioExercise extends Exercise {

    public CardioExercise(String name, float energy, int duration) {
        super(name, energy);
        this.duration = duration;
    }

    @Getter
    private final int duration;
}
