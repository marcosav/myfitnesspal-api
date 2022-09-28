package com.gmail.marcosav2010.myfitnesspal.api.user;

import com.gmail.marcosav2010.json.JSONObject;
import com.gmail.marcosav2010.myfitnesspal.api.BaseFetcher;
import lombok.AllArgsConstructor;

import java.io.IOException;

@AllArgsConstructor
public class UserFetcher {

    private static final String API_USER_DATA = "users/%s?";

    private static final String ITEM_WRAPPER = "item";

    private static final String[] USER_DATA_QUERY_FIELDS = {
            "diary_preferences",
            "goal_preferences",
            "unit_preferences",
            "account",
            "goal_displays",
            "location_preferences",
            "system_data",
            "profiles",
            "step_sources",
            "app_preferences",
            "privacy_preferences"
    };

    private final BaseFetcher fetcher;

    public UserData load(String userId) throws IOException {
        String queryUrl = getUserQuery(userId, USER_DATA_QUERY_FIELDS);
        JSONObject content = fetcher.json(queryUrl).getJSONObject(ITEM_WRAPPER);

        return new UserData(content);
    }

    public String getUserQuery(String userId, String... args) {
        StringBuilder queryFieldsUrl = new StringBuilder();

        for (String f : args)
            queryFieldsUrl.append(BaseFetcher.ENCODED_FIELDS).append("=").append(f).append("&");

        return fetcher.getApiURL(String.format(API_USER_DATA, userId) + queryFieldsUrl);
    }
}
