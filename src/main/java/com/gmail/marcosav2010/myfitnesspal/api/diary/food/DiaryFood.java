package com.gmail.marcosav2010.myfitnesspal.api.diary.food;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.util.Map;

@ToString
@AllArgsConstructor
public class DiaryFood {

    @NonNull
    @Getter
    private final String entryId, name, brand, unit;
    @Getter
    private final float amount;
    @Getter
    private final float energy;

    private final Map<String, Float> values;

    public float get(String value) {
        return values.get(value);
    }
}