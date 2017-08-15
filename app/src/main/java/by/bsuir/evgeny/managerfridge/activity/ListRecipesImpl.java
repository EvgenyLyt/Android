package by.bsuir.evgeny.managerfridge.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import by.bsuir.evgeny.managerfridge.BuildConfig;
import by.bsuir.evgeny.managerfridge.Internet;
import by.bsuir.evgeny.managerfridge.MessageBox;
import by.bsuir.evgeny.managerfridge.R;
import by.bsuir.evgeny.managerfridge.adapter.SummaryRecipeAdapter;
import by.bsuir.evgeny.managerfridge.database.Database;
import by.bsuir.evgeny.managerfridge.database.Helper;
import by.bsuir.evgeny.managerfridge.entity.Product;
import by.bsuir.evgeny.managerfridge.entity.Recipe;
import by.bsuir.evgeny.managerfridge.entity.SavedRecipe;
import by.bsuir.evgeny.managerfridge.entity.SummaryRecipe;

class ListRecipesImpl {
    private Context context;
    private Activity activity;
    private ArrayList<Recipe> recipes = new ArrayList<>();
    private ArrayList<HashMap<String, SummaryRecipe>> items_recipes = new ArrayList<>();
    private ArrayList<String> recipe_categories = new ArrayList<>();
    private SummaryRecipeAdapter recipeAdapter;

    ListRecipesImpl(Context context){
        this.context=context;
        activity = (Activity) context;
    }

    void makeComponents(){
        activity.setTitle("Список рецептов");
        ListView lvRecipes = (ListView) activity.findViewById(R.id.list_recipes);
        EditText edtRecipe = (EditText) activity.findViewById(R.id.edit_name_recipe);
        TextView btnBackToCategory = (TextView) activity.findViewById(R.id.back_to_list_recipes);
        TextView btnUpdateRecipes = (TextView) activity.findViewById(R.id.update_list_recipes);
        ImageButton btnBasket = (ImageButton) activity.findViewById(R.id.btn_list_recipes_basket);
        ImageButton btnAddRecipe = (ImageButton) activity.findViewById(R.id.btn_list_recipes_add_recipe);
        ImageButton btnFridge = (ImageButton) activity.findViewById(R.id.btn_list_recipes_fridge);
        initializeDB(context);
        initializeList();
        lvRecipes.setOnItemClickListener(clickCategory);
        btnBackToCategory.setOnClickListener(backToList);
        edtRecipe.addTextChangedListener(searchRecipe);
        btnUpdateRecipes.setOnClickListener(updateBaseResipes);
        btnBasket.setOnClickListener(clickBasket);
        btnAddRecipe.setOnClickListener(clickAddRecipe);
        btnFridge.setOnClickListener(clickFridge);
    }

