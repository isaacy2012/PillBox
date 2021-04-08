package com.innerCat.pillBox.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.innerCat.pillBox.R;
import com.innerCat.pillBox.factories.ColorFactory;
import com.innerCat.pillBox.factories.TextWatcherFactory;
import com.innerCat.pillBox.objects.Item;
import com.innerCat.pillBox.room.Converters;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class FormActivity extends AppCompatActivity {

    int requestCode;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form_activity);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        //Adding textChangedListener for nameEdit
        CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.toolbar_layout);
        ImageButton okButton = findViewById(R.id.okButton);
        EditText nameEdit = findViewById(R.id.editName);
        EditText stockEdit = findViewById(R.id.editStock);
        SwitchMaterial widgetSwitch = findViewById(R.id.widgetSwitch);
        Button deleteButton = findViewById(R.id.deleteButton);

        nameEdit.addTextChangedListener(TextWatcherFactory.getTitleTextAndImageButton(
                nameEdit,
                collapsingToolbarLayout,
                okButton));

        //Add offset listener for when the view is collapsing or expanded
        AppBarLayout appBarLayout = findViewById(R.id.app_bar);
        Context context = this;
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {

                //if (Math.abs(verticalOffset)-appBarLayout.getTotalScrollRange() == 0) {
                if (Math.abs(verticalOffset) > appBarLayout.getTotalScrollRange()*0.3) {
                    //  Collapsing
                    nameEdit.setFocusableInTouchMode(false);
                    nameEdit.setFocusable(false);
                    nameEdit.setTextColor(ContextCompat.getColor(context, R.color.transparent));
                    //nameEdit.setVisibility(GONE);
                    stockEdit.requestFocus();
                } else {
                    //Expanded
                    //nameEdit.setVisibility(VISIBLE);
                    //get the default color
                    int color = ColorFactory.getDefaultTextColor(context);
                    nameEdit.setTextColor(color);

                    nameEdit.setFocusableInTouchMode(true);
                    nameEdit.setFocusable(true);
                }
            }
        });

        Intent intent = getIntent();
        requestCode = intent.getIntExtra("requestCode", -1);
        if (requestCode == MainActivity.EDIT_ITEM_REQUEST) {
            String name = intent.getStringExtra("name");
            int stock = intent.getIntExtra("stock", 0);
            boolean showInWidget = intent.getBooleanExtra("showInWidget", false);
            nameEdit.setText(name);
            stockEdit.setText(String.valueOf(stock));
            widgetSwitch.setChecked(showInWidget);
            deleteButton.setVisibility(VISIBLE);
        } else {
            deleteButton.setVisibility(GONE);
            nameEdit.requestFocus();
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
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
        boolean showInWidget = ((SwitchMaterial)findViewById(R.id.widgetSwitch)).isChecked();

        Item item = new Item(name, stock, showInWidget);
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
}