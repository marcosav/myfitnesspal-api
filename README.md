# myfitnesspal-api
API for accessing MyFitnessPal diary food data 

Work in progress, more documentation/examples coming soon...

Create a new session as follows:

```java
IMFPSession session = MFPSession.create(user, password);
```

Access to diary food fetcher with:

```java
Diary diary = session.toDiary();
```

Then, use it like so:
```java
// Get one specific day data
FoodDay day = diary.getDay(date);

// Or multiple days...
List<FoodDay> days = diary.getDayRange(dates);

// You can specify which meals you want by setting their indices
day = diary.getDay(date, "012"); // Same applies for multi-day queries

// Fetch water amount and notes
int water = diary.getWater(date);
String notes = diary.getNotes(date);

// Get day meals
DiaryMeal dinner = day.getMeals().get(2);
String name = dinner.getName();

// Get day nutrients
float carbs = dinner.get(FoodValues.CARBOHYDRATES);
float calcium = dinner.get(FoodValues.CALCIUM);

// Get food info
dinner.getFood().forEach(food -> {
  String name = food.getName();
  String brand = food.getBrand();
  String unit = food.getUnit();
  float amnt = food.getAmount();
  float energy = food.getEnergy();
  
  // You can also fetch food nutrients
  float protein = food.get(FoodValues.PROTEIN);
  ...
});
```

Check user data and settings using ```UserData```:

```java
UserData userData = session.toUser();

String energyUnit = userData.getUnit(Unit.ENERGY); // calories
String weightUnit = userData.getUnit(Unit.WEIGHT); // kg

List<String> meals = userData.getMealNames(); // ["Breakfast", "Launch"...]

String email = userData.getEmail();
...
```
