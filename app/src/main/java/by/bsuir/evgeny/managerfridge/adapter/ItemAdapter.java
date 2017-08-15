package by.bsuir.evgeny.managerfridge.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import by.bsuir.evgeny.managerfridge.R;
import by.bsuir.evgeny.managerfridge.database.Database;
import by.bsuir.evgeny.managerfridge.entity.Product;

public class ItemAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater lInflater;
    private ArrayList<Product> objects;

    public ItemAdapter(Context _context, ArrayList<Product> products) {
        context = _context;
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
            view = lInflater.inflate(R.layout.item_product, parent, false);
        Product p = getProduct(position);
        ((TextView) view.findViewById(R.id.text_product_name)).setText(p.getProduct_name());
        ((TextView) view.findViewById(R.id.text_product_description)).setText(p.getProduct_descripton());
        ImageButton btnFavs = (ImageButton) view.findViewById(R.id.btn_product_favs);
        btnFavs.setTag(position);
        initFavs(btnFavs);
        btnFavs.setOnClickListener(clickFavs);
        CheckBox cbBuy = (CheckBox) view.findViewById(R.id.btn_check_product);
        cbBuy.setOnCheckedChangeListener(check_products);
        cbBuy.setTag(position);
        cbBuy.setChecked(p.isCheck_product());
        return view;
    }

    private View.OnClickListener clickFavs = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ImageButton button = (ImageButton) v;
            Product product = getProduct((Integer) v.getTag());
            Activity current_activity=(Activity)context;
            boolean fridge = current_activity.getTitle().toString().equals("Холодильник");
            Database db = new Database(context);
            db.openDB();
            if (db.findFavs(fridge,product.getProduct_name())==1){
                db.updateFavs(fridge,product.getProduct_name(),0);
                button.setImageResource(R.mipmap.btn_star_big_off);
            }
            else {
                db.updateFavs(fridge,product.getProduct_name(),1);
                button.setImageResource(R.mipmap.btn_star_big_on);
            }
        }
    };

    private void initFavs(ImageButton button){
        Product product = getProduct((Integer) button.getTag());
        Activity current_activity=(Activity)context;
        Database db = new Database(context);
        db.openDB();
        int in=(current_activity.getTitle().toString().equals("Холодильник"))?db.findFavs(true,product.getProduct_name()):db.findFavs(false,product.getProduct_name());
        if (in==1)
            button.setImageResource(R.mipmap.btn_star_big_on);
        else
            button.setImageResource(R.mipmap.btn_star_big_off);
        db.closeDB();
    }

    private Product getProduct(int position) {
        return ((Product) getItem(position));
    }

    public ArrayList<Product> getBox() {
        ArrayList<Product> box = new ArrayList<Product>();
        for (Product p : objects)
            if (p.isCheck_product())
                box.add(p);
        return box;
    }

    private CompoundButton.OnCheckedChangeListener check_products = new CompoundButton.OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            getProduct((Integer) buttonView.getTag()).setCheck_product(isChecked);
            Activity current_activity=(Activity)context;
            TextView tv_delete_list = (TextView) current_activity.findViewById(R.id.delete_list);
            ViewGroup.LayoutParams params = tv_delete_list.getLayoutParams();
            ArrayList<Product> box = getBox();
            params.height=0;
            if (box.size()>0)
                params.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            tv_delete_list.setLayoutParams(params);
        }
    };
}
