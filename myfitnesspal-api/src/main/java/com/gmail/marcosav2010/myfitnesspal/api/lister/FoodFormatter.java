package com.gmail.marcosav2010.myfitnesspal.api.lister;

import com.gmail.marcosav2010.myfitnesspal.api.Food;

public interface FoodFormatter {

	Food get(String name, String brand, String unit, double number);
}
