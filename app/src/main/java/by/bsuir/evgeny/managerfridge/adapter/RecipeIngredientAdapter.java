package by.bsuir.evgeny.managerfridge.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import by.bsuir.evgeny.managerfridge.ListViewHeight;
import by.bsuir.evgeny.managerfridge.R;
import by.bsuir.evgeny.managerfridge.entity.Name;
import by.bsuir.evgeny.managerfridge.entity.Product;

public class RecipeIngredientAdapter extends BaseAdapter{
    private Context context;
    private LayoutInflater lInflater;
    private ArrayList<Name> objects;
    private ArrayList<Product> products;
    public RecipeIngredientAdapter(Context context, ArrayList<Name> _obj, ArrayList<Product> _prod) {
        this.context = context;
        objects = _obj;
        products = _prod;
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
            view = lInflater.inflate(R.layout.name_product, parent, false);
        Name product_name = getName(position);
        TextView tv_name_product = (TextView) view.findViewById(R.id.text_name_product);
        tv_name_product.setText(product_name.getName_product());
        tv_name_product.setTag(position);
        CheckBox cb_check_name = (CheckBox) view.findViewById(R.id.btn_check_name);
        cb_check_name.setTag(position);
        cb_check_name.setOnCheckedChangeListener(check_product);
        cb_check_name.setChecked(product_name.isCheck_product());
        return view;
    }
    private Name getName(int position) {
        return ((Name) getItem(position));
    }

    private CompoundButton.OnCheckedChangeListener check_product = new CompoundButton.OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            Name product = getName((Integer) buttonView.getTag());
            product.setCheck_product(isChecked);
            if (isChecked && notInBox(product.getName_product()))
                products.add(new Product(product.getName_product(),"",false));
            else
                if (!isChecked &&!notInBox(product.getName_product()))
                 products.remove(findIndex(product.getName_product()));
            IngredientAdapter adapter = new IngredientAdapter(context,products);
            Activity act = (Activity) context;
            ListView ingredients = (ListView) act.findViewById(R.id.list_recipe_ingredients);
            ingredients.setAdapter(adapter);
            ListViewHeight.set(ingredients);
        }
    };

    private boolean notInBox(String name){
        for (Product p: products)
            if (p.getProduct_name().equals(name))
                return false;
        return true;
    }

    private int findIndex(String name){
        boolean result = false;
        int index=0;
        while (!result){
            if (products.get(index).getProduct_name().equals(name))
                result=true;
            else
                index++;
        }
        return index;
    }
}
