package com.gmail.marcosav2010.myfitnesspal.api.diary.exercise;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor
public abstract class Exercise {

    @NonNull
    @Getter
    private final String name;
    @Getter
    private final float energy;
}
