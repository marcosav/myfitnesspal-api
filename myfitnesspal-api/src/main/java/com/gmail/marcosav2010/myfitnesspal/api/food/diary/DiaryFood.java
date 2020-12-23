package com.gmail.marcosav2010.myfitnesspal.api.food.diary;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.Map;

@ToString
@AllArgsConstructor
public class DiaryFood {

    @Getter
    private final String entryId;
    @Getter
    private final String name, brand, unit;
    @Getter
    private final float amount;
    @Getter
    private final float energy;

    private final Map<String, Float> values;

    public float get(String value) {
        return values.get(value);
    }
}