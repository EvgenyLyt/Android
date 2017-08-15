package by.bsuir.evgeny.managerfridge.entity;

import java.util.ArrayList;

public class SavedRecipe {
    private static String category="";
    private static String time="";
    private static String instruction="";
    private static String name="";
    private static String id ="";
    private static ArrayList<Product> ingredients = new ArrayList<>();
    private static ArrayList<String> found_recipes = new ArrayList<>();

    public static String getCategory() {
        return category;
    }

    public static void setCategory(String category) {
        SavedRecipe.category = category;
    }

    public static ArrayList<Product> getIngredients() {
        return ingredients;
    }

    public static void setIngredients(ArrayList<Product> ingredients) {
        SavedRecipe.ingredients = ingredients;
    }

    public static String getTime() {
        return time;
    }

    public static void setTime(String time) {
        SavedRecipe.time = time;
    }

    public static String getInstruction() {
        return instruction;
    }

    public static void setInstruction(String instruction) {
        SavedRecipe.instruction = instruction;
    }

    public static String getName() {
        return name;
    }

    public static void setName(String name) {
        SavedRecipe.name = name;
    }

    public static String getId() {
        return id;
    }

    public static void setId(String id) {
        SavedRecipe.id = id;
    }

    public static ArrayList<String> getFound_recipes() {
        return found_recipes;
    }

    public static void setFound_recipes(ArrayList<String> found_recipes) {
        SavedRecipe.found_recipes = found_recipes;
    }

    public static void cleanAll(){
        category="";
        id="";
        time="";
        instruction="";
        name="";
        ingredients.clear();
        found_recipes.clear();
    }
}

