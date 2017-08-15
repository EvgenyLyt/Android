package by.bsuir.evgeny.managerfridge.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import by.bsuir.evgeny.managerfridge.R;

public class AddRecipeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);
        AddRecipeImpl activity = new AddRecipeImpl(this);
        activity.makeComponents();
    }
}
