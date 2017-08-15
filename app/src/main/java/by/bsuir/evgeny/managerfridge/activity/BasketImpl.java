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
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import by.bsuir.evgeny.managerfridge.MessageBox;
import by.bsuir.evgeny.managerfridge.R;
import by.bsuir.evgeny.managerfridge.adapter.ItemAdapter;
import by.bsuir.evgeny.managerfridge.adapter.NameAdapter;
import by.bsuir.evgeny.managerfridge.database.Database;
import by.bsuir.evgeny.managerfridge.database.Helper;
import by.bsuir.evgeny.managerfridge.entity.Name;
import by.bsuir.evgeny.managerfridge.entity.Product;

class BasketImpl {
    private ArrayList<Product> check_products = new ArrayList<>();
    private ArrayList<String> product_categories = new ArrayList<>();
    private ArrayList<HashMap<String, Name>> products = new ArrayList<>();
    private ItemAdapter itemAdapter;
    private NameAdapter nameAdapter;
    private Context context;
    private Activity activity;

    BasketImpl(Context context){
        this.context = context;
        this.activity = (Activity) context;
    }

    void makeComponents() {
        activity.setTitle("Корзина");
        TextView btnDelete = (TextView) activity.findViewById(R.id.delete_list);
        ListView lvProducts = (ListView) activity.findViewById(R.id.list_basket_products);
        EditText edtProduct = (EditText) activity.findViewById(R.id.edit_name_basket_product);
        TextView btnBackToCategory = (TextView) activity.findViewById(R.id.back_to_list_basket_products);
        ListView lvBasketProducts = (ListView) activity.findViewById(R.id.basket_products);
        TextView btnAddToFridge = (TextView) activity.findViewById(R.id.add_products_to_fridge);
        ImageButton btnSearch = (ImageButton) activity.findViewById(R.id.btn_basket_list_recipes);
        ImageButton btnAddRecipe = (ImageButton) activity.findViewById(R.id.btn_basket_add_recipe);
        ImageButton btnFridge = (ImageButton) activity.findViewById(R.id.btn_basket_fridge);
        initializeDB(context);
        lvProducts.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, product_categories.toArray(new String[0])));
        lvProducts.setTag("List");
        itemAdapter = new ItemAdapter(context, check_products);
        lvBasketProducts.setAdapter(itemAdapter);
        lvBasketProducts.setOnItemClickListener(clickProduct);
        lvProducts.setOnItemClickListener(clickCategory);
        btnBackToCategory.setOnClickListener(backToList);
        btnDelete.setOnClickListener(deleteProduct);
        btnAddToFridge.setOnClickListener(addToFridge);
        edtProduct.addTextChangedListener(searchProduct);
        btnSearch.setOnClickListener(clickSearch);
        btnAddRecipe.setOnClickListener(clickAddRecipe);
        btnFridge.setOnClickListener(clickFridge);
    }

    private void initializeDB(Context context){
        try {
            Database db = new Database(context);
            File database = activity.getApplicationContext().getDatabasePath(Helper.DB_NAME);
            if (!database.exists())
                db.dbHelper.createDB(db.getHelper());
            db.openDB();
            db.fillItems(product_categories,products,check_products,Helper.COLUMN_PRODUCT_BOX_DESCRIPTION,Helper.COLUMN_PRODUCT_BOX);
            db.closeDB();
        }
        catch (Exception ex){
            MessageBox.Show(context,ex.toString(),ex.getMessage());
        }
    }

    private AdapterView.OnItemClickListener clickProduct = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
            LayoutInflater li = LayoutInflater.from(context);
            View promptsView = li.inflate(R.layout.dialog, null);
            AlertDialog.Builder mDialogBuilder = new AlertDialog.Builder(context);
            mDialogBuilder.setView(promptsView);
            final EditText tv_description = (EditText) promptsView.findViewById(R.id.dialog_text_product_description);
            final TextView tv_name = (TextView) promptsView.findViewById(R.id.dialog_text_product_name);
            tv_description.setText(check_products.get(position).getProduct_descripton());
            tv_name.setText(check_products.get(position).getProduct_name());
            mDialogBuilder.setCancelable(false).setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int id) {
                            check_products.get(position).setProduct_descripton(tv_description.getText().toString());
                            Database db = new Database(context);
                            db.openDB();
                            db.addProduct(check_products.get(position).getProduct_name(),check_products.get(position).getProduct_descripton(),Helper.COLUMN_PRODUCT_BOX_DESCRIPTION,
                                    Helper.COLUMN_PRODUCT_BOX,Helper.COLUMN_PRODUCT_BOX_FAVORITE);
                            db.closeDB();
                            itemAdapter.notifyDataSetChanged();
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

    private View.OnClickListener addToFridge = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Database db = new Database(context);
            db.openDB();
            for (Product product: check_products)
                db.addToFridge(product.getProduct_name(),product.getProduct_descripton());
            db.closeDB();
            check_products.clear();
            itemAdapter.notifyDataSetChanged();
            MessageBox.Show(context,activity.getTitle().toString(),"Продукты успешно добавлены в холодильник.");
        }
    };

    private TextWatcher searchProduct = new TextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
        }
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            ListView lvProducts = (ListView) activity.findViewById(R.id.list_basket_products);
            if (s.length() <= 2) {
                TextView back = (TextView) activity.findViewById(R.id.back_to_list_basket_products);
                ViewGroup.LayoutParams params = back.getLayoutParams();
                params.height = 0;
                back.setLayoutParams(params);
                lvProducts.setTag("List");
                lvProducts.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, product_categories.toArray(new String[0])));
            } else {
                ArrayList<Name> foundProducts = new ArrayList<>();
                nameAdapter = new NameAdapter(context, foundProducts, check_products, itemAdapter);
                for (Map<String, Name> w : products) {
                    int i = 0;
                    while (w.get(product_categories.get(i)) == null) i++;
                    Name product = w.get(product_categories.get(i));
                    if (product.getName_product().contains(s)) {
                        product.setCheck_product(searchName(product.getName_product()));
                        foundProducts.add(product);
                    }
                }
                lvProducts.setAdapter(nameAdapter);
            }
        }
    };

    private View.OnClickListener deleteProduct = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ArrayList<Product> box = itemAdapter.getBox();
            for (Product p : box) {
                check_products.remove(p);
                Database db = new Database(context);
                db.openDB();
                db.deleteProduct(p.getProduct_name(), p.getProduct_descripton(),Helper.COLUMN_PRODUCT_BOX_DESCRIPTION,Helper.COLUMN_PRODUCT_BOX,
                        Helper.COLUMN_PRODUCT_BOX_FAVORITE);
                db.closeDB();
            }
            itemAdapter.notifyDataSetChanged();
            ViewGroup.LayoutParams params = v.getLayoutParams();
            params.height = 0;
            v.setLayoutParams(params);
        }
    };

    private View.OnClickListener backToList = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ViewGroup.LayoutParams params = v.getLayoutParams();
            params.height = 0;
            v.setLayoutParams(params);
            ListView lvProducts = (ListView) activity.findViewById(R.id.list_basket_products);
            lvProducts.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, product_categories.toArray(new String[0])));
            lvProducts.setTag("List");
        }
    };

    private AdapterView.OnItemClickListener clickCategory = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (parent.getTag().equals("List"))
                checkCategory(position);
        }
    };


    private boolean searchName(String name) {
        for (Product p : check_products)
            if (p.getProduct_name().equals(name))
                return true;
        return false;

    }

    private void checkCategory(int position) {
        TextView btnBackToCategory = (TextView) activity.findViewById(R.id.back_to_list_basket_products);
        ListView lvProducts = (ListView) activity.findViewById(R.id.list_basket_products);
        ArrayList<Name> foundProducts = new ArrayList<>();
        nameAdapter = new NameAdapter(context, foundProducts, check_products, itemAdapter);
        for (Map<String, Name> w : products) {
            Name product = w.get(product_categories.get(position));
            if (product != null) {
                product.setCheck_product(searchName(product.getName_product()));
                foundProducts.add(product);
            }
        }
        lvProducts.setAdapter(nameAdapter);
        btnBackToCategory.setText(product_categories.get(position));
        lvProducts.setTag("Words");
        btnBackToCategory.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
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
}
