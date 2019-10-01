package com.gmail.marcosav2010.myfitnesspal.lister;

import com.gmail.marcosav2010.myfitnesspal.api.Food;

public interface FoodFormater {
	
	Food get(String name, String brand, String unit, double number);
}
