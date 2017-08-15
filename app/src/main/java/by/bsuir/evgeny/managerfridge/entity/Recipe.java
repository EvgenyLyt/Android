package by.bsuir.evgeny.managerfridge.entity;

import java.util.ArrayList;

public class Recipe {
    private String name="";
    private String category="";
    private String time="";
    private String instruction="";
    private String id="";

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    private ArrayList<Product> ingredients = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public ArrayList<Product> getIngredients() {
        return ingredients;
    }

    public void setIngredients(ArrayList<Product> ingredients) {
        this.ingredients = ingredients;
    }

    public Recipe(String _name, String _category, String _time, String _instruction, ArrayList<Product> products){
        name=_name;
        category=_category;
        time=_time;
        instruction=_instruction;
        ingredients=products;
    }

    public Recipe(){

    }

}
