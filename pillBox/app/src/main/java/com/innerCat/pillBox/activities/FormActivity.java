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

import com.google.android.material.appbar.AppBarLayout;
import com.innerCat.pillBox.R;
import com.innerCat.pillBox.databinding.FormActivityBinding;
import com.innerCat.pillBox.factories.ColorFactory;
import com.innerCat.pillBox.factories.TextWatcherFactory;
import com.innerCat.pillBox.objects.ColorItem;
import com.innerCat.pillBox.objects.Item;
import com.innerCat.pillBox.recyclerViews.ColorAdapter;

import java.time.LocalDate;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class FormActivity extends AppCompatActivity {

    private FormActivityBinding g;

    private Item itemToEdit = null;

    int requestCode;
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
                g.toolbarLayout,
                g.okButton));

        //Add offset listener for when the view is collapsing or expanded
        AppBarLayout appBarLayout = findViewById(R.id.app_bar);
        Context context = this;
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged( AppBarLayout appBarLayout, int verticalOffset ) {

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
            itemToEdit = (Item) intent.getSerializableExtra("item");
            g.editName.setText(itemToEdit.getName());
            g.editStock.setText(String.valueOf(itemToEdit.getRawStock()));
            g.widgetSwitch.setChecked(itemToEdit.getShowInWidget());
            if (itemToEdit.getAutoDecStartDate() != null) {
                g.autoDecLinearLayout.setVisibility(VISIBLE);
                g.autoDecSwitch.setChecked(true);
                g.autoDecNDaysPicker.setValue(itemToEdit.getAutoDecNDays());
                g.autoDecPerDayPicker.setValue(itemToEdit.getAutoDecPerDay());
            }
            //gets the color from intent for adding, or from item intent for editing
            selectedColor = itemToEdit.getColor();

            //because EDIT
            g.deleteButton.setVisibility(VISIBLE);
        } else {
            g.deleteButton.setVisibility(GONE);
            g.editName.requestFocus();
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
        g.autoDecSwitch.setOnCheckedChangeListener(( buttonView, isChecked ) -> {
            int state = isChecked ? VISIBLE : GONE;
            g.autoDecLinearLayout.setVisibility(state);
        });

        adapter = new ColorAdapter();
        g.rvColors.setAdapter(adapter);
        g.rvColors.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
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
        Intent returnIntent = new Intent();
        String name = ((EditText) findViewById(R.id.editName)).getText().toString();
        String stockString = ((EditText) findViewById(R.id.editStock)).getText().toString();
        boolean showInWidget = g.widgetSwitch.isChecked();
        boolean autodec = g.autoDecSwitch.isChecked();
        int stock = 0;
        if (stockString.isEmpty() == false) {
            try {
                stock = Integer.parseInt(stockString);
            } catch (NumberFormatException ignored) {
                //leave the stock
            }
        }
        if (requestCode == MainActivity.EDIT_ITEM_REQUEST) {
            itemToEdit.setName(name);
            itemToEdit.setRawStock(stock);
            itemToEdit.setShowInWidget(showInWidget);
            itemToEdit.setAutoDecStartDate(autodec ? LocalDate.now() : null);
            if (autodec) {
                itemToEdit.setAutoDecNDays(g.autoDecNDaysPicker.getValue());
                itemToEdit.setAutoDecPerDay(g.autoDecPerDayPicker.getValue());
            }

            returnIntent.putExtra("item", itemToEdit);
        } else {
            Item item;
            if (autodec) {
                item = new Item(name,
                        stock,
                        selectedColor,
                        showInWidget,
                        LocalDate.now(),
                        g.autoDecNDaysPicker.getValue(),
                        g.autoDecPerDayPicker.getValue());
            } else {
                item = new Item(name, stock, selectedColor, showInWidget);
            }
            returnIntent.putExtra("item", item);
        }
        returnIntent.putExtra("position", getIntent().getIntExtra("position", -1));
        setResult(RESULT_OK, returnIntent);
        System.out.println("FINISHED");
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