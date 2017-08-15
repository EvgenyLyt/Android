package by.bsuir.evgeny.managerfridge.database;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import by.bsuir.evgeny.managerfridge.MessageBox;

public class Helper extends SQLiteOpenHelper {
    private Context context;
    static String DB_PATH = "";
    public static final String DB_NAME = "fridge.db";
    private static final int SCHEMA = 1;
    public static final String TABLE_PRODUCT = "product";
    public static final String TABLE_CATEGORY = "category";
    public static final String COLUMN_CATEGORY_NAME = "category_name";
    public static final String TABLE_TYPE_PRODUCT = "type_product";
    public static final String COLUMN_TYPE_PRODUCT_NAME = "type_product_name";
    public static final String COLUMN_PRODUCT_FRIDGE = "product_fridge";
    public static final String COLUMN_PRODUCT_NAME = "product_name";
    public static final String COLUMN_PRODUCT_TYPE = "product_type";
    public static final String COLUMN_PRODUCT_DESCRIPTION = "product_description";
    public static final String TABLE_RECIPE = "recipe";
    public static final String TABLE_RECIPE_HAS_PRODUCTS = "recipe_has_products";
    public static final String COLUMN_RECIPE_PRODUCT_DESCRIPTION = "recipe_product_description";
    public static final String COLUMN_CATEGORY_ID = "category_id";
    public static final String COLUMN_RECIPE_NAME = "recipe_name";
    public static final String COLUMN_RECIPE_TIME = "recipe_time";
    public static final String COLUMN_RECIPE_INSTRUCTION = "recipe_instruction";
    public static final String COLUMN_RECIPE_CATEGORY = "recipe_category";
    public static final String COLUMN_RECIPE_ID = "recipe_id";
    public static final String COLUMN_PRODUCT_RECIPE = "product_recipe";
    public static final String COLUMN_RECIPE_PRODUCT = "recipe_product";
    public static final String COLUMN_PRODUCT_ID = "product_id";
    public static final String COLUMN_PRODUCT_BOX = "product_box";
    public static final String COLUMN_PRODUCT_BOX_DESCRIPTION = "product_box_description";
    public static final String COLUMN_PRODUCT_FAVORITE = "product_favorite";
    public static final String COLUMN_PRODUCT_BOX_FAVORITE = "product_box_favorite";

    public Helper(Context context) {
        super(context, DB_NAME, null, SCHEMA);
        this.context=context;
        DB_PATH = context.getDatabasePath(DB_NAME).getPath();
    }

    public void createDB(Helper dbHelper){
        dbHelper.getReadableDatabase();
        InputStream myInput = null;
        OutputStream myOutput = null;
        try {
            myInput = context.getAssets().open(Helper.DB_NAME);
            String outFileName = Helper.DB_PATH;
            myOutput = new FileOutputStream(outFileName);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = myInput.read(buffer)) > 0)
                myOutput.write(buffer, 0, length);
            myOutput.flush();
            myOutput.close();
            myInput.close();
        }
        catch(IOException ex){
            MessageBox.Show(context,"Helper",ex.toString()+"\n"+ex.getMessage());
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public SQLiteDatabase open() throws SQLException {
        return SQLiteDatabase.openDatabase(DB_PATH, null, SQLiteDatabase.OPEN_READWRITE);
    }
}