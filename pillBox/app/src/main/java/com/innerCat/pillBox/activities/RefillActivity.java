package com.innerCat.pillBox.activities;

import android.animation.ValueAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.innerCat.pillBox.R;
import com.innerCat.pillBox.factories.DatabaseFactory;
import com.innerCat.pillBox.factories.OnOffsetChangedListenerFactory;
import com.innerCat.pillBox.factories.ToolbarAnimatorFactory;
import com.innerCat.pillBox.objects.Item;
import com.innerCat.pillBox.objects.Refill;
import com.innerCat.pillBox.recyclerViews.RefillAdapter;
import com.innerCat.pillBox.room.Converters;
import com.innerCat.pillBox.room.DataDao;
import com.innerCat.pillBox.room.Database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.Comparator.reverseOrder;

public class RefillActivity extends AppCompatActivity {

    Database database;
    DataDao dao;
    RecyclerView rvRefills;
    ExtendedFloatingActionButton deleteFAB;
    ImageButton editButton;
    RefillAdapter adapter;
    boolean editMode;
    boolean selectAllMode = false;
    boolean changed = false;
    ArrayList<Refill> deleteRefills = new ArrayList<>();

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        int itemId = intent.getIntExtra("id", -1);
        String name = intent.getStringExtra("name");

        setContentView(R.layout.refill_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Add offset listener for when the view is collapsing or expanded
        AppBarLayout appBarLayout = findViewById(R.id.app_bar);
        appBarLayout.addOnOffsetChangedListener(OnOffsetChangedListenerFactory.create(this));

        CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.toolbar_layout);
        collapsingToolbarLayout.setTitle("Refills of " + name);

        //initialise the database
        database = DatabaseFactory.create(this);
        dao = database.getDao();

        //get the recyclerview in activity layout
        rvRefills = findViewById(R.id.rvRefills);

        //get the FAB
        deleteFAB = findViewById(R.id.deleteFAB);
        editButton = findViewById(R.id.editButton);

        //ROOM Threads
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            //Background work here
            //NB: This is the new thread in which the database stuff happens
            //today rvItem
            List<Refill> futureRefills = dao.getFutureRefillsOfItemId(itemId, Converters.todayString());
            //soonest first
            Collections.sort(futureRefills);
            List<Refill> expiredRefills = dao.getExpiredRefillsOfItemId(itemId, Converters.todayString());
            //least expired first
            expiredRefills.sort(reverseOrder());

