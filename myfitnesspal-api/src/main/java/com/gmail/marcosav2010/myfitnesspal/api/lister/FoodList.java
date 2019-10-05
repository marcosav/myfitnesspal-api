package com.gmail.marcosav2010.myfitnesspal.api.lister;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.gmail.marcosav2010.myfitnesspal.api.Food;
import com.gmail.marcosav2010.myfitnesspal.api.Meal;
import com.gmail.marcosav2010.myfitnesspal.json.JSONArray;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class FoodList {

	private ListerData data;
	private Collection<Meal> inputList;

	private String removeEnd(Number n) {
		return String.valueOf(n).replaceFirst("\\.0$", "");
	}

	private String getAmount(Food f) {
		Double conv = data.getConversion(f.getName());
		if (conv == null)
			return removeEnd(f.getAmount()) + "" + f.getUnit() + " ";

		if (conv > 0)
			return removeEnd(f.getAmount() / conv) + " ";

		return "";
	}

	public Collection<Food> toFood(boolean buy) {
		if (buy) {
			Map<String, Food> out = new HashMap<>();
			inputList.forEach(m -> m.getFood().forEach(f -> {
				if (out.containsKey(f.getName())) {
					out.get(f.getName()).add(f.getAmount());
				} else
					out.put(f.getName(), f);
			}));

			return out.values();

		} else {
			List<Food> out = new LinkedList<>();
			inputList.forEach(m -> out.addAll(m.getFood()));
			return out;
		}
	}

	public List<String> toList(boolean buy) {
		List<String> out = new LinkedList<>();

		toFood(buy).forEach(f -> {
			String alias = f.getName();
			String q = "";

			if (buy) {
				if (!data.isException(alias)) {
					q = data.isMeasured(alias) ? getAmount(f) : "";
					out.add(q + alias);
				}
			} else {
				String o = "";

				if (data.isSacar(alias))
					o = "Sacar ";
				else if (data.isPesar(alias))
					o = "";
				else if (data.isPicar(alias))
					o = "Picar ";
				else
					return;

				q = getAmount(f);

				out.add(o + q + alias);
			}
		});

		return out;
	}

	public String toJSON(boolean buy) {
		return new JSONArray(toList(buy)).toString();
	}
}
