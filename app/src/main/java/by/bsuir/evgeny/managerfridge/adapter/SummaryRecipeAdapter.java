package by.bsuir.evgeny.managerfridge.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import by.bsuir.evgeny.managerfridge.R;
import by.bsuir.evgeny.managerfridge.entity.SummaryRecipe;

public class SummaryRecipeAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater lInflater;
    private ArrayList<SummaryRecipe> objects;

    public SummaryRecipeAdapter(Context context, ArrayList<SummaryRecipe> _obj) {
        this.context = context;
        objects = _obj;
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
            view = lInflater.inflate(R.layout.item_recipe, parent, false);
        SummaryRecipe recipe = getRecipe(position);
        fillTextView(view,R.id.text_name_recipe,recipe.getName(),position);
        fillTextView(view,R.id.text_ingredients_recipe,recipe.getIngredients(),position);
        fillTextView(view,R.id.text_count_ingredients_recipe,recipe.getCount(),position);
        fillTextView(view,R.id.text_time_recipe,recipe.getTime(),position);
        view.setTag(recipe.getName());
        return view;
    }

    private void fillTextView(View view, int id, String text, int position){
        TextView textView = (TextView) view.findViewById(id);
        textView.setText(text);
        textView.setTag(position);
    }

    private SummaryRecipe getRecipe(int position) {
        return ((SummaryRecipe) getItem(position));
    }
}
