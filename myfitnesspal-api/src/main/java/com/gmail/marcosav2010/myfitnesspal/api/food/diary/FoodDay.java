package com.gmail.marcosav2010.myfitnesspal.api.food.diary;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.Collections;
import java.util.Date;
import java.util.List;

@ToString
@AllArgsConstructor
public class FoodDay {

    private final Date date;

    private final List<DiaryMeal> meals;
    @Getter
    private final int water;
    @Getter
    private final String notes;

    public List<DiaryMeal> getMeals() {
        return Collections.unmodifiableList(meals);
    }

    public Date getDate() {
        return new Date(date.getTime());
    }
}
