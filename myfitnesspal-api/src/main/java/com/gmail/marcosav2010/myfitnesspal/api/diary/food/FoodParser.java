package com.gmail.marcosav2010.myfitnesspal.api.diary.food;

import com.gmail.marcosav2010.json.JSONObject;
import com.gmail.marcosav2010.myfitnesspal.api.user.UserData;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

@RequiredArgsConstructor
public class FoodParser {

    private final UserData userData;

    public void parseFood(Map<Integer, DiaryMeal> meals, JSONObject e) {
        String mealName = e.getString("meal_name");
        int mealIndex = e.getInt("meal_position");

        if (meals.get(mealIndex) == null)
            meals.put(mealIndex, new DiaryMeal(mealIndex, mealName));

        DiaryMeal meal = meals.get(mealIndex);

        JSONObject fe = e.getJSONObject("food");
        String id = fe.getString("id");
        String[] desc = fe.getString("description").split(" - ");
        String brand = "";
        if (fe.get("brand_ame") != JSONObject.NULL)
            brand = fe.getString("brand_name");

        String name = desc.length > 1 ? desc[1] : desc[0];

        float servings = e.getFloat("servings");
        JSONObject servingSize = e.getJSONObject("serving_size");
        float servingValue = servingSize.getFloat("value");
        String unit = servingSize.getString("unit");

        float amount = servings * servingValue;

        JSONObject ne = e.getJSONObject("nutritional_contents");
        Map<String, Float> values = getValues(ne);

        DiaryFood food = new DiaryFood(id, name, brand, unit, amount, values.get(FoodValues.ENERGY), values);

        meal._getFood().add(food);
    }

    public void parseMeal(Map<Integer, DiaryMeal> meals, JSONObject e) {
        String mealName = e.getString("diary_meal");
        int mealIndex = userData.getMealNames().indexOf(mealName);
        if (mealIndex == -1)
            return;

        if (meals.get(mealIndex) == null)
            meals.put(mealIndex, new DiaryMeal(mealIndex, mealName));

        DiaryMeal meal = meals.get(mealIndex);

        JSONObject ne = e.getJSONObject("nutritional_contents");
        meal.setValues(getValues(ne));
    }

    private Map<String, Float> getValues(JSONObject ne) {
        Map<String, Float> values = new LinkedHashMap<>();

        float energy = ne.getJSONObject(FoodValues.ENERGY).getFloat("value");
        values.put(FoodValues.ENERGY, energy);

        ne.toMap().forEach((k, v) -> {
            if (FoodValues.ENERGY.equals(k) || v == null)
                return;

            values.put(k, Float.parseFloat(v.toString()));
        });

        return values;
    }
}
