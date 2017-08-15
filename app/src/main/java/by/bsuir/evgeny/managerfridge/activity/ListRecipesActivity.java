package by.bsuir.evgeny.managerfridge.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import by.bsuir.evgeny.managerfridge.R;

public class ListRecipesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_recipes);
        ListRecipesImpl activity = new ListRecipesImpl(this);
        activity.makeComponents();
    }
}
