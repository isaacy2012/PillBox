package com.innerCat.pillBox.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.NumberPicker;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.innerCat.pillBox.R;
import com.innerCat.pillBox.databinding.FormActivityBinding;
import com.innerCat.pillBox.factories.ColorFactory;
import com.innerCat.pillBox.factories.TextWatcherFactory;
import com.innerCat.pillBox.objects.Item;
import com.innerCat.pillBox.recyclerViews.ColorAdapter;

import java.time.LocalDate;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.innerCat.pillBox.util.IfNotNull.ifNotNull;

public class FormActivity extends AppCompatActivity {

    private FormActivityBinding g;

    private Item itemToEdit = null;

    int requestCode;
    ColorAdapter colorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        g = FormActivityBinding.inflate(getLayoutInflater());
        View view = g.getRoot();
        setContentView(view);
        setupUI(view);

        setSupportActionBar(g.toolbar);
        ifNotNull(getSupportActionBar(), x -> x.setHomeAsUpIndicator(R.drawable.ic_baseline_close_24));
        ifNotNull(getSupportActionBar(), x -> x.setDisplayHomeAsUpEnabled(true));


        colorAdapter = new ColorAdapter();
        g.rvColors.setAdapter(colorAdapter);
        g.rvColors.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));


        //Add offset listener for when the view is collapsing or expanded
        AppBarLayout appBarLayout = findViewById(R.id.app_bar);
        Context context = this;
        appBarLayout.addOnOffsetChangedListener((appBarLayout1, verticalOffset) -> {

            //if (Math.abs(verticalOffset)-appBarLayout.getTotalScrollRange() == 0) {
            if (Math.abs(verticalOffset) > 0) { //appBarLayout.getTotalScrollRange()*0.3) {
                //  Collapsing
                g.editName.setFocusableInTouchMode(false);
                g.editName.setFocusable(false);
                g.editName.setTextColor(ContextCompat.getColor(context, R.color.transparent));
                g.editName.setHint("");
//                g.editStock.requestFocus();
            } else {
                //Expanded
                //get the default color
                int color = ColorFactory.getDefaultTextColor(context);
                g.editName.setTextColor(color);
                g.editName.setHint(getString(R.string.title_string));
                g.editName.setFocusableInTouchMode(true);
                g.editName.setFocusable(true);
            }
        });

        Intent intent = getIntent();
        requestCode = intent.getIntExtra("requestCode", -1);
        if (requestCode == MainActivity.EDIT_ITEM_REQUEST) {
            itemToEdit = (Item) intent.getSerializableExtra("item");
            populateFromEditItem(itemToEdit);
            //gets the color from intent for adding, or from item intent for editing
            colorAdapter.setSelectedColor(itemToEdit.getColor());

            //because EDIT
            g.deleteButton.setVisibility(VISIBLE);
        } else {
            g.toolbarLayout.setTitle(getString(R.string.title_string));
            g.deleteButton.setVisibility(GONE);
            g.editName.requestFocus();
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
        g.autoDecSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int state = isChecked ? VISIBLE : GONE;
            g.autoDecLinearLayout.setVisibility(state);
        });
        g.autoDecNDaysPicker.setOnValueChangedListener((picker, oldVal, newVal) -> updateDaysTv(newVal));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.form_menu, menu);
        //Adding textChangedListener for g.editName
        MenuItem okButton = g.toolbar.getMenu().getItem(0);
        okButton.setEnabled(false);
        g.editName.addTextChangedListener(TextWatcherFactory.getTitleTextAndImageButton(
                g.editName,
                g.toolbarLayout,
                okButton));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_ok) {
            okButton();
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            cancel();
        }
        return false;
    }

    /**
     * Populate from edit item.
     *
     * @param itemToEdit the item to edit
     */
    private void populateFromEditItem(Item itemToEdit) {
        g.editName.setText(itemToEdit.getName());
        g.toolbarLayout.setTitle(itemToEdit.getName());
        g.editStock.setText(String.valueOf(itemToEdit.getRawStock()));
        g.widgetSwitch.setChecked(itemToEdit.getShowInWidget());
        if (itemToEdit.isAutoDec()) {
            g.autoDecLinearLayout.setVisibility(VISIBLE);
            g.autoDecSwitch.setChecked(true);
            int nDays = itemToEdit.getAutoDecNDays();
            g.autoDecNDaysPicker.setValue(nDays);
            updateDaysTv(nDays);
            g.autoDecPerDayPicker.setValue(itemToEdit.getAutoDecPerDay());
        }
    }

    /**
     * Ok button.
     */
    private void okButton() {
        Intent returnIntent = new Intent();
        String name = ((EditText) findViewById(R.id.editName)).getText().toString();
        int color = colorAdapter.getSelectedColor();
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
            itemToEdit.setColor(color);
            itemToEdit.setRawStock(stock);
            itemToEdit.setShowInWidget(showInWidget);
            //flatten if autodec has changed
            itemToEdit.flattenAndSetAutoDecIf(autodec, g.autoDecPerDayPicker.getValue(), g.autoDecNDaysPicker.getValue());

            returnIntent.putExtra("item", itemToEdit);
        } else {
            Item item;
            if (autodec) {
                item = new Item(name,
                        stock,
                        color,
                        showInWidget,
                        LocalDate.now(),
                        g.autoDecNDaysPicker.getValue(),
                        g.autoDecPerDayPicker.getValue());
            } else {
                item = new Item(name, stock, color, showInWidget);
            }
            returnIntent.putExtra("item", item);
        }
        returnIntent.putExtra("position", getIntent().getIntExtra("position", -1));
        setResult(RESULT_OK, returnIntent);
        System.out.println("FINISHED");
        finish();
    }


    /**
     * Update Days TextView
     *
     * @param value the value of the numberpicker
     */
    private void updateDaysTv(int value) {
        if (value == 1) {
            g.daysTV.setText(R.string.day);
        } else {
            g.daysTV.setText(R.string.days);
        }
    }

    /**
     * Delete button.
     *
     * @param view the view
     */
    public void deleteButton(View view) {
        // Use the Builder class for convenient dialog construction
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialog_Rounded);

        builder.setMessage("Are you sure you wish to delete " + itemToEdit.getName() + "?")
                .setPositiveButton("Delete", (dialog, id) -> {
                    Intent intent = new Intent();
                    intent.putExtra("item", itemToEdit);
                    setResult(MainActivity.RESULT_DELETE, intent);
                    finish();
                })
                .setNegativeButton("Cancel", (dialog, id) -> {
                    // cancelled
                });
        AlertDialog dialog = builder.create();
        dialog.getWindow().setDimAmount(0.0f);
        dialog.show();
    }


    /**
     * Sets click listener so that the softKeyboard is auto hidden when clicking outside.
     *
     * @param view the view
     */
    @SuppressLint("ClickableViewAccessibility")
    public void setupUI(View view) {
        // Set up touch listener for non-text box views to hide keyboard.
        if (view instanceof NumberPicker == false && view instanceof EditText == false) {
            view.setOnTouchListener((v, event) -> {
                //hide keyboard
                hideSoftKeyboard();
                if (g.editStock.hasFocus()) {
                    g.editStock.clearFocus();
                }
                return false;
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }

    public void hideSoftKeyboard() {
        InputMethodManager inputMethodManager =
                (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);

        inputMethodManager.hideSoftInputFromWindow(
                getWindow().getDecorView().getRootView().getWindowToken(),
                0);

    }

    /**
     * Cancel.
     */
    private void cancel() {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        cancel();
    }


}