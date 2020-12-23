package com.gmail.marcosav2010.myfitnesspal.api.user;

import com.gmail.marcosav2010.json.JSONArray;
import com.gmail.marcosav2010.json.JSONObject;
import com.gmail.marcosav2010.myfitnesspal.api.Unit;
import lombok.Getter;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UserData {

    private final JSONObject userMetadata;

    private List<String> mealNames;
    private List<String> trackedNutrients;

    private final Map<Unit, String> units = new HashMap<>();

    @Getter
    private String username;
    @Getter
    private String email;

    public UserData(JSONObject metadata) {
        userMetadata = metadata;
        parseMetadata();
    }

    private void parseMetadata() {
        JSONObject diaryPreferences = userMetadata
                .getJSONObject("diary_preferences");

        mealNames = toString(diaryPreferences.getJSONArray("meal_names"));
        trackedNutrients = toString(diaryPreferences.getJSONArray("tracked_nutrients"));

        units.clear();
        userMetadata
                .getJSONObject("unit_preferences")
                .toMap()
                .forEach((k, v) -> units.put(Unit.valueOf(k.toUpperCase()), v.toString()));

        username = userMetadata.getString("username");
        email = userMetadata.getString("email");
    }

    public List<String> getMealNames() {
        return Collections.unmodifiableList(mealNames);
    }

    public List<String> getTrackedNutrients() {
        return Collections.unmodifiableList(trackedNutrients);
    }

    public String getUnit(Unit unit) {
        return units.get(unit);
    }

    private List<String> toString(JSONArray array) {
        return Collections.unmodifiableList(array.toList().stream().map(Object::toString).collect(Collectors.toList()));
    }

    public JSONObject toJSONObject() {
        return userMetadata;
    }
}