            handler.post(() -> {
                // Create adapter passing in the sample user data
                adapter = new RefillAdapter(expiredRefills, futureRefills);
                // Attach the adapter to the recyclerview to populate items
                rvRefills.setAdapter(adapter);
                // Set layout manager to position the items
                rvRefills.setLayoutManager(new LinearLayoutManager(this));
                updateRVVisibility();
            });
        });
    }

    /**
     * Update rv visibility.
     */
    private void updateRVVisibility() {
        TextView emptyView = findViewById(R.id.emptyRefillRVView);
        if (adapter.getItemCount() == 0) {
            rvRefills.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            rvRefills.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }

    /**
     * Edit button.
     *
     * @param view the view
     */
    public void editButton( View view ) {
        editMode = !editMode;
        CollapsingToolbarLayout toolbarLayout = findViewById(R.id.toolbar_layout);
        ValueAnimator colorAnimator = ToolbarAnimatorFactory.create(this, editMode,
                editButton, toolbarLayout);
        colorAnimator.start();

        deleteRefills = new ArrayList<>();
        checkDelete();
        checkFAB(false);
    }

    /**
     * When the deleteFAB is pressed
     *
     * @param view
     */
    public void onDeleteFAB( View view ) {
        if (deleteRefills.size() == 0) { //if there are no items in the deleteTasks list then the deleteFAB acts as a 'select all' button
            selectAllMode = true;
            adapter.selectAll(this);
        } else { //otherwise, delete all the items in the deleteTasks list
            // Use the Builder class for convenient dialog construction
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialog_Rounded);
            LayoutInflater inflater = LayoutInflater.from(this);

            builder.setMessage("Are you sure you wish to delete " + deleteRefills.size() + " " + (deleteRefills.size() > 1 ? "refills" : "refill") + "?")
                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        public void onClick( DialogInterface dialog, int id ) {
                            changed = true;
                            editMode = false;
                            selectAllMode = false;
                            int defHorizPadding = Converters.fromDpToPixels(16, getResources());
                            int defTopPadding = Converters.fromDpToPixels(10, getResources());
                            rvRefills.setPadding(defHorizPadding, defTopPadding, defHorizPadding, defHorizPadding);
                            deleteFAB.setVisibility(View.INVISIBLE);
                            checkDelete();

                            //ROOM Threads
                            ExecutorService executor = Executors.newSingleThreadExecutor();
                            Handler handler = new Handler(Looper.getMainLooper());
                            executor.execute(() -> {
                                //Background work here
                                for (Refill refill : deleteRefills) {
                                    Item refillItem = dao.getItem(refill.getItemId());
                                    refillItem.decrementStockBy(refill.getAmount());
                                    dao.update(refillItem);
                                    dao.removeRefillById(refill.getId());
                                }
                                handler.post(() -> {
                                    for (Refill refill : deleteRefills) {
                                        adapter.notifyItemRemoved(adapter.getRefillListObjects().indexOf(refill));
                                        adapter.getRefillListObjects().remove(refill);
                                    }
                                    deleteRefills.clear();
                                    updateRVVisibility();
                                });
                            });
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick( DialogInterface dialog, int id ) {
                            // cancelled
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.getWindow().setDimAmount(0.0f);
            dialog.show();
        }
        checkFAB(false);
    }

    /**
     * Check the status of the UI items with respect to the editMode
     */
    private void checkDelete() {
        adapter.checkDelete(getResources(), editMode);
        int defHorizPadding = Converters.fromDpToPixels(16, getResources());
        int defTopPadding = Converters.fromDpToPixels(10, getResources());
        if (editMode == true) {
            rvRefills.setPadding(defHorizPadding, defTopPadding, defHorizPadding, Converters.fromDpToPixels(68, getResources()));
            deleteFAB.setVisibility(View.VISIBLE);
            deleteFAB.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_baseline_check_24));
            deleteFAB.setText("SELECT ALL");
            editButton.setImageResource(R.drawable.ic_baseline_close_24);
        } else {
            rvRefills.setPadding(defHorizPadding, defTopPadding, defHorizPadding, defHorizPadding);
            deleteFAB.setVisibility(View.INVISIBLE);
            selectAllMode = false;
            editButton.setImageResource(R.drawable.ic_baseline_edit_24);
        }
    }

    /**
     * Check the status of the extended FAB
     */
    public void checkFAB( boolean back ) {
        if (deleteRefills.size() != 0) {
            if (deleteFAB.getText().equals("DELETE") == false) {
                deleteFAB.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_baseline_edit_24));
                deleteFAB.setText("DELETE");
            }
        } else {
            if (back == true) {
                if (deleteFAB.getText().equals("SELECT ALL") == false) {
                    deleteFAB.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_baseline_edit_24));
                    deleteFAB.setText("SELECT ALL");
                }
            }
        }
    }

    /**
     * Gets edit mode.
     *
     * @return the edit mode
     */
    public boolean getEditMode() {
        return editMode;
    }

    /**
     * Gets select all mode.
     *
     * @return the select all mode
     */
    public boolean getSelectAllMode() {
        return selectAllMode;
    }

    /**
     * Add delete refill.
     *
     * @param refill the refill
     */
    public void addDeleteRefill(Refill refill) {
        deleteRefills.add(refill);
        checkFAB(true);
    }

    /**
     * Remove delete refill.
     *
     * @param refill the refill
     */
    public void removeDeleteRefill(Refill refill) {
        deleteRefills.remove(refill);
        checkFAB(true);
    }

    @Override
    public void finish() {
        Intent intent = new Intent();
        intent.putExtra("position", getIntent().getIntExtra("position", -1));
        intent.putExtra("id", getIntent().getIntExtra("id", -1));
        if (changed == true) {
            setResult(MainActivity.RESULT_OK_CHANGED, intent);
        } else {
            setResult(RESULT_OK, intent);
        }
        super.finish();
    }

    /**
     * When the hardware/software back button is pressed
     */
    @Override
    public void onBackPressed() {
        finish();
    }


    /**
     * Software back button.
     *
     * @param view the view
     */
    public void backButton( View view ) {
        finish();
    }

}