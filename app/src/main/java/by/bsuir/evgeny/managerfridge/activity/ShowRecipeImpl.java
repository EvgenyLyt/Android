package by.bsuir.evgeny.managerfridge.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;

import by.bsuir.evgeny.managerfridge.BuildConfig;
import by.bsuir.evgeny.managerfridge.Internet;
import by.bsuir.evgeny.managerfridge.ListViewHeight;
import by.bsuir.evgeny.managerfridge.MessageBox;
import by.bsuir.evgeny.managerfridge.R;
import by.bsuir.evgeny.managerfridge.adapter.ProductRecipeAdapter;

import by.bsuir.evgeny.managerfridge.database.Database;
import by.bsuir.evgeny.managerfridge.database.Helper;
import by.bsuir.evgeny.managerfridge.entity.Product;
import by.bsuir.evgeny.managerfridge.entity.Recipe;
import by.bsuir.evgeny.managerfridge.entity.SavedRecipe;
import by.bsuir.evgeny.managerfridge.mail.GMailSender;

class ShowRecipeImpl {
    private Activity activity;
    private Context context;
    private Recipe recipe = new Recipe();

    ShowRecipeImpl(Context context){
        this.context=context;
        activity=(Activity)context;
    }

    void makeComponents(){
        activity.setTitle("Рецепт");
        initializeRecipe();
        TextView recipe_name = (TextView) activity.findViewById(R.id.text_recipe_name);
        TextView recipe_category = (TextView) activity.findViewById(R.id.text_recipe_category);
        TextView recipe_time = (TextView) activity.findViewById(R.id.text_recipe_time);
        TextView recipe_instruction = (TextView) activity.findViewById(R.id.text_recipe_instruction);
        TextView add_to_basket = (TextView) activity.findViewById(R.id.btn_add_to_basket);
        TextView edit_recipe = (TextView)activity.findViewById(R.id.btn_edit_recipe);
        TextView delete_recipe = (TextView)activity.findViewById(R.id.btn_delete_recipe);
        TextView back_to_recipes = (TextView) activity.findViewById(R.id.btn_back_to_recipes);
        ListView recipe_ingredients = (ListView) activity.findViewById(R.id.text_recipe_ingredients);
        TextView send_recipe = (TextView) activity.findViewById(R.id.btn_send_recipe);
        recipe_ingredients.setAdapter(new ProductRecipeAdapter(context,recipe));
        ListViewHeight.set(recipe_ingredients);
        recipe_name.setText(recipe.getName());
        recipe_category.setText(recipe.getCategory());
        recipe_time.setText(recipe_time.getText()+" "+recipe.getTime());
        recipe_instruction.setText(recipe.getInstruction());
        back_to_recipes.setOnClickListener(back_to_list);
        add_to_basket.setOnClickListener(add_basket);
        edit_recipe.setOnClickListener(recipe_edit);
        delete_recipe.setOnClickListener(recipe_delete);
        send_recipe.setOnClickListener(recipe_send);
    }


    private void initializeRecipe(){
        Intent intent = activity.getIntent();
        String recipe_name = intent.getStringExtra("Recipe_name");
        Database db = new Database(context);
        db.openDB();
        db.searchRecipe(recipe_name,recipe);
        db.closeDB();
    }

    private View.OnClickListener recipe_delete = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Database db = new Database(context);
            db.openDB();
            db.deleteRecipe(recipe.getId());
            db.closeDB();
            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setTitle(activity.getTitle().toString());
            dialog.setMessage("Рецепт успешно удален из базы.");
            dialog.setCancelable(false).setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int id) {
                            Intent intent = new Intent(context,ListRecipesActivity.class);
                            activity.startActivity(intent);
                            activity.finish();
                        }
                    });
            AlertDialog alertDialog = dialog.create();
            alertDialog.show();
        }
    };

    private View.OnClickListener recipe_edit = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            SavedRecipe.setId(recipe.getId());
            SavedRecipe.setName(recipe.getName());
            SavedRecipe.setCategory(recipe.getCategory());
            SavedRecipe.setTime(recipe.getTime());
            SavedRecipe.setInstruction(recipe.getInstruction());
            SavedRecipe.setIngredients(recipe.getIngredients());
            Intent intent = new Intent(context, AddRecipeActivity.class);
            activity.startActivity(intent);
            activity.finish();
        }
    };

    private View.OnClickListener add_basket = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Database db = new Database(context);
            db.openDB();
            for (Product p: recipe.getIngredients()){
                if (!p.isCheck_product())
                    db.addToBasket(p.getProduct_name(),p.getProduct_descripton());
            }
            db.closeDB();
            MessageBox.Show(context,activity.getTitle().toString(),"Продукты успешно добавлены в корзину.");
        }
    };

    private View.OnClickListener recipe_send = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            LayoutInflater li = LayoutInflater.from(context);
            View promptsView = li.inflate(R.layout.dialog, null);
            AlertDialog.Builder mDialogBuilder = new AlertDialog.Builder(context);
            mDialogBuilder.setView(promptsView);
            final EditText tv_description = (EditText) promptsView.findViewById(R.id.dialog_text_product_description);
            final TextView tv_name = (TextView) promptsView.findViewById(R.id.dialog_text_product_name);
            tv_name.setText("Введите email:");
            mDialogBuilder.setCancelable(false).setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int id) {
                            String email = tv_description.getText().toString();
                            SendRecipe sendRecipe = new SendRecipe(email);
                            sendRecipe.execute();
                        }
                    }).setNegativeButton("Отмена",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alertDialog = mDialogBuilder.create();
            alertDialog.show();
        }
    };



    private View.OnClickListener back_to_list = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            activity.onBackPressed();
            activity.finish();
        }
    };

    private class SendRecipe extends AsyncTask<Object, String, Boolean> {
        ProgressDialog WaitingDialog;
        String email="";
        SendRecipe(String email){
            super();
            this.email=email;
        }
        @Override
        protected void onPreExecute() {
            WaitingDialog = ProgressDialog.show(context, "Отправка рецепта", "Отправляем письмо...", true);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            WaitingDialog.dismiss();
            if (result)
                MessageBox.Show(context,activity.getTitle().toString(),"Отправка завершена");
            else
                MessageBox.Show(context,activity.getTitle().toString(),"Отправка не произошла.\nПроверьте правильность email'a или подключение к Интернету.");
        }

        @Override
        protected Boolean doInBackground(Object... params) {
            boolean result = false;
            try {
                if (Internet.isAvailable(context)){
                    GMailSender sender = new GMailSender(context, BuildConfig.API_MAIL_URL, BuildConfig.API_MAIL_PASSWORD);
                    result=sender.sendMail("Рецепт", getContentRecipe(), BuildConfig.API_MAIL_URL, email);
                }
            }
            catch (Exception ex) {
                MessageBox.Show(context,ex.toString(),ex.getMessage());
            }
            return result;
        }
    }

    private String getContentRecipe(){
        String text="";
        text+= recipe.getName()+"\n";
        text+="Категория: "+recipe.getCategory()+"\n";
        text+="Время приготовления (в минутах): "+recipe.getTime()+"\n";
        text+="Ингредиенты:\n";
        for (Product p: recipe.getIngredients())
            text+=p.getProduct_name()+ " - " +p.getProduct_descripton()+"\n";
        text+="Инструкция по приготовлению:\n"+recipe.getInstruction()+"\n";
        text+="\nManager Fridge желает вам кулинарных успехов и всего наилучшего!";
        return text;
    }
}