    private View.OnClickListener updateBaseResipes = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            connectBaseRecipes query = new connectBaseRecipes();
            query.execute();
            ListView lvRecipes = (ListView) activity.findViewById(R.id.list_recipes);
            TextView back_to_list = (TextView) activity.findViewById(R.id.back_to_list_recipes);
            ViewGroup.LayoutParams params = back_to_list.getLayoutParams();
            params.height = 0;
            back_to_list.setLayoutParams(params);
            lvRecipes.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, recipe_categories.toArray(new String[0])));
            lvRecipes.setTag("List");
        }
    };

    private boolean existRecipeName(String name){
        for (Recipe recipe: recipes)
            if (recipe.getName().equals(name))
                return true;
        return false;
    }

    private class connectBaseRecipes extends AsyncTask<Object, String, Boolean> {
        ProgressDialog waitingDialog;
        @Override
        protected void onPreExecute() {
            waitingDialog = ProgressDialog.show(context, "Подключение к базе рецептов", "Отправляем запрос на обновление...", true);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            waitingDialog.dismiss();
            if (result)
                MessageBox.Show(context,activity.getTitle().toString(),"Обновление базы рецептов завершено.");
            else
                MessageBox.Show(context,activity.getTitle().toString(),"Обновление не произошло.\nПроверьте подключение к Интернету.");
        }

        @Override
        protected Boolean doInBackground(Object... params) {
            boolean result = false;
            try {
                if (Internet.isAvailable(context)){
                    Database db = new Database(context);
                    db.openDB();
                    String query=String.format("select %s, %s, %s, %s from %s join %s on %s=%s",Helper.COLUMN_RECIPE_NAME,Helper.COLUMN_CATEGORY_NAME,Helper.COLUMN_RECIPE_TIME,
                            Helper.COLUMN_RECIPE_INSTRUCTION, Helper.TABLE_RECIPE, Helper.TABLE_CATEGORY, Helper.COLUMN_CATEGORY_ID,Helper.COLUMN_RECIPE_CATEGORY);
                    String username = BuildConfig.API_BASE_USERNAME;
                    String pass = BuildConfig.API_BASE_PASSWORD;
                    String url = "jdbc:mysql://"+BuildConfig.API_BASE_URL;
                    Class.forName("com.mysql.jdbc.Driver").newInstance();
                    Connection conn = DriverManager.getConnection(url,username,pass);
                    Statement statement  = conn.createStatement();
                    statement.executeQuery(query);
                    ResultSet resultSet = statement.getResultSet();
                    while (resultSet.next()){
                        if (!existRecipeName(resultSet.getString(Helper.COLUMN_RECIPE_NAME))){
                            ArrayList<Product> recipe_products=new ArrayList<>();
                            try {
                                String new_query = String.format("select %s, %s from %s join %s on %s=%s join %s on %s=%s where %s=%s", Helper.COLUMN_PRODUCT_NAME,
                                        Helper.COLUMN_RECIPE_PRODUCT_DESCRIPTION, Helper.TABLE_RECIPE, Helper.TABLE_RECIPE_HAS_PRODUCTS, Helper.COLUMN_PRODUCT_RECIPE,
                                        Helper.COLUMN_RECIPE_ID, Helper.TABLE_PRODUCT, Helper.COLUMN_PRODUCT_ID, Helper.COLUMN_RECIPE_PRODUCT, Helper.COLUMN_RECIPE_NAME,
                                        "\'" + resultSet.getString(Helper.COLUMN_RECIPE_NAME) + "\'");
                                Statement st = conn.createStatement();
                                st.executeQuery(new_query);
                                ResultSet rs = st.getResultSet();
                                while (rs.next())
                                    recipe_products.add(new Product(rs.getString(Helper.COLUMN_PRODUCT_NAME),rs.getString(Helper.COLUMN_RECIPE_PRODUCT_DESCRIPTION),false));
                                db.addRecipe("",resultSet.getString(Helper.COLUMN_RECIPE_NAME),recipe_categories.indexOf(resultSet.getString(Helper.COLUMN_CATEGORY_NAME))+1,
                                        resultSet.getString(Helper.COLUMN_RECIPE_TIME), resultSet.getString(Helper.COLUMN_RECIPE_INSTRUCTION),recipe_products);
                                rs.close();
                                st.close();
                            }
                            catch (Exception ex){
                                MessageBox.Show(context,ex.toString(),ex.getMessage());
                            }
                        }

                    }
                    resultSet.close();
                    statement.close();
                    conn.close();
                    recipes.clear();
                    items_recipes.clear();
                    db.fillRecipes(recipe_categories,items_recipes,recipes);
                    db.closeDB();
                    result=true;
                }
            }
            catch (Exception ex) {
                MessageBox.Show(context,ex.toString(),ex.getMessage());
            }
            return result;
        }
    }

    private View.OnClickListener backToList = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ViewGroup.LayoutParams params = v.getLayoutParams();
            params.height = 0;
            v.setLayoutParams(params);
            ListView lvRecipes = (ListView) activity.findViewById(R.id.list_recipes);
            lvRecipes.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, recipe_categories.toArray(new String[0])));
            lvRecipes.setTag("List");
        }
    };

    private TextWatcher searchRecipe = new TextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
        }
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            ListView lvListRecipes = (ListView) activity.findViewById(R.id.list_recipes);
            if (s.length() <= 2) {
                TextView back = (TextView) activity.findViewById(R.id.back_to_list_recipes);
                ViewGroup.LayoutParams params = back.getLayoutParams();
                params.height = 0;
                back.setLayoutParams(params);
                lvListRecipes.setTag("List");
                lvListRecipes.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, recipe_categories.toArray(new String[0])));
            }
            else {
                lvListRecipes.setTag("Word");
                ArrayList<SummaryRecipe> foundRecipes = new ArrayList<>();
                recipeAdapter = new SummaryRecipeAdapter(context, foundRecipes);
                for (Map<String, SummaryRecipe> w : items_recipes) {
                    int i = 0;
                    while (w.get(recipe_categories.get(i)) == null) i++;
                    SummaryRecipe recipe = w.get(recipe_categories.get(i));
                    if (recipe.getName().contains(s))
                        foundRecipes.add(recipe);
                }
                lvListRecipes.setAdapter(recipeAdapter);
            }
        }
    };


    private AdapterView.OnItemClickListener clickCategory = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (parent.getTag().equals("List"))
                checkCategory(position);
            else
            {
                Intent intent = new Intent(context, ShowRecipeActivity.class);
                intent.putExtra("Recipe_name",view.getTag().toString());
                activity.startActivity(intent);
            }
        }
    };

    private void checkCategory(int position) {
        TextView btnBackToCategory = (TextView) activity.findViewById(R.id.back_to_list_recipes);
        ListView lvRecipes = (ListView) activity.findViewById(R.id.list_recipes);
        ArrayList<SummaryRecipe> foundRecipes = new ArrayList<>();
        recipeAdapter = new SummaryRecipeAdapter(context, foundRecipes);
        for (Map<String, SummaryRecipe> w : items_recipes) {
            SummaryRecipe recipe = w.get(recipe_categories.get(position));
            if (recipe != null)
                foundRecipes.add(recipe);
        }
        lvRecipes.setAdapter(recipeAdapter);
        btnBackToCategory.setText(recipe_categories.get(position));
        lvRecipes.setTag("Words");
        btnBackToCategory.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    }

    private void initializeList(){
        ListView lvRecipes = (ListView) activity.findViewById(R.id.list_recipes);
        if (!SavedRecipe.getFound_recipes().isEmpty()){
            ArrayList<SummaryRecipe> foundRecipes = new ArrayList<>();
            recipeAdapter = new SummaryRecipeAdapter(context, foundRecipes);
            for (Map<String, SummaryRecipe> w : items_recipes) {
                for (String category: recipe_categories){
                    SummaryRecipe recipe = w.get(category);
                    if (recipe != null) {
                        if (SavedRecipe.getFound_recipes().contains(recipe.getName()))
                            foundRecipes.add(recipe);
                    }
                }
            }
            lvRecipes.setAdapter(recipeAdapter);
            lvRecipes.setTag("Words");
            SavedRecipe.setFound_recipes(new ArrayList<String>());
        }
        else
        {
            lvRecipes.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, recipe_categories.toArray(new String[0])));
            lvRecipes.setTag("List");
        }
    }

    private void initializeDB(Context context){
        Database db = new Database(context);
        File database = activity.getApplicationContext().getDatabasePath(Helper.DB_NAME);
        if (!database.exists())
             db.dbHelper.createDB(db.getHelper());
        db.openDB();
        db.fillCategories(recipe_categories);
        db.fillRecipes(recipe_categories,items_recipes,recipes);
        db.closeDB();
    }

    private View.OnClickListener clickFridge = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(context, FridgeActivity.class);
            activity.startActivity(intent);
            activity.finish();
        }
    };

    private View.OnClickListener clickAddRecipe = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(context, AddRecipeActivity.class);
            activity.startActivity(intent);
            activity.finish();
        }
    };

    private View.OnClickListener clickBasket = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(context, BasketActivity.class);
            activity.startActivity(intent);
            activity.finish();
        }
    };
}
