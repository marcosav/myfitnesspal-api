package com.gmail.marcosav2010.myfitnesspal.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class Food {

	@Getter
	private String name, brand, unit;
	@Getter
	private double amount;

	public void add(double amount) {
		this.amount += amount;
	}
}