package com.innerCat.pillBox.activities;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.innerCat.pillBox.BuildConfig;
import com.innerCat.pillBox.R;
import com.innerCat.pillBox.databinding.MainActivityBinding;
import com.innerCat.pillBox.databinding.RefillInputBinding;
import com.innerCat.pillBox.databinding.UpdateTextBinding;
import com.innerCat.pillBox.factories.DatabaseFactory;
import com.innerCat.pillBox.factories.OnOffsetChangedListenerFactory;
import com.innerCat.pillBox.factories.SharedPreferencesFactory;
import com.innerCat.pillBox.factories.TextWatcherFactory;
import com.innerCat.pillBox.factories.ToolbarAnimatorFactory;
import com.innerCat.pillBox.objects.ColorItem;
import com.innerCat.pillBox.objects.Item;
import com.innerCat.pillBox.objects.Refill;
import com.innerCat.pillBox.recyclerViews.ItemAdapter;
import com.innerCat.pillBox.room.Converters;
import com.innerCat.pillBox.room.DataDao;
import com.innerCat.pillBox.room.Database;
import com.innerCat.pillBox.util.StringFormatter;
import com.innerCat.pillBox.util.Updates;
import com.innerCat.pillBox.widgets.HomeWidgetProvider;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static androidx.recyclerview.widget.StaggeredGridLayoutManager.VERTICAL;


/**
 * The type Main activity.
 */
public class MainActivity extends AppCompatActivity {

    int ANIMATION_DURATION = 0;

    private MainActivityBinding g;

    //private fields for the Dao and the Database
    public Database database;
    DataDao dao;
    ItemAdapter adapter;
    SharedPreferences sharedPreferences;

    //modes
    boolean editMode = false;

    public static final int ADD_ITEM_REQUEST = 1;
    public static final int EDIT_ITEM_REQUEST = 2;
    public static final int REFILL_EDIT_REQUEST = 3;
    public static final int SETTINGS_EDIT_REQUEST = 4;
    public static final int RESULT_DELETE = 123;
    public static final int RESULT_REFILL_CHANGED = 124;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        g = MainActivityBinding.inflate(getLayoutInflater());
        View view = g.getRoot();
        setContentView(view);
        setSupportActionBar(g.toolbar);

        //constants
        ANIMATION_DURATION = getResources().getInteger(R.integer.animation_duration);

        //shared preferences
        sharedPreferences = SharedPreferencesFactory.getSP(this);

        //Add offset listener for when the view is collapsing or expanded
        g.appBar.addOnOffsetChangedListener(OnOffsetChangedListenerFactory.create(this));

//        Updates.setUpdateUnseen(this);

        //if the user hasn't seen the update dialog yet, then show it
        if (Updates.shouldShowUpdateDialog(this)) {
            showUpdateDialog();
        }

        //empty adapter
        adapter = ItemAdapter.empty();
        g.rvItems.setAdapter(adapter);

        //initialise the database
        database = DatabaseFactory.create(this);
        dao = database.getDao();


        // Add the itemTouchHelper for drag and drop
        ItemTouchHelper.Callback callback = getItemTouchHelperCallback();

        // Create an `ItemTouchHelper` and attach it to the `RecyclerView`
        ItemTouchHelper ith = new ItemTouchHelper(callback);
        ith.attachToRecyclerView(g.rvItems);


        //ROOM Threads
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            //Background work here
            //NB: This is the new thread in which the database stuff happens
            //today rvItem
            List<Item> items = dao.getAllItems();
            for (Item item : items) {
                Refill expiringRefill = dao.getSoonestExpiringRefillOfItemId(item.getId(), Converters.todayString());
                item.setExpiringRefill(expiringRefill);
            }


