package com.gmail.marcosav2010.myfitnesspal.api;

import java.util.LinkedList;
import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class Meal {

	@Getter
	private String name;
	@Getter
	private final List<Food> food = new LinkedList<>();
}