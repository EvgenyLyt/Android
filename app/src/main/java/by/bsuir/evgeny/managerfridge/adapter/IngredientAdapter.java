package by.bsuir.evgeny.managerfridge.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import by.bsuir.evgeny.managerfridge.ListViewHeight;
import by.bsuir.evgeny.managerfridge.R;
import by.bsuir.evgeny.managerfridge.entity.Product;

public class IngredientAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater lInflater;
    private ArrayList<Product> objects;
    public IngredientAdapter(Context context, ArrayList<Product> products) {
        this.context = context;
        objects = products;
        lInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public Object getItem(int position) {
        return objects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null)
            view = lInflater.inflate(R.layout.item_recipe_ingredient, parent, false);
        Product p = getProduct(position);
        ((TextView) view.findViewById(R.id.text_ingredient_name)).setText(p.getProduct_name());
        ((TextView) view.findViewById(R.id.text_ingredient_description)).setText(p.getProduct_descripton());
        ImageButton delIngr = (ImageButton) view.findViewById(R.id.btn_delete_igredient);
        delIngr.setOnClickListener(deleteIngredient);
        delIngr.setTag(position);
        return view;
    }

    Product getProduct(int position) {
        return ((Product) getItem(position));
    }

    View.OnClickListener deleteIngredient = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            objects.remove(getProduct((Integer)v.getTag()));
            IngredientAdapter new_ad = new IngredientAdapter(context, objects);
            Activity act = (Activity) context;
            ListView products = (ListView) act.findViewById(R.id.list_recipe_ingredients);
            products.setAdapter(new_ad);
            ListViewHeight.set(products);
        }
    };
}
