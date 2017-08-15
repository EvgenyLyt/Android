package by.bsuir.evgeny.managerfridge.entity;

import java.util.ArrayList;

public class SummaryRecipe {
    private String name="";
    private String time="";
    private String ingredients="";
    private String count="";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }

    public SummaryRecipe(String _name, ArrayList<Product> _ingredients, int _count, String _time){
        ingredients="";
        for (Product ing: _ingredients)
            ingredients+=ing.getProduct_name()+"; ";
        name=_name;
        count=String.valueOf(_count)+"/"+String.valueOf(_ingredients.size());
        time=_time;
    }
}
