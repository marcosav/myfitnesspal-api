package com.gmail.marcosav2010.myfitnesspal.api.food.diary;

import lombok.*;

import java.util.*;

@ToString
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DiaryMeal {

    @Getter
    private final int index;
    @Getter
    private final String name;

    private final List<DiaryFood> food = new LinkedList<>();

    private Map<String, Float> values;

    public float get(String value) {
        return values.get(value);
    }

    public List<DiaryFood> getFood() {
        return Collections.unmodifiableList(food);
    }

    List<DiaryFood> _getFood() {
        return food;
    }

    void setValues(Map<String, Float> values) {
        this.values = new LinkedHashMap<>(values);
    }
}