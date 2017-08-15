package by.bsuir.evgeny.managerfridge.activity;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import by.bsuir.evgeny.managerfridge.ListViewHeight;
import by.bsuir.evgeny.managerfridge.MessageBox;
import by.bsuir.evgeny.managerfridge.R;
import by.bsuir.evgeny.managerfridge.adapter.IngredientAdapter;
import by.bsuir.evgeny.managerfridge.adapter.RecipeIngredientAdapter;
import by.bsuir.evgeny.managerfridge.database.Database;
import by.bsuir.evgeny.managerfridge.database.Helper;
import by.bsuir.evgeny.managerfridge.entity.Name;
import by.bsuir.evgeny.managerfridge.entity.Product;
import by.bsuir.evgeny.managerfridge.entity.Recipe;
import by.bsuir.evgeny.managerfridge.entity.SavedRecipe;

class AddRecipeImpl {
    private Context context;
    private Activity activity;
    private ArrayList<Product> ingredients = new ArrayList<>();
    private ArrayList<String> product_categories = new ArrayList<>();
    private ArrayList<HashMap<String, Name>> products = new ArrayList<>();
    private ArrayList<String> recipe_categories = new ArrayList<>();
    private ArrayList<Recipe> recipes = new ArrayList<>();
    private RecipeIngredientAdapter ingredientAdapter;

    AddRecipeImpl(Context context){
        this.context=context;
        this.activity=(Activity)context;
    }

