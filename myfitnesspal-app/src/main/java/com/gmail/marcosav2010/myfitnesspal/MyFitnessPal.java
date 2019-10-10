package com.gmail.marcosav2010.myfitnesspal;

import java.io.IOException;
import java.util.Calendar;

import com.gmail.marcosav2010.myfitnesspal.api.MFPSession;
import com.gmail.marcosav2010.myfitnesspal.api.lister.CustomFoodFormater;
import com.gmail.marcosav2010.myfitnesspal.api.lister.FoodList;
import com.gmail.marcosav2010.myfitnesspal.api.lister.ListerData;

public class MyFitnessPal {

	public static void main(String[] args) {
		// new/use [user password][json config P/B 2 2 2 0123]
		try {
			switch (args[0]) {
			case "new":
				requestSession(args[1], args[2]);
				break;
			case "use":
				if (args.length == 7 || args.length == 8) {
					ListerData lc = new ListerData(args[2].replaceAll("__", "\""));
					lc.load();
					generateList(args[1], lc, args[3], args[4], args[5], args[6], args.length == 7 ? "" : args[7]);
					break;
				}
			default:
				error("Incorrect parameters");
			}
		} catch (Exception ex) {
			error(ex.getMessage());
		}
	}

	private static void requestSession(String user, String password) throws IOException {
		output(MFPSession.create(user, password).encode());
	}

	private static void generateList(String jsonSession, ListerData lc, String option, String day, String month, String year, String meals) throws IOException {
		Calendar calendar = Calendar.getInstance();
		calendar.set(Integer.parseInt(year), Integer.parseInt(month) - 1, Integer.parseInt(day));
		FoodList fl = new FoodList(lc, MFPSession.from(jsonSession).getDayFood(calendar.getTime(), meals, new CustomFoodFormater(lc)));

		String result;

		if (option.equalsIgnoreCase("b"))
			result = fl.toJSON(true);
		else if (option.equalsIgnoreCase("p"))
			result = fl.toJSON(false);
		else {
			error("Incorrect option");
			return;
		}

		output(result);
	}

	private static void output(String out) {
		System.out.print("0" + out);
	}

	private static void error(String out) {
		System.err.print(out);
	}
}
