package com.gmail.marcosav2010.myfitnesspal.api.lister;

import com.gmail.marcosav2010.myfitnesspal.api.Food;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CustomFoodFormatter implements FoodFormatter {

	private final ListerData data;

	private String getAmountUnit(String u) {
		if (u.startsWith("g"))
			return "g";

		if (data.isUnitAlias(u))
			return "";

		return u;
	}

	@Override
	public Food get(String name, String brand, String unit, double number) {
		return new Food(data.getAlias(name), brand, getAmountUnit(unit), number);
	}
}
