package com.innerCat.pillBox.activities;

import android.animation.ValueAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.innerCat.pillBox.R;
import com.innerCat.pillBox.StringFormatter;
import com.innerCat.pillBox.databinding.RefillActivityBinding;
import com.innerCat.pillBox.databinding.RefillInputBinding;
import com.innerCat.pillBox.factories.DatabaseFactory;
import com.innerCat.pillBox.factories.TextWatcherFactory;
import com.innerCat.pillBox.factories.ToolbarAnimatorFactory;
import com.innerCat.pillBox.objects.Item;
import com.innerCat.pillBox.objects.Refill;
import com.innerCat.pillBox.recyclerViews.RefillAdapter;
import com.innerCat.pillBox.room.Converters;
import com.innerCat.pillBox.room.DataDao;
import com.innerCat.pillBox.room.Database;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.Comparator.reverseOrder;

/**
 * The type Refill activity.
 */
public class RefillActivity extends AppCompatActivity {

    /**
     * The Activity Binding.
     */
    private RefillActivityBinding g;

    /**
     * The Refill item.
     */
    private Item refillItem = null;

    /**
     * The Database.
     */
    Database database;
    /**
     * The Dao.
     */
    DataDao dao;
    /**
     * The Adapter.
     */
    RefillAdapter adapter;
    /**
     * The Edit mode.
     */
    boolean editMode;
    /**
     * The Select all mode.
     */
    boolean selectAllMode = false;
    /**
     * The Changed.
     */
    boolean changed = false;
    /**
     * The Delete refills.
     */
    ArrayList<Refill> deleteRefills = new ArrayList<>();

