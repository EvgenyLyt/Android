package by.bsuir.evgeny.managerfridge.database;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashMap;

import by.bsuir.evgeny.managerfridge.MessageBox;
import by.bsuir.evgeny.managerfridge.entity.Name;
import by.bsuir.evgeny.managerfridge.entity.Product;
import by.bsuir.evgeny.managerfridge.entity.Recipe;
import by.bsuir.evgeny.managerfridge.entity.SummaryRecipe;

public class Database {
    private SQLiteDatabase db;
    public Helper dbHelper;
    private Context context;

    public Helper getHelper() {
        return dbHelper;
    }

    public Database(Context context){
        this.context=context;
        dbHelper = new Helper(context);
    }

    public void openDB(){
        db = dbHelper.open();
    }

    public void updateFavs(boolean fridge, String name, int value){
        ContentValues cv = new ContentValues();
        String field=(fridge)?Helper.COLUMN_PRODUCT_FAVORITE:Helper.COLUMN_PRODUCT_BOX_FAVORITE;
        cv.put(field,value);
        db.update(Helper.TABLE_PRODUCT,cv,Helper.COLUMN_PRODUCT_NAME+ " = ?", new String[] {name});
    }

    public int findFavs(boolean fridge, String name){
        int result=0;
        String field = (fridge)?Helper.COLUMN_PRODUCT_FAVORITE:Helper.COLUMN_PRODUCT_BOX_FAVORITE;
        String query= String.format("select %s from %s where %s=%s",field,Helper.TABLE_PRODUCT,Helper.COLUMN_PRODUCT_NAME,"\'" + name + "\'");
        Cursor cursor = db.rawQuery(query,null);
        while (cursor.moveToNext())
            result = cursor.getInt(cursor.getColumnIndex(field));
        cursor.close();
        return result;
    }

    public void fillItems(ArrayList<String> allnames, ArrayList<HashMap<String, Name>> allitems, ArrayList<Product> products, String description, String condition){
        try{
            String query = String.format("select %s from %s",Helper.COLUMN_TYPE_PRODUCT_NAME,Helper.TABLE_TYPE_PRODUCT);
            Cursor cursor =  db.rawQuery(query, null);
            while (cursor.moveToNext())
                allnames.add(cursor.getString(cursor.getColumnIndex(Helper.COLUMN_TYPE_PRODUCT_NAME)));
            for  (int i=0; i<allnames.size(); i++)
            {
                query = String.format("select %s, %s, %s from %s where %s = %s",Helper.COLUMN_PRODUCT_NAME,description,
                        condition,Helper.TABLE_PRODUCT,Helper.COLUMN_PRODUCT_TYPE,String.valueOf(i+1));
                cursor =  db.rawQuery(query, null);
                while (cursor.moveToNext()) {
                    HashMap<String, Name> map = new HashMap<String, Name>();
                    int list = cursor.getInt(cursor.getColumnIndex(condition));
                    boolean in_list = (list==1);
                    Name name = new Name(cursor.getString(cursor.getColumnIndex(Helper.COLUMN_PRODUCT_NAME)), in_list);
                    map.put(allnames.get(i), name);
                    allitems.add(map);
                    if (in_list)
                        products.add(new Product(name.getName_product(),cursor.getString(cursor.getColumnIndex(description)),false));
                }
            }
            cursor.close();
        }
        catch (Exception ex){
            MessageBox.Show(context,"Database",ex.toString()+"\n"+ex.getMessage());
        }
    }

    public void fillItems(ArrayList<String> allnames, ArrayList<HashMap<String, Name>> allitems){
        try{
            String query = String.format("select %s from %s",Helper.COLUMN_TYPE_PRODUCT_NAME,Helper.TABLE_TYPE_PRODUCT);
            Cursor cursor =  db.rawQuery(query, null);
            while (cursor.moveToNext())
                allnames.add(cursor.getString(cursor.getColumnIndex(Helper.COLUMN_TYPE_PRODUCT_NAME)));
            for  (int i=0; i<allnames.size(); i++)
            {
                query = String.format("select %s, %s, %s from %s where %s = %s",Helper.COLUMN_PRODUCT_NAME,Helper.COLUMN_PRODUCT_DESCRIPTION,
                        Helper.COLUMN_PRODUCT_FRIDGE,Helper.TABLE_PRODUCT,Helper.COLUMN_PRODUCT_TYPE,String.valueOf(i+1));
                cursor =  db.rawQuery(query, null);
                while (cursor.moveToNext()) {
                    HashMap<String, Name> map = new HashMap<String, Name>();
                    Name name = new Name(cursor.getString(cursor.getColumnIndex(Helper.COLUMN_PRODUCT_NAME)), false);
                    map.put(allnames.get(i), name);
                    allitems.add(map);
                }
            }
            cursor.close();
        }
        catch (Exception ex){
            MessageBox.Show(context,"Database",ex.toString()+"\n"+ex.getMessage());
        }
    }