    void makeComponents(){
        activity.setTitle("Добавить рецепт");
        ListView lvProducts = (ListView) activity.findViewById(R.id.list_recipe_products);
        EditText edtProduct = (EditText) activity.findViewById(R.id.edit_name_recipe_product);
        TextView btnBackToCategory = (TextView) activity.findViewById(R.id.back_to_list_recipe_products);
        ListView lvIngredients = (ListView) activity.findViewById(R.id.list_recipe_ingredients);
        TextView btnSaveRecipe = (TextView) activity.findViewById(R.id.btn_save_recipe);
        ImageButton btnSearch = (ImageButton) activity.findViewById(R.id.btn_add_recipe_list_recipes);
        ImageButton btnFridge = (ImageButton) activity.findViewById(R.id.btn_add_recipe_fridge);
        ImageButton btnBasket = (ImageButton) activity.findViewById(R.id.btn_add_recipe_basket);
        initializeDB(context);
        initializeData();
        lvIngredients.setAdapter(new IngredientAdapter(context,ingredients));
        ListViewHeight.set(lvIngredients);
        lvProducts.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, product_categories.toArray(new String[0])));
        lvProducts.setTag("List");
        lvProducts.setOnItemClickListener(clickCategory);
        btnBackToCategory.setOnClickListener(backToList);
        edtProduct.addTextChangedListener(searchProduct);
        lvIngredients.setOnItemClickListener(clickIngredient);
        btnSaveRecipe.setOnClickListener(saveRecipe);
        btnSearch.setOnClickListener(clickSearch);
        btnFridge.setOnClickListener(clickFridge);
        btnBasket.setOnClickListener(clickBasket);
    }


    private void initializeDB(Context context){
        try {
            Database db = new Database(context);
            File database = activity.getApplicationContext().getDatabasePath(Helper.DB_NAME);
            if (!database.exists())
                db.dbHelper.createDB(db.getHelper());
            db.openDB();
            db.fillItems(product_categories,products);
            db.fillCategories(recipe_categories);
            db.fillRecipes(recipe_categories, recipes);
            db.closeDB();
        }
        catch (Exception ex){
            MessageBox.Show(context,activity.getTitle().toString(),ex.toString()+"\n"+ex.getMessage());
        }
    }

    private void initializeData(){
        initializeSpinner();
        ingredients = SavedRecipe.getIngredients();
        EditText instruction = (EditText) activity.findViewById(R.id.edit_recipe_instruction);
        EditText name = (EditText) activity.findViewById(R.id.edit_recipe_name);
        EditText time = (EditText) activity.findViewById(R.id.edit_recipe_time);
        instruction.setText(SavedRecipe.getInstruction());
        name.setText(SavedRecipe.getName());
        time.setText(SavedRecipe.getTime());
    }

    private void initializeSpinner(){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, recipe_categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        final Spinner spinner = (Spinner) activity.findViewById(R.id.sp_recipe_categories);
        spinner.setAdapter(adapter);
        spinner.setSelection(recipe_categories.indexOf(SavedRecipe.getCategory()));
    }

    private AdapterView.OnItemClickListener clickIngredient = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
            LayoutInflater li = LayoutInflater.from(context);
            View promptsView = li.inflate(R.layout.dialog, null);
            AlertDialog.Builder mDialogBuilder = new AlertDialog.Builder(context);
            mDialogBuilder.setView(promptsView);
            final EditText tv_description = (EditText) promptsView.findViewById(R.id.dialog_text_product_description);
            final TextView tv_name = (TextView) promptsView.findViewById(R.id.dialog_text_product_name);
            tv_description.setText(ingredients.get(position).getProduct_descripton());
            tv_name.setText(ingredients.get(position).getProduct_name());
            mDialogBuilder.setCancelable(false).setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int id) {
                            if (tv_description.getText().toString().length()>20)
                                ingredients.get(position).setProduct_descripton(tv_description.getText().toString().substring(0,20));
                            else
                                ingredients.get(position).setProduct_descripton(tv_description.getText().toString());
                            ListView list = (ListView) activity.findViewById(R.id.list_recipe_ingredients);
                            list.setAdapter(new IngredientAdapter(context,ingredients));
                            ListViewHeight.set(list);
                        }
                    }).setNegativeButton("Отмена",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog dialog = mDialogBuilder.create();
            dialog.show();
        }
    };

    private View.OnClickListener saveRecipe = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            createRecipe();
            String error = checkEmpty();
            if (!error.isEmpty())
                MessageBox.Show(context,activity.getTitle().toString(),error.trim());
            else
                if (checkName(SavedRecipe.getName())&& SavedRecipe.getId().isEmpty())
                    MessageBox.Show(context,activity.getTitle().toString(),"Такое название рецепта уже есть в базе.");
                else
                {
                    Database db = new Database(context);
                    db.openDB();
                    db.addRecipe(SavedRecipe.getId(),SavedRecipe.getName(),recipe_categories.indexOf(SavedRecipe.getCategory())+1,
                        SavedRecipe.getTime(), SavedRecipe.getInstruction(), SavedRecipe.getIngredients());
                    db.closeDB();
                    cleanAll();
                    MessageBox.Show(context,activity.getTitle().toString(),"Рецепт успешно сохранен.");
                }
        }
    };

    private void cleanAll(){
        EditText instruction = (EditText) activity.findViewById(R.id.edit_recipe_instruction);
        EditText name = (EditText) activity.findViewById(R.id.edit_recipe_name);
        EditText time = (EditText) activity.findViewById(R.id.edit_recipe_time);
        Spinner category = (Spinner) activity.findViewById(R.id.sp_recipe_categories);
        SavedRecipe.cleanAll();
        category.setSelection(0);
        name.setText(SavedRecipe.getName());
        time.setText(SavedRecipe.getTime());
        instruction.setText(SavedRecipe.getInstruction());
        ingredients = SavedRecipe.getIngredients();
        ListView lvIngredients = (ListView) activity.findViewById(R.id.list_recipe_ingredients);
        lvIngredients.setAdapter(new IngredientAdapter(context,ingredients));
        ListViewHeight.set(lvIngredients);
    }

    private boolean checkName(String name){
        for (Recipe recipe: recipes)
            if (recipe.getName().equals(name))
                return true;
        return false;
    }

    private String checkEmpty(){
        String error="";
        error += (SavedRecipe.getName().isEmpty()) ? "Введите название рецепта.\n" : "" ;
        error += (SavedRecipe.getCategory().isEmpty()) ? "Введите категорию.\n" : "";
        error += (SavedRecipe.getTime().isEmpty()) ? "Введите время приготовления.\n" : "";
        error += (SavedRecipe.getInstruction().isEmpty()) ? "Введите рецепт.\n" : "";
        error += (SavedRecipe.getIngredients().isEmpty()) ? "Добавьте ингредиенты.\n" : "";
        return error;
    }

    private void createRecipe(){
        EditText instruction = (EditText) activity.findViewById(R.id.edit_recipe_instruction);
        EditText name = (EditText) activity.findViewById(R.id.edit_recipe_name);
        EditText time = (EditText) activity.findViewById(R.id.edit_recipe_time);
        Spinner category = (Spinner) activity.findViewById(R.id.sp_recipe_categories);
        SavedRecipe.setName(name.getText().toString().trim());
        SavedRecipe.setCategory(category.getSelectedItem().toString().trim());
        SavedRecipe.setInstruction(instruction.getText().toString().trim());
        SavedRecipe.setTime(time.getText().toString().trim());
        SavedRecipe.setIngredients(ingredients);
    }

    private TextWatcher searchProduct = new TextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
        }
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            ListView lvProducts = (ListView) activity.findViewById(R.id.list_recipe_products);
            if (s.length() <= 2) {
                TextView back = (TextView) activity.findViewById(R.id.back_to_list_recipe_products);
                ViewGroup.LayoutParams params = back.getLayoutParams();
                params.height = 0;
                back.setLayoutParams(params);
                lvProducts.setTag("List");
                lvProducts.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, product_categories.toArray(new String[0])));
            } else {
                ArrayList<Name> foundProducts = new ArrayList<>();
                ingredientAdapter = new RecipeIngredientAdapter(context, foundProducts, ingredients);
                for (Map<String, Name> w : products) {
                    int i = 0;
                    while (w.get(product_categories.get(i)) == null) i++;
                    Name product = w.get(product_categories.get(i));
                    if (product.getName_product().contains(s)) {
                        product.setCheck_product(searchName(product.getName_product()));
                        foundProducts.add(product);
                    }
                }
                lvProducts.setAdapter(ingredientAdapter);
            }
        }
    };


    private AdapterView.OnItemClickListener clickCategory = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (parent.getTag().equals("List"))
                checkCategory(position);
        }
    };

    private void checkCategory(int position) {
        TextView btnBackToCategory = (TextView) activity.findViewById(R.id.back_to_list_recipe_products);
        ListView lvProducts = (ListView) activity.findViewById(R.id.list_recipe_products);
        ArrayList<Name> foundProducts = new ArrayList<>();
        ingredientAdapter = new RecipeIngredientAdapter(context, foundProducts, ingredients);
        for (Map<String, Name> w : products) {
            Name product = w.get(product_categories.get(position));
            if (product != null) {
                product.setCheck_product(searchName(product.getName_product()));
                foundProducts.add(product);
            }
        }
        lvProducts.setAdapter(ingredientAdapter);
        btnBackToCategory.setText(product_categories.get(position));
        lvProducts.setTag("Words");
        btnBackToCategory.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    }

    private boolean searchName(String name) {
        for (Product p : ingredients)
            if (p.getProduct_name().equals(name))
                return true;
        return false;
    }

    private View.OnClickListener backToList = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ViewGroup.LayoutParams params = v.getLayoutParams();
            params.height = 0;
            v.setLayoutParams(params);
            ListView lvProducts = (ListView) activity.findViewById(R.id.list_recipe_products);
            lvProducts.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, product_categories.toArray(new String[0])));
            lvProducts.setTag("List");
        }
    };

    private void saveData(){
        if (SavedRecipe.getId().isEmpty()){
            Spinner category = (Spinner) activity.findViewById(R.id.sp_recipe_categories);
            EditText instruct = (EditText) activity.findViewById(R.id.edit_recipe_instruction);
            EditText name = (EditText) activity.findViewById(R.id.edit_recipe_name);
            EditText time = (EditText) activity.findViewById(R.id.edit_recipe_time);
            SavedRecipe.setCategory(category.getSelectedItem().toString());
            SavedRecipe.setTime(time.getText().toString());
            SavedRecipe.setInstruction(instruct.getText().toString());
            SavedRecipe.setName(name.getText().toString());
            SavedRecipe.setIngredients(ingredients);
        }
        else
            SavedRecipe.cleanAll();
    }

    private View.OnClickListener clickSearch = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(context, ListRecipesActivity.class);
            activity.startActivity(intent);
            activity.finish();
        }
    };

    private View.OnClickListener clickFridge = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            saveData();
            Intent intent = new Intent(context, FridgeActivity.class);
            activity.startActivity(intent);
            activity.finish();
        }
    };

    private View.OnClickListener clickBasket = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            saveData();
            Intent intent = new Intent(context, BasketActivity.class);
            activity.startActivity(intent);
            activity.finish();
        }
    };

}
