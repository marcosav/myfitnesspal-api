package com.gmail.marcosav2010.myfitnesspal.api.diary.exercise;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class StrengthExercise extends Exercise {

    public StrengthExercise(String name, float energy, int quantity, int sets, int repetitions, float weight) {
        super(name, energy);
        this.quantity = quantity;
        this.sets = sets;
        this.repetitions = repetitions;
        this.weight = weight;
    }

    @Getter
    private final int quantity, sets, repetitions;
    @Getter
    @Setter(AccessLevel.PACKAGE)
    private float weight;
}