    public void deleteRecipe(String id){
        try {
            db.delete(Helper.TABLE_RECIPE_HAS_PRODUCTS,Helper.COLUMN_PRODUCT_RECIPE+" = ?",new String[]{id});
            db.delete(Helper.TABLE_RECIPE,Helper.COLUMN_RECIPE_ID+" = ?",new String[]{id});
        }
        catch (Exception ex){
            MessageBox.Show(context,"Database",ex.toString()+"\n"+ex.getMessage());
        }
    }

    public void addToFridge(String product_name, String product_desc){
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(Helper.COLUMN_PRODUCT_BOX, 0);
            contentValues.put(Helper.COLUMN_PRODUCT_FRIDGE, 1);
            contentValues.put(Helper.COLUMN_PRODUCT_BOX_DESCRIPTION, "");
            contentValues.put(Helper.COLUMN_PRODUCT_DESCRIPTION, product_desc);
            db.update(Helper.TABLE_PRODUCT, contentValues, Helper.COLUMN_PRODUCT_NAME + " = ?", new String[]{product_name});
        }
        catch (Exception ex){
            MessageBox.Show(context,"Database",ex.toString()+"\n"+ex.getMessage());
        }
    }

    public void addToBasket(String product_name, String product_desc){
        try{
            ContentValues contentValues = new ContentValues();
            contentValues.put(Helper.COLUMN_PRODUCT_BOX,1);
            contentValues.put(Helper.COLUMN_PRODUCT_BOX_DESCRIPTION,product_desc);
            db.update(Helper.TABLE_PRODUCT,contentValues,Helper.COLUMN_PRODUCT_NAME+ " = ?", new String[] {product_name});}
        catch (Exception ex){
            MessageBox.Show(context,"Database",ex.toString()+"\n"+ex.getMessage());
        }
    }

    public ArrayList<String> findRecipe(ArrayList<String> names){
        ArrayList<String> id = new ArrayList<>();
        try {
            String condition = Helper.COLUMN_PRODUCT_NAME + "=";
            if (names.isEmpty())
                condition += "\'" + "\'";
            else {
                condition += "\'" + names.get(0) + "\'";
                for (int i = 1; i < names.size(); i++) {
                    condition += " or ";
                    condition += Helper.COLUMN_PRODUCT_NAME + "=" + "\'" + names.get(i) + "\'";
                }
            }
            String query = String.format("select %s from %s join %s on %s=%s join %s on %s=%s where %s", Helper.COLUMN_RECIPE_NAME, Helper.TABLE_PRODUCT,
                    Helper.TABLE_RECIPE_HAS_PRODUCTS, Helper.COLUMN_PRODUCT_ID, Helper.COLUMN_RECIPE_PRODUCT, Helper.TABLE_RECIPE,
                    Helper.COLUMN_RECIPE_ID, Helper.COLUMN_PRODUCT_RECIPE, condition);
            Cursor cursor = db.rawQuery(query, null);
            while (cursor.moveToNext()) {
                id.add(cursor.getString(cursor.getColumnIndex(Helper.COLUMN_RECIPE_NAME)));
            }
            cursor.close();
        }
        catch (Exception ex){
            MessageBox.Show(context,"Database",ex.toString()+"\n"+ex.getMessage());
        }
        return id;
    }

    public void searchRecipe(String recipe_name, Recipe recipe){
        try {
            int id = 0;
            String name_recipe = "\'" + recipe_name + "\'";
            String query = String.format("select %s, %s, %s, %s from %s where %s = %s", Helper.COLUMN_RECIPE_ID, Helper.COLUMN_RECIPE_TIME, Helper.COLUMN_RECIPE_INSTRUCTION,
                    Helper.COLUMN_RECIPE_CATEGORY, Helper.TABLE_RECIPE, Helper.COLUMN_RECIPE_NAME, name_recipe);
            Cursor cursor = db.rawQuery(query, null);
            while (cursor.moveToNext()) {
                recipe.setName(recipe_name);
                ArrayList<String> categories = new ArrayList<>();
                fillCategories(categories);
                recipe.setCategory(categories.get(cursor.getInt(cursor.getColumnIndex(Helper.COLUMN_RECIPE_CATEGORY)) - 1));
                recipe.setTime(cursor.getString(cursor.getColumnIndex(Helper.COLUMN_RECIPE_TIME)));
                recipe.setInstruction(cursor.getString(cursor.getColumnIndex(Helper.COLUMN_RECIPE_INSTRUCTION)));
                recipe.setId(String.valueOf(cursor.getInt(cursor.getColumnIndex(Helper.COLUMN_RECIPE_ID))));
                String new_query = String.format("select %s, %s, %s from %s join %s on %s=%s join %s on %s=%s where %s=%s", Helper.COLUMN_PRODUCT_NAME, Helper.COLUMN_RECIPE_PRODUCT_DESCRIPTION, Helper.COLUMN_PRODUCT_FRIDGE, Helper.TABLE_RECIPE, Helper.TABLE_RECIPE_HAS_PRODUCTS, Helper.COLUMN_PRODUCT_RECIPE, Helper.COLUMN_RECIPE_ID, Helper.TABLE_PRODUCT, Helper.COLUMN_PRODUCT_ID, Helper.COLUMN_RECIPE_PRODUCT, Helper.COLUMN_RECIPE_NAME, name_recipe);
                Cursor new_cursor = db.rawQuery(new_query, null);
                ArrayList<Product> products = new ArrayList<>();
                while (new_cursor.moveToNext()) {
                    int fridge = new_cursor.getInt(new_cursor.getColumnIndex(Helper.COLUMN_PRODUCT_FRIDGE));
                    String name = new_cursor.getString(new_cursor.getColumnIndex(Helper.COLUMN_PRODUCT_NAME));
                    String desc = new_cursor.getString(new_cursor.getColumnIndex(Helper.COLUMN_RECIPE_PRODUCT_DESCRIPTION));
                    Product product = new Product(name, desc, fridge == 1);
                    products.add(product);
                }
                new_cursor.close();
                recipe.setIngredients(products);
            }
            cursor.close();
        }
        catch (Exception ex){
            MessageBox.Show(context,"Database",ex.toString()+"\n"+ex.getMessage());
        }
    }

    public void fillRecipes(ArrayList<String> categories, ArrayList<HashMap<String, SummaryRecipe>> items_recipes, ArrayList<Recipe> recipes){
        try{
            for  (int i=0; i<categories.size(); i++)
            {
                String query = String.format("select %s, %s, %s from %s where %s = %s",Helper.COLUMN_RECIPE_NAME,Helper.COLUMN_RECIPE_TIME,
                        Helper.COLUMN_RECIPE_INSTRUCTION,Helper.TABLE_RECIPE,Helper.COLUMN_RECIPE_CATEGORY,String.valueOf(i+1));
                Cursor cursor =  db.rawQuery(query, null);
                while (cursor.moveToNext()) {
                    Recipe recipe = new Recipe();
                    recipe.setName(cursor.getString(cursor.getColumnIndex(Helper.COLUMN_RECIPE_NAME)));
                    recipe.setInstruction(cursor.getString(cursor.getColumnIndex(Helper.COLUMN_RECIPE_INSTRUCTION)));
                    recipe.setTime(cursor.getString(cursor.getColumnIndex(Helper.COLUMN_RECIPE_TIME)));
                    recipe.setCategory(categories.get(i));
                    String name_table = "\'"+cursor.getString(cursor.getColumnIndex(Helper.COLUMN_RECIPE_NAME))+"\'";
                    HashMap<String, SummaryRecipe> map = new HashMap<String, SummaryRecipe>();
                    String new_query = String.format("select %s, %s, %s from %s join %s on %s=%s join %s on %s=%s where %s=%s",Helper.COLUMN_PRODUCT_NAME,
                            Helper.COLUMN_RECIPE_PRODUCT_DESCRIPTION, Helper.COLUMN_PRODUCT_FRIDGE, Helper.TABLE_RECIPE, Helper.TABLE_RECIPE_HAS_PRODUCTS,
                            Helper.COLUMN_PRODUCT_RECIPE,Helper.COLUMN_RECIPE_ID, Helper.TABLE_PRODUCT,Helper.COLUMN_PRODUCT_ID,Helper.COLUMN_RECIPE_PRODUCT,
                            Helper.COLUMN_RECIPE_NAME, name_table);
                    Cursor new_cursor = db.rawQuery(new_query,null);
                    ArrayList<Product> products = new ArrayList<>();
                    int count=0;
                    while (new_cursor.moveToNext()){
                        int fridge = new_cursor.getInt(new_cursor.getColumnIndex(Helper.COLUMN_PRODUCT_FRIDGE));
                        if (fridge==1) count++;
                        String name = new_cursor.getString(new_cursor.getColumnIndex(Helper.COLUMN_PRODUCT_NAME));
                        String desc = new_cursor.getString(new_cursor.getColumnIndex(Helper.COLUMN_RECIPE_PRODUCT_DESCRIPTION));
                        Product product = new Product(name, desc, fridge==1);
                        products.add(product);
                    }
                    new_cursor.close();
                    recipe.setIngredients(products);
                    map.put(categories.get(i), new SummaryRecipe(recipe.getName(),recipe.getIngredients(),count,recipe.getTime()));
                    items_recipes.add(map);
                    recipes.add(recipe);
                }
                cursor.close();
            }
        }
        catch (Exception ex){
            MessageBox.Show(context,"Database",ex.toString()+"\n"+ex.getMessage());
        }
    }

    public void fillRecipes(ArrayList<String> categories, ArrayList<Recipe> recipes){
        try{
            for  (int i=0; i<categories.size(); i++)
            {
                String query = String.format("select %s, %s, %s from %s where %s = %s",Helper.COLUMN_RECIPE_NAME,Helper.COLUMN_RECIPE_TIME,
                        Helper.COLUMN_RECIPE_INSTRUCTION,Helper.TABLE_RECIPE,Helper.COLUMN_RECIPE_CATEGORY,String.valueOf(i+1));
                Cursor cursor =  db.rawQuery(query, null);
                while (cursor.moveToNext()) {
                    Recipe recipe = new Recipe();
                    recipe.setName(cursor.getString(cursor.getColumnIndex(Helper.COLUMN_RECIPE_NAME)));
                    recipe.setInstruction(cursor.getString(cursor.getColumnIndex(Helper.COLUMN_RECIPE_INSTRUCTION)));
                    recipe.setTime(cursor.getString(cursor.getColumnIndex(Helper.COLUMN_RECIPE_TIME)));
                    recipe.setCategory(categories.get(i));
                    String name_table = "\'"+cursor.getString(cursor.getColumnIndex(Helper.COLUMN_RECIPE_NAME))+"\'";
                    String new_query = String.format("select %s, %s, %s from %s join %s on %s=%s join %s on %s=%s where %s=%s",Helper.COLUMN_PRODUCT_NAME,Helper.COLUMN_RECIPE_PRODUCT_DESCRIPTION,
                            Helper.COLUMN_PRODUCT_FRIDGE, Helper.TABLE_RECIPE, Helper.TABLE_RECIPE_HAS_PRODUCTS, Helper.COLUMN_PRODUCT_RECIPE,Helper.COLUMN_RECIPE_ID,
                            Helper.TABLE_PRODUCT,Helper.COLUMN_PRODUCT_ID,Helper.COLUMN_RECIPE_PRODUCT,Helper.COLUMN_RECIPE_NAME, name_table);
                    Cursor new_cursor = db.rawQuery(new_query,null);
                    ArrayList<Product> products = new ArrayList<>();
                    while (new_cursor.moveToNext()){
                        int fridge = new_cursor.getInt(new_cursor.getColumnIndex(Helper.COLUMN_PRODUCT_FRIDGE));
                        String name = new_cursor.getString(new_cursor.getColumnIndex(Helper.COLUMN_PRODUCT_NAME));
                        String desc =new_cursor.getString(new_cursor.getColumnIndex(Helper.COLUMN_RECIPE_PRODUCT_DESCRIPTION));
                        Product product = new Product(name, desc, fridge==1);
                        products.add(product);
                    }
                    new_cursor.close();
                    recipe.setIngredients(products);
                    recipes.add(recipe);
                }
                cursor.close();
            }
        }
        catch (Exception ex){
            MessageBox.Show(context,"Database",ex.toString()+"\n"+ex.getMessage());
        }
    }

    public void fillCategories(ArrayList<String> categories){
        try{
            String query = String.format("select %s from %s",Helper.COLUMN_CATEGORY_NAME,Helper.TABLE_CATEGORY);
            Cursor cursor =  db.rawQuery(query, null);
            while (cursor.moveToNext())
                categories.add(cursor.getString(cursor.getColumnIndex(Helper.COLUMN_CATEGORY_NAME)));
            cursor.close();
        }
        catch (Exception ex){
            MessageBox.Show(context,"Database",ex.toString()+"\n"+ex.getMessage());
        }
    }

    public void addProduct(String name, String desc, String _description, String _condition, String _fav){
        try {
            ContentValues content = new ContentValues();
            content.put(_condition, 1);
            content.put(_description, desc);
            db.update(Helper.TABLE_PRODUCT, content, Helper.COLUMN_PRODUCT_NAME + " = ?", new String[]{name});
        }
        catch (Exception ex){
            MessageBox.Show(context,"Database",ex.toString()+"\n"+ex.getMessage());
        }
    }

    public void deleteProduct(String name, String desc, String _description, String _condition, String _fav){
        try {
            ContentValues content = new ContentValues();
            content.put(_condition, 0);
            content.put(_fav, 0);
            content.put(_description, desc);
            db.update(Helper.TABLE_PRODUCT, content, Helper.COLUMN_PRODUCT_NAME + " = ?", new String[]{name});
        }
        catch (Exception ex){
            MessageBox.Show(context,"Database",ex.toString()+"\n"+ex.getMessage());
        }

    }

    public void addRecipe(String id, String name, int category, String time, String instruction, ArrayList<Product> ingredients){
        try {
            int new_id = 0;
            ContentValues content = new ContentValues();
            content.put(Helper.COLUMN_RECIPE_NAME, name);
            content.put(Helper.COLUMN_RECIPE_CATEGORY, category);
            content.put(Helper.COLUMN_RECIPE_TIME, time);
            content.put(Helper.COLUMN_RECIPE_INSTRUCTION, instruction);
            if (id.isEmpty()) {
                db.insert(Helper.TABLE_RECIPE, null, content);
                String name_recipe = "\'" + name + "\'";
                String search_id = String.format("select %s from %s where %s = %s", Helper.COLUMN_RECIPE_ID, Helper.TABLE_RECIPE,
                        Helper.COLUMN_RECIPE_NAME, name_recipe);
                Cursor curs = db.rawQuery(search_id, null);
                while (curs.moveToNext()) {
                    new_id = curs.getInt(curs.getColumnIndex(Helper.COLUMN_RECIPE_ID));
                }
                curs.close();
            } else
                db.update(Helper.TABLE_RECIPE, content, Helper.COLUMN_RECIPE_ID + " = ?", new String[]{id});
            if (!id.isEmpty())
                db.delete(Helper.TABLE_RECIPE_HAS_PRODUCTS, Helper.COLUMN_PRODUCT_RECIPE + " = ?", new String[]{id});
            for (Product p : ingredients) {
                ContentValues insertValues = new ContentValues();
                String name_product = "\'" + p.getProduct_name() + "\'";
                String query = String.format("select %s from %s where %s = %s", Helper.COLUMN_PRODUCT_ID, Helper.TABLE_PRODUCT,
                        Helper.COLUMN_PRODUCT_NAME, name_product);
                Cursor cursor = db.rawQuery(query, null);
                while (cursor.moveToNext()) {
                    if (id.isEmpty())
                        insertValues.put(Helper.COLUMN_PRODUCT_RECIPE, new_id);
                    else
                        insertValues.put(Helper.COLUMN_PRODUCT_RECIPE, Integer.valueOf(id));
                    insertValues.put(Helper.COLUMN_RECIPE_PRODUCT_DESCRIPTION, p.getProduct_descripton());
                    insertValues.put(Helper.COLUMN_RECIPE_PRODUCT, cursor.getInt(cursor.getColumnIndex(Helper.COLUMN_PRODUCT_ID)));
                }
                cursor.close();
                db.insert(Helper.TABLE_RECIPE_HAS_PRODUCTS, null, insertValues);
            }
        }
        catch (Exception ex){
            MessageBox.Show(context,"Database",ex.toString()+"\n"+ex.getMessage());
        }
    }

    public void closeDB(){
        if (db!=null)
            db.close();
    }
}