package com.gmail.marcosav2010.myfitnesspal.api;

import com.gmail.marcosav2010.myfitnesspal.api.food.diary.Diary;
import com.gmail.marcosav2010.myfitnesspal.api.user.UserData;

import java.io.IOException;

public interface IMFPSession {

    long getCreationTime();

    UserData toUser();

    String encode();

    boolean shouldReLog();

    Diary toDiary();

    void logout() throws IOException;

    IMFPSession setTimeout(int timeout);
}
