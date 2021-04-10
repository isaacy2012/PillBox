package com.innerCat.pillBox.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.innerCat.pillBox.R;
import com.innerCat.pillBox.databinding.FormActivityBinding;
import com.innerCat.pillBox.factories.ColorFactory;
import com.innerCat.pillBox.factories.TextWatcherFactory;
import com.innerCat.pillBox.objects.ColorItem;
import com.innerCat.pillBox.objects.Item;
import com.innerCat.pillBox.recyclerViews.ColorAdapter;
import com.innerCat.pillBox.room.Converters;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class FormActivity extends AppCompatActivity {

    private FormActivityBinding g;

    int requestCode;
    CollapsingToolbarLayout collapsingToolbarLayout;
    RecyclerView rvColors;
    ColorAdapter adapter;
    int selectedColor = ColorItem.NO_COLOR;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        g = FormActivityBinding.inflate(getLayoutInflater());
        View view = g.getRoot();
        setContentView(view);

        setSupportActionBar(g.toolbar);


        //Adding textChangedListener for g.editName

        g.editName.addTextChangedListener(TextWatcherFactory.getTitleTextAndImageButton(
                g.editName,
                collapsingToolbarLayout,
                g.okButton));

        //Add offset listener for when the view is collapsing or expanded
        AppBarLayout appBarLayout = findViewById(R.id.app_bar);
        Context context = this;
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {

                //if (Math.abs(verticalOffset)-appBarLayout.getTotalScrollRange() == 0) {
                if (Math.abs(verticalOffset) > 0) { //appBarLayout.getTotalScrollRange()*0.3) {
                    //  Collapsing
                    g.editName.setFocusableInTouchMode(false);
                    g.editName.setFocusable(false);
                    g.editName.setTextColor(ContextCompat.getColor(context, R.color.transparent));
                    g.editName.setHint("");
                    //g.editName.setVisibility(GONE);
                    g.editStock.requestFocus();
                } else {
                    //Expanded
                    //g.editName.setVisibility(VISIBLE);
                    //get the default color
                    int color = ColorFactory.getDefaultTextColor(context);
                    g.editName.setTextColor(color);
                    g.editName.setHint("Title");

                    g.editName.setFocusableInTouchMode(true);
                    g.editName.setFocusable(true);
                }
            }
        });

        Intent intent = getIntent();
        requestCode = intent.getIntExtra("requestCode", -1);
        if (requestCode == MainActivity.EDIT_ITEM_REQUEST) {
            String name = intent.getStringExtra("name");
            int stock = intent.getIntExtra("stock", 0);
            selectedColor = intent.getIntExtra("color", ColorItem.NO_COLOR);
            boolean showInWidget = intent.getBooleanExtra("showInWidget", false);
            g.editName.setText(name);
            g.editStock.setText(String.valueOf(stock));
            g.widgetSwitch.setChecked(showInWidget);
            g.deleteButton.setVisibility(VISIBLE);
        } else {
            g.deleteButton.setVisibility(GONE);
            g.editName.requestFocus();
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }

        adapter = new ColorAdapter();
        rvColors.setAdapter(adapter);
        rvColors.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        adapter.setSelectedColor(selectedColor);
    }

    public void deleteButton( View view ) {
        Intent intent = new Intent();
        setResult(MainActivity.RESULT_DELETE, intent);
        finish();
    }

    /**
     * Ok button.
     *
     * @param view the view
     */
    public void okButton( View view ) {
        Intent intent = new Intent();
        String name = ((EditText)findViewById(R.id.editName)).getText().toString();
        int stock = 0;
        String stockString = ((EditText) findViewById(R.id.editStock)).getText().toString();
        if (stockString.isEmpty() == false) {
            try {
                stock = Integer.parseInt(stockString);
            } catch (NumberFormatException ignored) {
            }
        }
        boolean showInWidget = g.widgetSwitch.isChecked();

        Item item = new Item(name, stock, selectedColor, showInWidget);
        //get the pos from the original incoming intent
        int pos = getIntent().getIntExtra("position", -1);

        intent.putExtras(Converters.getEditBundleFromItemAndPosition(item, pos));
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * Cancel.
     */
    private void cancel() {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    /**
     * Cancel button.
     *
     * @param view the view
     */
    public void cancelButton( View view ) {
        cancel();
    }

    /**
     * When the hardware/software back button is pressed
     */
    @Override
    public void onBackPressed() {
        cancel();
    }

    /**
     * Sets chosen color.
     *
     * @param color the color
     */
    public void setSelectedColor( Integer color ) {
        this.selectedColor = color;
    }
}