    private MenuItem editButton = null;


    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        g = RefillActivityBinding.inflate(getLayoutInflater());
        View view = g.getRoot();
        setContentView(view);
        setSupportActionBar(g.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        refillItem = (Item)intent.getSerializableExtra("item");
        int itemId = refillItem.getId();
        String name = refillItem.getName();


        g.appBar.addOnOffsetChangedListener(this::updateScroll);

        g.toolbarLayout.setTitle(name);

        //empty adapter
        adapter = RefillAdapter.empty();
        g.rvRefills.setAdapter(adapter);

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
            List<Refill> nonExpiringRefills = dao.getNonExpiringRefillsOfItemId(itemId);
            nonExpiringRefills.sort(( a, b ) -> a.getAmount() - b.getAmount());
            List<Refill> futureRefills = dao.getFutureRefillsOfItemId(itemId, Converters.todayString());
            //soonest first
            Collections.sort(futureRefills);
            List<Refill> expiredRefills = dao.getExpiredRefillsOfItemId(itemId, Converters.todayString());
            //least expired first
            expiredRefills.sort(reverseOrder());

            handler.post(() -> {
                // Create adapter passing in the sample user data
                adapter = new RefillAdapter(this, expiredRefills, nonExpiringRefills, futureRefills);
                // Attach the adapter to the recyclerview to populate items
                g.rvRefills.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                // Set layout manager to position the items
                g.rvRefills.setLayoutManager(new LinearLayoutManager(this));
                updateRVVisibility();
            });
        });
    }

    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.edit_menu, menu);
        editButton = g.toolbar.getMenu().getItem(0);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        if (item.getItemId() == R.id.action_edit) {
            editMode = !editMode;
            ValueAnimator colorAnimator = ToolbarAnimatorFactory.create(this, editMode,
                    g.toolbar.getMenu().getItem(0), g.toolbarLayout);
            colorAnimator.start();

            deleteRefills = new ArrayList<>();
            checkDelete();
            checkFAB(false);
            return true;
        }
        return false;
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
     * Update refill in background.
     *
     * @param updateRefill  the update refill
     * @param initialAmount the initial amount
     */
    private void updateRefillInBackground( Refill updateRefill, int initialAmount ) {
        changed = true;
        refillItem.decrementStockBy(initialAmount - updateRefill.getAmount());
        //ROOM Threads
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            //Background work here

            //If there is another refill of this item with the same expiry date, merge them together
            //so that their amounts are added into a single refill
            dao.update(updateRefill);
        });
    }

    /**
     * Edit refill item.
     *
     * @param refill   the refill
     * @param position the position
     */
    public void editRefillItem(Refill refill, int position) {
        int initialAmount = refill.getAmount();
        // Use the Builder class for convenient dialog construction
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialog_Rounded);
        RefillInputBinding refillG = RefillInputBinding.inflate(getLayoutInflater());
        final LocalDate[] date = {refill.getExpiryDate()};

        refillG.editRefill.setText(String.valueOf(refill.getAmount()));
        refillG.editRefill.requestFocus();

        //Set the behaviour of the expiry button
        if (date[0] != null) {
            refillG.expiryButton.setText(StringFormatter.dateToString(date[0]));
        }
        refillG.expiryButton.setOnClickListener(v -> {
            CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder()
                    .setValidator(DateValidatorPointForward.now());

            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select date")
                    .setCalendarConstraints(constraintsBuilder.build())
                    //set the selection to the day of the refill
                    .setSelection(date[0] == null ? MaterialDatePicker.todayInUtcMilliseconds() : date[0].toEpochDay()*1000*60*60*24)
                    .build();
            datePicker.show(getSupportFragmentManager(), "tag");
            datePicker.addOnPositiveButtonClickListener(selection -> { //long selection
                date[0] = LocalDate.from(LocalDateTime.ofInstant(Instant.ofEpochMilli(selection), ZoneId.systemDefault()));
                refillG.expiryButton.setText(StringFormatter.dateToString(date[0]));
            });
        });

        builder.setTitle(R.string.refill_amount)
                .setView(refillG.getRoot())
                .setPositiveButton("Ok", ( dialog, id ) -> {
                    //get the name of the Item to add
                    int refillAmount = Integer.parseInt(refillG.editRefill.getText().toString().trim());
                    refill.setAmount(refillAmount);
                    refill.setExpiryDate(date[0]);

//                    System.out.println("WINNOW: POS: " + position);
                    adapter.editRefill(this, refill, position);

                    updateRefillInBackground(refill, initialAmount);

                })
                .setNegativeButton("Cancel", ( dialog, id ) -> {});
        AlertDialog dialog = builder.create();
        dialog.getWindow().setDimAmount(0.0f);
        dialog.show();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        Button okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        okButton.setEnabled(true);
        refillG.editRefill.addTextChangedListener(TextWatcherFactory.getRefill(refillG.editRefill, okButton));
    }

    /**
     * When the deleteFAB is pressed
     *
     * @param view the view
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
                    .setPositiveButton("Delete", ( DialogInterface dialog, int id ) -> {
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
                                refillItem.decrementStockBy(refill.getAmount());
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
                    })
                    .setNegativeButton("Cancel", ( dialog, id ) -> {
                        // cancelled
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
        if (editMode) {
            g.rvRefills.setPadding(defHorizPadding, defTopPadding, defHorizPadding, Converters.fromDpToPixels(68, getResources()));
            g.deleteFAB.setVisibility(View.VISIBLE);
            g.deleteFAB.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_baseline_check_24));
            g.deleteFAB.setText("SELECT ALL");
            editButton.setIcon(R.drawable.ic_baseline_close_24);
        } else {
            g.rvRefills.setPadding(defHorizPadding, defTopPadding, defHorizPadding, defHorizPadding);
            g.deleteFAB.setVisibility(View.INVISIBLE);
            selectAllMode = false;
            editButton.setIcon(R.drawable.ic_baseline_edit_24);
        }
    }

    /**
     * Check the status of the extended FAB
     *
     * @param back the back
     */
    public void checkFAB( boolean back ) {
        if (deleteRefills.size() != 0) {
            if (g.deleteFAB.getText().equals("DELETE") == false) {
                g.deleteFAB.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_baseline_edit_24));
                g.deleteFAB.setText("DELETE");
            }
        } else {
            if (back) {
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
        if (changed) {
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


}