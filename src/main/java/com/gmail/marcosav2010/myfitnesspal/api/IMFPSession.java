package com.gmail.marcosav2010.myfitnesspal.api;

import com.gmail.marcosav2010.myfitnesspal.api.diary.Diary;
import com.gmail.marcosav2010.myfitnesspal.api.user.UserData;

public interface IMFPSession {

    long getCreationTime();

    UserData toUser();

    String encode();

    boolean shouldReLog();

    Diary toDiary();

    IMFPSession setTimeout(int timeout);
}
