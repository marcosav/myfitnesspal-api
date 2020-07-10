package com.gmail.marcosav2010.myfitnesspal.api.lister;

import com.gmail.marcosav2010.json.JSONArray;
import com.gmail.marcosav2010.myfitnesspal.api.Food;
import com.gmail.marcosav2010.myfitnesspal.api.Meal;
import lombok.AllArgsConstructor;

import java.util.*;

@AllArgsConstructor
public class FoodList {

    private final ListerData data;
    private final Collection<Meal> inputList;

    private String removeEnd(Number n) {
        return String.valueOf(n).replaceFirst("\\.0$", "");
    }

    private String getAmount(Food f) {
        Double conversion;
        try {
            conversion = data.getConversion(f.getName());
        } catch (NumberFormatException ex) {
            return "[Unit error] ";
        }

        if (conversion == null)
            return removeEnd(f.getAmount()) + "" + f.getUnit() + " ";

        if (conversion > 0)
            return removeEnd(f.getAmount() / conversion) + " ";

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