            handler.post(() -> {
                // Create adapter passing in the sample user data
                adapter = new ItemAdapter(items);
                // Attach the adapter to the recyclerview to populate items
                g.rvItems.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                // Set layout manager to position the items
                //g.rvItems.setLayoutManager(new GridLayoutManager(this, 2));
                g.rvItems.setLayoutManager(new StaggeredGridLayoutManager(2, VERTICAL));
                // Set the initial padding
                updateRVPadding();
            });
        });

        //set timer to refresh at 12:00
        Handler timerHandler = new Handler();
        // Execute items on main thread
        Runnable runItem = this::newDay;
        timerHandler.postDelayed(runItem, getDelayToStartOfTomorrow());
    }

    /**
     * Refresh rv items.
     */
    private void refreshRVItems() {
        Handler handler = new Handler(Looper.getMainLooper());
        Executors.newSingleThreadExecutor().execute(() -> {
            //Background work here
            List<Item> items = dao.getAllItems();
            for (Item item : items) {
                Refill expiringRefill = dao.getSoonestExpiringRefillOfItemId(item.getId(), Converters.todayString());
                item.setExpiringRefill(expiringRefill);
            }
            adapter.setItems(items);

            handler.post(() -> {
                adapter.notifyDataSetChanged();
            });
        });
    }

    /**
     * When the view is resumed
     */
    @Override
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            //ROOM Threads
            updateHomeWidget();

            //only update rv if widget asked for an update
            boolean widgetUpdate = sharedPreferences.getBoolean("widgetUpdate", false);
            long todayEpoch = LocalDate.now().toEpochDay();
            boolean dateUpdate = todayEpoch != sharedPreferences.getLong("dateUpdate", todayEpoch);
            if (widgetUpdate || dateUpdate) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("widgetUpdate", false);
                editor.apply();
                refreshRVItems();
            }
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong("dateUpdate", LocalDate.now().toEpochDay());
            editor.apply();

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.edit_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_edit) {
            editMode = !editMode;
            ValueAnimator colorAnimator = ToolbarAnimatorFactory.create(this, editMode,
                    g.toolbar.getMenu().getItem(0), g.toolbarLayout);
            colorAnimator.start();
            return true;
        } else if (item.getItemId() == R.id.action_settings) {
            toSettings();
            return true;
        }
        return false;
    }


    /**
     * Focus on color.
     *
     * @param color the color
     */
    public void focusOnColor(int color) {
        g.appBar.setExpanded(true, true);
        adapter.setFocusColor(color);
    }

    /**
     * Reset color focus, resetting the adapter's focus color to ColorItem.NO_COLOR
     */
    public void resetColorFocus() {
        adapter.reset();
    }

    /**
     * Update home widget.
     */
    private void updateHomeWidget() {
        HomeWidgetProvider.broadcastUpdate(this);
    }

    /**
     * Called at 00:00, updates the RVItems and updates the lastTakenTV for the widget
     */
    public void newDay() {
        refreshRVItems();
        adapter.checkLastTaken();
    }

    /**
     * @return the delay until 00:00 tomorrow
     */
    public long getDelayToStartOfTomorrow() {
        Calendar calendar = Calendar.getInstance();
        long currentTimestamp = calendar.getTimeInMillis();
        calendar.add(Calendar.DATE, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 1);
        long diffTimestamp = calendar.getTimeInMillis() - currentTimestamp;
        return (diffTimestamp < 0 ? 0 : diffTimestamp);
    }


    /**
     * Update an existing item in the database
     *
     * @param item     the item to change
     * @param position its position in the RecyclerView
     */
    public void updateItem(Item item, int position) {
        //ROOM Threads
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            //Background work here
            dao.update(item);
            handler.post(() -> {
                //UI Thread work here
                // Notify the adapter that an item was changed at position
                adapter.setItem(item, position);
                adapter.notifyChanged(this, position);
                updateHomeWidget();
            });
        });
    }

    /**
     * Update multiple items in the background, without notifying UI
     *
     * @param updated the updated
     */
    public void updateMultipleInBackground(List<Item> updated) {
        //ROOM Threads
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            //Background work here
            System.out.println("UPDATED IN DAO");
            for (Item item : updated) {
                dao.update(item);
            }
        });
    }

    /**
     * Remove an existing item in the database
     *
     * @param item     the item to remove
     * @param position its position in the RecyclerView
     */
    public void removeItem(Item item, int position) {
        //ROOM Threads
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            //Background work here
            dao.removeItemById(item.getId());
            handler.post(() -> {
                //UI Thread work here
                // Notify the adapter that an item was removed at position
                adapter.removeItem(this, item, position);
                updateHomeWidget();
                updateRVPadding();
            });
        });
    }

    /**
     * Updates the recylerView padding. If there are an even number of items then padding to
     * accomodate for the FAB button, otherwise no padding
     */
    private void updateRVPadding() {
        int normal = Converters.fromDpToPixels(8, getResources());
        if (adapter.getItemCount() % 2 == 0) { //if even
            int bottom = Converters.fromDpToPixels(80, getResources());
            g.rvItems.setPadding(normal, normal, normal, bottom);
        } else {
            g.rvItems.setPadding(normal, normal, normal, normal);
        }
    }


    /**
     * Refill item.
     *
     * @param item     the item
     * @param position the position
     */
    public void refillItem(Item item, int position) {
        //get the UI elements
        g.fab.setVisibility(View.INVISIBLE);
        RefillInputBinding refillG = RefillInputBinding.inflate(getLayoutInflater());
        final LocalDate[] date = new LocalDate[1];

        refillG.editRefill.requestFocus();

        //Set the behaviour of the expiry button
        refillG.expiryButton.setOnClickListener(v -> {
            CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder()
                    .setValidator(DateValidatorPointForward.now());

            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select date")
                    .setCalendarConstraints(constraintsBuilder.build())
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();
            datePicker.show(getSupportFragmentManager(), "tag");
            datePicker.addOnPositiveButtonClickListener(selection -> { //long selection
                date[0] = LocalDate.from(LocalDateTime.ofInstant(Instant.ofEpochMilli(selection), ZoneId.systemDefault()));
                refillG.expiryButton.setText(StringFormatter.dateToString(date[0]));
            });
        });

        // Use the Builder class for convenient dialog construction
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialog_Rounded);
        builder.setTitle("Add Refill")
                .setView(refillG.getRoot())
                .setPositiveButton("Ok", (dialog, id) -> {
                    //get the name of the Item to add
                    int refillAmount = Integer.parseInt(refillG.editRefill.getText().toString().trim());
                    Refill refill;
                    refill = new Refill(item.getId(), refillAmount, date[0]);

                    //if there is an expiry date
                    if (date[0] != null) {
                        Refill itemRefill = item.getExpiringRefill();
                        if (itemRefill == null || refill.getExpiryDate().isBefore(itemRefill.getExpiryDate())) {
                            item.setExpiringRefill(refill);
                        }
                        if (itemRefill != null && itemRefill.getExpiryDate().isEqual(refill.getExpiryDate())) {
                            //merge if it's the same date as another refill
                            itemRefill.mergeWith(refill);
                            updateRefillInBackground(itemRefill);
                            adapter.notifyItemChanged(position);
                        } else {
                            //add the refill
                            addRefillInBackground(refill);
                        }
                    } else {
                        //add the refill
                        addRefillInBackground(refill);
                    }
                    item.refillByAmount(refillAmount);
                    updateItem(item, position);

                    //set the visibility of the fab
                    g.fab.setVisibility(View.VISIBLE);
                })
                .setNegativeButton("Cancel", (dialog, id) -> {
                    // User cancelled the dialog
                    g.fab.setVisibility(View.VISIBLE);
                });
        AlertDialog dialog = builder.create();
        dialog.setOnCancelListener(dialog1 -> {
            // dialog dismisses
            g.fab.setVisibility(View.VISIBLE);
        });
        dialog.getWindow().setDimAmount(0.0f);
        dialog.show();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        Button okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        okButton.setEnabled(false);
        refillG.editRefill.addTextChangedListener(TextWatcherFactory.getRefill(refillG.editRefill, okButton));
    }

    /**
     * Add a addRefill to the database
     *
     * @param addRefill the addRefill
     */
    private void addRefillInBackground(Refill addRefill) {
        //ROOM Threads
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            //Background work here

            //If there is another refill of this item with the same expiry date, merge them together
            //so that their amounts are added into a single refill
            Refill refillToUpdate = null;
            List<Refill> refillsOfSameItem = dao.getFutureRefillsOfItemId(addRefill.getItemId(),
                    Converters.todayString());
            for (Refill refill : refillsOfSameItem) {
                if (refill.getExpiryDate().equals(addRefill.getExpiryDate())) {
                    refillToUpdate = refill;
                }
            }
            if (refillToUpdate != null) {
                refillToUpdate.mergeWith(addRefill);
                dao.update(refillToUpdate);
            } else {
                dao.insert(addRefill);
            }
        });
    }

    /**
     * Update refill in background.
     *
     * @param updateRefill the update refill
     */
    private void updateRefillInBackground(Refill updateRefill) {
        //ROOM Threads
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            //Background work here
            dao.update(updateRefill);
        });
    }

    /**
     * Add an item to the database
     *
     * @param item the item
     */
    private void addItem(Item item) {
        //ROOM Threads
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            //Background work here
            long id = dao.insert(item);
            item.setId((int) id);
            handler.post(() -> {
                //UI Thread work here
                // Add a new item
                adapter.addItem(this, item);
                g.rvItems.scrollToPosition(0);
                updateHomeWidget();
                updateRVPadding();
            });
        });
    }

    /**
     * Gets edit mode.
     *
     * @return the edit mode
     */
    public boolean getEditMode() {
        return this.editMode;
    }


    public void fabButton(View view) {
        Intent intent = new Intent(this, FormActivity.class);
        int requestCode = ADD_ITEM_REQUEST;
        intent.putExtra("requestCode", requestCode);
        intent.putExtra("color", adapter.getFocusColor());
        startActivityForResult(intent, requestCode);
    }

    private void showUpdateDialog() {
        UpdateTextBinding updateTextG = UpdateTextBinding.inflate(getLayoutInflater());
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialog_Rounded);
        TextView updateTextView = updateTextG.updateTV;

        updateTextView.setText(Html.fromHtml(Updates.getUpdateBodyString(), Html.FROM_HTML_MODE_COMPACT));
        updateTextView.setMovementMethod(new LinkMovementMethod());

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        updateTextView.setMaxHeight(height / 2);

        String updateString = ("What's new in version " + BuildConfig.VERSION_NAME + ":");

        builder.setTitle(updateString)
                .setView(updateTextG.getRoot())
                .setPositiveButton("Ok", (dialog, id) -> Updates.setUpdateSeen(this))
                .setNeutralButton("Leave a review", null);

        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.setOnCancelListener(dialog1 -> Updates.setUpdateSeen(this));

        Button neutralButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
        neutralButton.setOnClickListener(v -> {
            // dialog doesn't dismiss
            Updates.setUpdateSeen(this);
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.setData(Uri.parse(getString(R.string.google_play_listing)));
            startActivity(intent);
        });
    }

    /**
     * Opens the formActivity to update an item
     *
     * @param item     the item
     * @param position the position
     */
    public void toFormUpdate(Item item, int position) {
        Intent intent = new Intent(this, FormActivity.class);
        intent.putExtras(Converters.getExtrasFromItemAndPosition(item, position));
        int requestCode = EDIT_ITEM_REQUEST;
        intent.putExtra("requestCode", requestCode);
        startActivityForResult(intent, requestCode);
    }

    /**
     * To refill.
     *
     * @param item     the item
     * @param position the position
     */
    public void toRefill(Item item, int position) {
        Intent intent = new Intent(this, RefillActivity.class);
        intent.putExtras(Converters.getExtrasFromItemAndPosition(item, position));
        startActivityForResult(intent, REFILL_EDIT_REQUEST);
    }

    private ItemTouchHelper.Callback getItemTouchHelperCallback() {
        Context context = this;
        return new ItemTouchHelper.Callback() {
            /**
             * when an item is in the process of being moved
             * @param recyclerView  the recyclerView
             * @param viewHolder    the viewholder
             * @param target        the target viewHolder
             * @return whether the move was handled
             */
            public boolean onMove(@NonNull RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                // get the viewHolder's and target's positions in your adapter data, swap them
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();
                List<Item> items = adapter.getItems();
                if (fromPosition == toPosition) {
                    return true;
                } else {
                    Item thisItem = items.get(fromPosition);
                    items.remove(fromPosition);
                    items.add(toPosition, thisItem);
                }
                // and notify the adapter that its dataset has changed
                adapter.notifyMoved(context, viewHolder.getAdapterPosition(), target.getAdapterPosition());
                updateHomeWidget();
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            }

            //defines the enabled move directions in each state (idle, swiping, dragging).
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView,
                                        @NonNull RecyclerView.ViewHolder viewHolder) {
                return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG,
                        ItemTouchHelper.DOWN | ItemTouchHelper.UP | ItemTouchHelper.START | ItemTouchHelper.END);
            }

            @Override
            public boolean isLongPressDragEnabled() {
                return getEditMode() && (adapter.getFocusColor() == ColorItem.NO_COLOR);
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder,
                                    float dX, float dY, int actionState, boolean isCurrentlyActive) {
                CardView cardView = viewHolder.itemView.findViewById(R.id.cardView);
                float end = Converters.fromDpToPixels(0, getResources());
                if (isCurrentlyActive) {
                    end = Converters.fromDpToPixels(8, getResources());
                }
                cardView.animate().z(end);
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
    }

    /**
     * To settings.
     */
    public void toSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivityForResult(intent, SETTINGS_EDIT_REQUEST);
        
    }

    /**
     * When there is a result from an activity
     *
     * @param requestCode the requestCode
     * @param resultCode  the resultCode
     * @param data        the data from the activity
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case RESULT_OK: {
                if (requestCode == ADD_ITEM_REQUEST || requestCode == EDIT_ITEM_REQUEST) {
                    Item item = (Item) data.getSerializableExtra("item");
                    int position = data.getIntExtra("position", -1);
                    if (item.getColor() != adapter.getFocusColor() && adapter.getFocusColor() != ColorItem.NO_COLOR) {
                        resetColorFocus();
                    }
                    switch (requestCode) {
                        case ADD_ITEM_REQUEST:
                            addItem(item);
                            break;
                        case EDIT_ITEM_REQUEST:
                            updateItem(item, position);
                            break;
                    }
                } else if (requestCode == SETTINGS_EDIT_REQUEST) {
                    refreshRVItems();
                }
                break;
            }
            case RESULT_DELETE: {
                Item item = (Item) data.getSerializableExtra("item");
                int pos = item.getViewHolderPosition();
                removeItem(item, pos);
                break;
            }
            case RESULT_REFILL_CHANGED:
                if (requestCode == REFILL_EDIT_REQUEST) {
                    Item item = (Item) data.getSerializableExtra("item");
                    int pos = data.getIntExtra("position", -1);
                    //ROOM Threads
                    ExecutorService executor = Executors.newSingleThreadExecutor();
                    Handler handler = new Handler(Looper.getMainLooper());
                    executor.execute(() -> {
                        //Background work here
                        if (item.getId() == -1 || pos == -1) {
                            return;
                        }
                        Refill expiringRefill = dao.getSoonestExpiringRefillOfItemId(item.getId(),
                                Converters.todayString());
                        item.setExpiringRefill(expiringRefill);
                        adapter.setItem(item, pos);
                        //update the item in the database
                        dao.update(item);
                        handler.post(() -> {
                            //UI Thread work here
                            // Add a new item
                            adapter.notifyItemChanged(pos);
                            updateHomeWidget();
                        });
                    });
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onBackPressed() {
        if (adapter.getFocusColor() != ColorItem.NO_COLOR) {
            resetColorFocus();
        } else {
            super.onBackPressed();
        }
    }

}