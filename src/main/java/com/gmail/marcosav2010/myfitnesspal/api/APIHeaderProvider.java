package com.gmail.marcosav2010.myfitnesspal.api;

import java.io.IOException;
import java.util.Map;

interface APIHeaderProvider {

    Map<String, String> getHeaders() throws IOException;
}
