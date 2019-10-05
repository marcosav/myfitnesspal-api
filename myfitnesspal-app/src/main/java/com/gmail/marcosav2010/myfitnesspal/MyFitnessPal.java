package com.gmail.marcosav2010.myfitnesspal;

import java.io.IOException;
import java.util.Calendar;

import com.gmail.marcosav2010.myfitnesspal.api.MFPSession;
import com.gmail.marcosav2010.myfitnesspal.api.lister.CustomFoodFormater;
import com.gmail.marcosav2010.myfitnesspal.api.lister.FoodList;
import com.gmail.marcosav2010.myfitnesspal.api.lister.ListerData;

public class MyFitnessPal {

	private static final String CONFIG_DATA = "{\"exceptions\":[\"Oregano\",\"Ajo Diente\",\"Perejil\",\"Lechuga\",\"Alcohol\",\"cocina\",\"Aceite\",\"Vino Blanco Verdejo\",\"Albahaca\",\"Clara\",\"Ginebra\",\"Pimienta Negra\",\"Laurel\",\"Tomillo\",\"sal con ajo\",\"Sal De Ajo\",\"Pan rallado\",\"Cebolla en polvo\",\"Merluza\",\"Cebolla\",\"Ketchup\",\"Huevos\",\"Mostaza\",\"Tomates\",\"Chorizo Aperitivo oreado\",\"Bacon para lentejas\",\"Lentejas\",\"Zanahoria\",\"Kiwi\"],\"aliases\":{\"Cacahuete tostado sin sal\":\"Cacahuetes\",\"FRESON\":\"Fresas\",\"creado\":\"Pan de hogaza\",\"Mostaza\":\"Mostaza\",\"Atun Claro\":\"Lata de atun\",\"Arroz Integral\":\"Arroz integral\",\"Huevo Entero\":\"Huevos\",\"Tomate Frito Con Aceite De Oliva Virgen Extra\":\"Bote de tomate frito\",\"Pollo Asado\":\"Pollo guisado\",\"patata bolsa\":\"Patatas baby\",\"Lenteja Pardina\":\"Lentejas\",\"Ensalada\":\"Ensalight\",\"ketchup\":\"Ketchup\",\"jamon\":\"Jamon york\",\"Jamon Serrano\":\"Jamon Serrano\",\"filete de pechuga(pollo)\":\"Filetes pechuga de pollo\",\"Tomates Frescos\":\"Tomates\",\"Queso Gouda Tierno\":\"Queso Gouda\",\"Pan burger integral\":\"Pan de hamburguesa integral\",\"Tiras De Bacon\":\"Tiras/Tacos de Bacon\",\"Bacon\":\"Bacon para lentejas\",\"Integral Familiar\":\"Pan rebanadas integral\",\"Vino Blanco\":\"Vino Blanco\",\"chimichurri\":\"Salsa Chimichurri\",\"salsa worcestershire\":\"Salsa Worcestershire (Perrins)\",\"Aceite De Oliva Virgen\":\"Aceite\",\"Pizza fresca 4 quesos\":\"Pizza 4 Quesos\",\"Pechuga De Pavo Reducido En Sal\":\"Pechuga De Pavo\",\"Lomo Embuchado Extra\":\"Lomo Embuchado\"},\"unit_aliases\":[\"pieza\",\"piezas\",\"pieces\",\"piece\",\"u\",\"ud\",\"uds\",\"units\",\"unit\",\"unidades\",\"unidad\"],\"buy_measured\":[\"Filetes pechuga de pollo\",\"Jamon york\",\"carne picada\",\"Langostino\",\"Chipirones\",\"Salmon Ahumado\",\"Pan de hamburguesa integral\",\"Ensalight\",\"Lata de atun\",\"Filete De Cerdo\",\"Filetes De Ternera\"],\"conversions\":{\"Lata de atun\":50,\"Kiwi\":60,\"Patatas baby\":400,\"Ensalight\":230,\"Queso Light Havarti\":25,\"queso gouda\":25,\"ñoquis\":500,\"Pan de hamburguesa integral\":85,\"Jamon Serrano\":10,\"Banana\":140,\"Vino Blanco\":-1,\"Lechuga\":-1,\"Bote de tomate frito\":-1,\"Pimiento\":-1,\"Cebolla\":-1,\"Ajo Diente\":3,\"Tomate Triturado Natural\":-1,\"Laurel\":0.5,\"Queso en polvo\":-1},\"dated_food\":{\"Pan de hogaza\":[1]},\"p_sacar\":[\"Huevo\",\"Vino\",\"Jamon Serrano\",\"Pan de hogaza\",\"Bote de tomate frito\",\"Laurel\",\"Tomate Triturado Natural\",\"Queso en polvo\"],\"p_pesar\":[\"Aguacate\",\"Pollo\",\"Uvas\",\"Fresas\",\"Manzana\",\"cacahuete\",\"Pistacho\",\"arroz\",\"Aceite\",\"Salmon\",\"Spaghetti\",\"Contra muslo\",\"Kiwi\",\"Banana\",\"Lata de atun\",\"Lechuga\",\"Jamon York\",\"Lomo Embuchado\",\"Pechuga De Pavo\",\"Empanada\",\"Sandia\",\"Nectarina\",\"Ciruela\",\"Cerezas\",\"Paraguayo\",\"Ensalight\",\"Langostino\",\"Chipirones\",\"Melocoton\",\"Pan de hogaza\",\"Melon\"],\"p_picar\":[\"Pimiento\",\"Ajo Diente\",\"Cebolla\"]}";

	// new/use [user password][json P/B 2 2 2 0123]
	public static void main(String[] args) {
		try {
			switch (args[0]) {
			case "new":
				requestSession(args[1], args[2]);
				break;
			case "use":
				if (args.length == 6 || args.length == 7) {
					ListerData lc = new ListerData(CONFIG_DATA);
					lc.load();
					generateList(args[1], lc, args[2], args[3], args[4], args[5], args.length == 6 ? "" : args[6]);
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
