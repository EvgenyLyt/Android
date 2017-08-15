package by.bsuir.evgeny.managerfridge.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.ArrayList;

import by.bsuir.evgeny.managerfridge.R;
import by.bsuir.evgeny.managerfridge.entity.Product;
import by.bsuir.evgeny.managerfridge.entity.Recipe;


public class ProductRecipeAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater lInflater;
    private Recipe recipe;

    public ProductRecipeAdapter(Context context,  Recipe _recipe) {
        this.context = context;
        recipe = _recipe;
        lInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return recipe.getIngredients().size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return recipe.getIngredients().get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = lInflater.inflate(R.layout.item_ingredient, parent, false);
        }
        Product p = getProduct(position);
        TextView name_ing = (TextView) view.findViewById(R.id.text_name_ingredient);
        TextView desc_ing = (TextView) view.findViewById(R.id.text_description_ingredient);
        CheckBox box_ing = (CheckBox) view.findViewById(R.id.check_ingredient);
        name_ing.setText(p.getProduct_name());
        desc_ing.setText(p.getProduct_descripton());
        if (p.isCheck_product())
            name_ing.setTextColor(ContextCompat.getColor(context, R.color.colorDarkGreen));
        else
            name_ing.setTextColor(ContextCompat.getColor(context, R.color.colorBlack));
        box_ing.setTag(position);
        box_ing.setOnCheckedChangeListener(Check);
        box_ing.setChecked(!p.isCheck_product());
        return view;
    }
    private Product getProduct(int position) {
        return ((Product) getItem(position));
    }

    private CompoundButton.OnCheckedChangeListener Check = new CompoundButton.OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            recipe.getIngredients().get((Integer) buttonView.getTag()).setCheck_product(!isChecked);
        }
    };
}
