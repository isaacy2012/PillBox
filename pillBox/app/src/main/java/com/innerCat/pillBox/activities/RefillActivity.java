package com.innerCat.pillBox.activities;

import android.animation.ValueAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.innerCat.pillBox.R;
import com.innerCat.pillBox.databinding.RefillActivityBinding;
import com.innerCat.pillBox.factories.DatabaseFactory;
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

    private RefillActivityBinding g;

    private Item refillItem = null;

    Database database;
    DataDao dao;
    RefillAdapter adapter;
    boolean editMode;
    boolean selectAllMode = false;
    boolean changed = false;
    ArrayList<Refill> deleteRefills = new ArrayList<>();


    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        g = RefillActivityBinding.inflate(getLayoutInflater());
        View view = g.getRoot();
        setContentView(view);

        Intent intent = getIntent();
        refillItem = (Item)intent.getSerializableExtra("item");
        int itemId = refillItem.getId();
        String name = refillItem.getName();


        g.appBar.addOnOffsetChangedListener(this::updateScroll);

        g.toolbarLayout.setTitle(name);

        //initialise the database
        database = DatabaseFactory.create(this);
        dao = database.getDao();


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
                g.rvRefills.setAdapter(adapter);
                // Set layout manager to position the items
                g.rvRefills.setLayoutManager(new LinearLayoutManager(this));
                updateRVVisibility();
            });
        });
    }

    /**
     * Update scroll.
     *
     * @param appBarLayout   the app bar layout
     * @param verticalOffset the vertical offset
     */
    private void updateScroll(AppBarLayout appBarLayout, int verticalOffset) {
        //set the behaviour for the coordinatorLayout
        if (Math.abs(verticalOffset)-appBarLayout.getTotalScrollRange() == 0) {
            //  Collapsed
            g.coordinatorLayout.setClipChildren(true);
        } else if (Math.abs(verticalOffset) > 0) {
            //  In the middle
            float percentage = (float) (Math.abs(verticalOffset)/(appBarLayout.getTotalScrollRange()*0.2));
            if (percentage > 1) { percentage = 1; }
            g.subtitleTV.setAlpha(1-percentage);
        } else {
            //Expanded
            g.coordinatorLayout.setClipChildren(false);
            g.subtitleTV.setAlpha(1);

        }
    }

    /**
     * Update rv visibility.
     */
    private void updateRVVisibility() {
        if (adapter.getItemCount() == 0) {
            g.rvRefills.setVisibility(View.GONE);
            g.emptyRefillRVView.setVisibility(View.VISIBLE);
        } else {
            g.rvRefills.setVisibility(View.VISIBLE);
            g.emptyRefillRVView.setVisibility(View.GONE);
        }
    }

    /**
     * Edit button.
     *
     * @param view the view
     */
    public void editButton( View view ) {
        editMode = !editMode;
        ValueAnimator colorAnimator = ToolbarAnimatorFactory.create(this, editMode,
                g.editButton, g.toolbarLayout);
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
                            g.rvRefills.setPadding(defHorizPadding, defTopPadding, defHorizPadding, defHorizPadding);
                            g.deleteFAB.setVisibility(View.INVISIBLE);
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
            g.rvRefills.setPadding(defHorizPadding, defTopPadding, defHorizPadding, Converters.fromDpToPixels(68, getResources()));
            g.deleteFAB.setVisibility(View.VISIBLE);
            g.deleteFAB.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_baseline_check_24));
            g.deleteFAB.setText("SELECT ALL");
            g.editButton.setImageResource(R.drawable.ic_baseline_close_24);
        } else {
            g.rvRefills.setPadding(defHorizPadding, defTopPadding, defHorizPadding, defHorizPadding);
            g.deleteFAB.setVisibility(View.INVISIBLE);
            selectAllMode = false;
            g.editButton.setImageResource(R.drawable.ic_baseline_edit_24);
        }
    }

    /**
     * Check the status of the extended FAB
     */
    public void checkFAB( boolean back ) {
        if (deleteRefills.size() != 0) {
            if (g.deleteFAB.getText().equals("DELETE") == false) {
                g.deleteFAB.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_baseline_edit_24));
                g.deleteFAB.setText("DELETE");
            }
        } else {
            if (back == true) {
                if (g.deleteFAB.getText().equals("SELECT ALL") == false) {
                    g.deleteFAB.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_baseline_edit_24));
                    g.deleteFAB.setText("SELECT ALL");
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
        int position = getIntent().getIntExtra("position", -1);
        intent.putExtras(Converters.getExtrasFromItemAndPosition(refillItem, position));
        if (changed == true) {
            setResult(MainActivity.RESULT_REFILL_CHANGED, intent);
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