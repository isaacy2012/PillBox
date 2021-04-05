package com.innerCat.pillBox.activities;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.innerCat.pillBox.R;
import com.innerCat.pillBox.StringFormatter;
import com.innerCat.pillBox.factories.DatabaseFactory;
import com.innerCat.pillBox.factories.OnOffsetChangedListenerFactory;
import com.innerCat.pillBox.factories.SharedPreferencesFactory;
import com.innerCat.pillBox.factories.TextWatcherFactory;
import com.innerCat.pillBox.objects.Item;
import com.innerCat.pillBox.objects.Refill;
import com.innerCat.pillBox.recyclerViews.ItemAdapter;
import com.innerCat.pillBox.room.Converters;
import com.innerCat.pillBox.room.DataDao;
import com.innerCat.pillBox.room.Database;
import com.innerCat.pillBox.widgets.HomeWidgetProvider;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * The type Main activity.
 */
public class MainActivity extends AppCompatActivity {

    int ANIMATION_DURATION = 0;

    //private fields for the Dao and the Database
    public Database database;
    DataDao dao;
    RecyclerView rvItems;
    ItemAdapter adapter;
    SharedPreferences sharedPreferences;
    Item itemToUpdate = null;

    boolean editMode = false;

    public static final int ADD_ITEM_REQUEST = 1;
    public static final int EDIT_ITEM_REQUEST = 2;
    public static final int REFILL_EDIT_REQUEST = 3;
    public static final int RESULT_DELETE = 123;
    public static final int RESULT_OK_CHANGED = 124;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        //constants
        ANIMATION_DURATION = getResources().getInteger(R.integer.animation_duration);

        //shared preferences
        sharedPreferences = getSharedPreferences("preferences", Context.MODE_PRIVATE);

        //Add offset listener for when the view is collapsing or expanded
        AppBarLayout appBarLayout = findViewById(R.id.app_bar);
        appBarLayout.addOnOffsetChangedListener(OnOffsetChangedListenerFactory.create(this));

        //setUpdateUnseen("update_1_dot_1");

        //if the user hasn't seen the update dialog yet, then show it
//        if (sharedPreferences.getBoolean("update_1_dot_0", false) == false) {
//            showUpdateDialog();
//        }

        //initialise the database
        database = DatabaseFactory.create(this);
        dao = database.getDao();

        //get the recyclerview in activity layout
        rvItems = findViewById(R.id.rvItems);

        // Extend the Callback class
        Context context = this;
        ItemTouchHelper.Callback callback = new ItemTouchHelper.Callback() {
            /**
             * when an item is in the process of being moved
             * @param recyclerView  the recyclerView
             * @param viewHolder    the viewholder
             * @param target        the target viewHolder
             * @return              whether the move was handled
             */
            public boolean onMove( @NonNull RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
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
            public void onSwiped( @NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            }

            //defines the enabled move directions in each state (idle, swiping, dragging).
            @Override
            public int getMovementFlags( @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG,
                        ItemTouchHelper.DOWN | ItemTouchHelper.UP | ItemTouchHelper.START | ItemTouchHelper.END);
            }

            @Override
            public boolean isLongPressDragEnabled() {
                return getEditMode();
                //return true;
            }

            @Override
            public void onChildDraw( @NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                     @NonNull RecyclerView.ViewHolder viewHolder,
                                     float dX, float dY, int actionState, boolean isCurrentlyActive) {
                CardView cardView = viewHolder.itemView.findViewById(R.id.cardView);
                float end = Converters.fromDpToPixels(8, getResources());
                if (isCurrentlyActive) {
                    end = Converters.fromDpToPixels(16, getResources());
                }
                cardView.animate().z(end);
                //cardView.setCardElevation(end);
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        // Create an `ItemTouchHelper` and attach it to the `RecyclerView`
        ItemTouchHelper ith = new ItemTouchHelper(callback);
        ith.attachToRecyclerView(rvItems);


        //ROOM Threads
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            //Background work here
            //NB: This is the new thread in which the database stuff happens
            //today rvItem
            List<Item> items = dao.getAllItems();
            for (Item item : items) {
                Refill expiringRefill = dao.getSoonestExpiringRefillOfItemId(item.getId());
                item.setExpiringRefill(expiringRefill);
            }


            handler.post(() -> {
                // Create adapter passing in the sample user data
                adapter = new ItemAdapter(items);
                // Attach the adapter to the recyclerview to populate items
                rvItems.setAdapter(adapter);
                // Set layout manager to position the items
                rvItems.setLayoutManager(new GridLayoutManager(this, 2));
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
     * When the view is resumed
     */
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            //ROOM Threads
            updateHomeWidget();

            //only update rv if widget asked for an update
            SharedPreferences sharedPreferences = SharedPreferencesFactory.getSP(this);
            if (sharedPreferences.getBoolean("widgetUpdate", false) == true) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("widgetUpdate", false);
                editor.apply();

                ExecutorService executor = Executors.newSingleThreadExecutor();
                Handler handler = new Handler(Looper.getMainLooper());
                executor.execute(() -> {
                    //Background work here
                    //NB: This is the new thread in which the database stuff happens
                    //today rvItem
                    List<Item> items = dao.getAllItems();
                    for (Item item : items) {
                        Refill expiringRefill = dao.getSoonestExpiringRefillOfItemId(item.getId());
                        item.setExpiringRefill(expiringRefill);
                    }
                    adapter.setItems(items);

                    handler.post(() -> {
                        adapter.notifyDataSetChanged();
                    });
                });
            }
        }
    }

    /**
     * Update home widget.
     */
    private void updateHomeWidget() {
        HomeWidgetProvider.broadcastUpdate(this);
    }

    /**
     * Called at 00:00, updates the lastTakenTV for the widget
     */
    public void newDay() {
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
     * Set a particular update as seen
     * @param updateString the update string
     */
    private void setUpdateSeen(String updateString) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(updateString, true);
        editor.apply();
    }

    /**
     * Set a particular update as unseen
     * @param updateString the update string
     */
    private void setUpdateUnseen(String updateString) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(updateString, false);
        editor.apply();
    }

    /**
     * Update an existing item in the database
     * @param item the item to change
     * @param position its position in the RecyclerView
     */
    public void updateItem( Item item, int position ) {
        //ROOM Threads
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            //Background work here
            dao.update(item);
            handler.post(() -> {
                //UI Thread work here
                // Notify the adapter that an item was changed at position
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
    public void updateMultipleInBackground( List<Item> updated ) {
        //ROOM Threads
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            //Background work here
            for (Item item : updated) {
                dao.update(item);
            }
        });
    }

    /**
     * Remove an existing item in the database
     * @param item the item to remove
     * @param position its position in the RecyclerView
     */
    public void removeItem( Item item, int position ) {
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
        int normal = Converters.fromDpToPixels(16, getResources());
        if (adapter.getItemCount()%2 == 0) { //if even
            int bottom = Converters.fromDpToPixels(80, getResources());
            rvItems.setPadding(normal, normal, normal, bottom);
        } else {
            rvItems.setPadding(normal, normal, normal, normal);
        }
    }


    /**
     * Refill item.
     *
     * @param item     the item
     * @param position the position
     */
    public void refillItem( Item item, int position) {
        //get the UI elements
        ExtendedFloatingActionButton fab = findViewById(R.id.floatingActionButton);
        fab.setVisibility(View.INVISIBLE);
        View refillInput = LayoutInflater.from(this).inflate(R.layout.refill_input, null);
        EditText refillTV = refillInput.findViewById(R.id.editRefill);
        Button expiryButton = refillInput.findViewById(R.id.expiryButton);
        final LocalDate[] date = new LocalDate[1];

        refillTV.requestFocus();

        //Set the behaviour of the expiry button
        expiryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
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
                    expiryButton.setText(StringFormatter.dateToString(date[0]));
                });
            }
        });

        // Use the Builder class for convenient dialog construction
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialog_Rounded);
        builder.setMessage("Refill Amount")
                .setView(refillInput)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //get the name of the Item to add
                        int refillAmount = Integer.parseInt(refillTV.getText().toString().trim());
                        Refill refill;
                        if (date[0] != null) {
                            refill = new Refill( item.getId(), refillAmount, date[0] );
                            Refill itemRefill = item.getExpiringRefill();
                            if (itemRefill == null || refill.getExpiryDate().isBefore(itemRefill.getExpiryDate())) {
                                item.setExpiringRefill(refill);
                            }
                            if (itemRefill != null && itemRefill.getExpiryDate().isEqual(refill.getExpiryDate())) {
                                itemRefill.mergeWith(refill);
                                updateRefillInBackground(itemRefill);
                                adapter.notifyItemChanged(position);
                            } else {
                                //add the refill
                                addRefillInBackground(refill);
                            }
                        }
                        item.refillByAmount( refillAmount );
                        updateItem(item, position);

                        //set the visibility of the fab
                        fab.setVisibility(View.VISIBLE);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        fab.setVisibility(View.VISIBLE);
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.setOnCancelListener(dialog1 -> {
            // dialog dismisses
            fab.setVisibility(View.VISIBLE);
        });
        dialog.getWindow().setDimAmount(0.0f);
        dialog.show();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        Button okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        okButton.setEnabled(false);
        refillTV.addTextChangedListener(TextWatcherFactory.getRefill(refillTV, okButton));
    }

    /**
     * Add a addRefill to the database
     *
     * @param addRefill the addRefill
     */
    private void addRefillInBackground( Refill addRefill ) {
        //ROOM Threads
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            //Background work here

            //If there is another refill of this item with the same expiry date, merge them together
            //so that their amounts are added into a single refill
            Refill refillToUpdate = null;
            List<Refill> refillsOfSameItem = dao.getRefillsOfItemId(addRefill.getItemId());
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
    private void updateRefillInBackground( Refill updateRefill ) {
        //ROOM Threads
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            //Background work here
            dao.update(updateRefill);
        });
    }

    /**
     * Add a item to the database
     *
     * @param name  the name of the item
     * @param stock the stock
     */
    private void addItem( String name, int stock, boolean showInWidget) {
        //ROOM Threads
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            //Background work here
            Item item = new Item(name, stock, showInWidget);
            long id = dao.insert(item);
            item.setId((int) id);
            handler.post(() -> {
                //UI Thread work here
                // Add a new item
                adapter.addItem(this, item, 0);
                //rvItems.scheduleLayoutAnimation();
                rvItems.scrollToPosition(0);
                //rvItems.scheduleLayoutAnimation();
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

    /**
     * Edit button.
     *
     * @param view the view
     */
    public void editButton( View view ) {
        editMode = !editMode;
        ImageButton editButton = findViewById(R.id.editButton);
        CollapsingToolbarLayout toolbarLayout = findViewById(R.id.toolbar_layout);
        AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) toolbarLayout.getLayoutParams();
        int transparent = ContextCompat.getColor(this, R.color.transparent);
        int primaryColor = ContextCompat.getColor(this, R.color.primaryColor);
        ValueAnimator colorAnimator = new ValueAnimator();
        if (editMode == true) {
            //set the toolbar to red
            colorAnimator.setIntValues(transparent, primaryColor);
            editButton.setImageResource(R.drawable.ic_baseline_close_24);
        } else {
            //set the toolbar to clear
            colorAnimator.setIntValues(primaryColor, transparent);
            editButton.setImageResource(R.drawable.ic_baseline_edit_24);
        }
        colorAnimator.setEvaluator(new ArgbEvaluator());
        colorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int animatedValue = (int) animation.getAnimatedValue();
                toolbarLayout.setContentScrimColor(animatedValue);
            }
        });
        //colorAnimator.setDuration(ANIMATION_DURATION);
        colorAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        colorAnimator.start();
        toolbarLayout.setLayoutParams(params);
    }

    /** Called when the user taps the FAB button */
    public void fabButton(View view) {
        Intent intent = new Intent(this, FormActivity.class);
        int requestCode = ADD_ITEM_REQUEST;
        intent.putExtra("requestCode", requestCode);
        startActivityForResult(intent, requestCode);
    }

    /**
     * Opens the formActivity to update an item
     *
     * @param item     the item
     * @param position the position
     */
    public void toFormUpdate(Item item, int position) {
        itemToUpdate = item;
        Intent intent = new Intent(this, FormActivity.class);
        intent.putExtras(Converters.getEditBundleFromItemAndPosition(item, position));
        int requestCode = EDIT_ITEM_REQUEST;
        intent.putExtra("requestCode", requestCode);
        startActivityForResult(intent, requestCode);
    }

    /**
     * To refill.
     *
     * @param itemId the item id
     */
    public void toRefill( Item item, int position ) {
        Intent intent = new Intent(this, RefillActivity.class);
        intent.putExtras(Converters.getEditBundleFromItemAndPosition(item, position));
        startActivityForResult(intent, REFILL_EDIT_REQUEST);
    }

    /**
     * Inject data into an item.
     *
     * @param item the item
     * @param data the data
     */
    private void injectDataToItem(Item item, Intent data) {
        String name = data.getStringExtra("name");
        int stock = data.getIntExtra("stock", 0);
        boolean showInWidget = data.getBooleanExtra("showInWidget", false);
        item.setName(name);
        item.setStock(stock);
        item.setShowInWidget(showInWidget);
    }

    /**
     * When there is a result from an activity
     * @param requestCode the requestCode
     * @param resultCode the resultCode
     * @param data the data from the activity
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK) {
            int pos = data.getIntExtra("position", -1);
            switch (requestCode) {
                case ADD_ITEM_REQUEST:
                    String name = data.getStringExtra("name");
                    int stock = data.getIntExtra("stock", 0);
                    boolean showInWidget = data.getBooleanExtra("showInWidget", false);
                    addItem(name, stock, showInWidget);
                    break;
                case EDIT_ITEM_REQUEST:
                    if (itemToUpdate == null) {
                        return;
                    }
                    injectDataToItem(itemToUpdate, data);
                    updateItem(itemToUpdate, pos);
                    itemToUpdate = null;
                    break;
            }
        } else if (resultCode == RESULT_DELETE) {
            if (itemToUpdate == null) {
                return;
            }
            int pos = itemToUpdate.getViewHolderPosition();
            removeItem(itemToUpdate, pos);
        } else if (resultCode == RESULT_OK_CHANGED) {
            if (requestCode == REFILL_EDIT_REQUEST) {
                int pos = data.getIntExtra("position", -1);
                //ROOM Threads
                ExecutorService executor = Executors.newSingleThreadExecutor();
                Handler handler = new Handler(Looper.getMainLooper());
                executor.execute(() -> {
                    //Background work here
                    int id = data.getIntExtra("id", -1);
                    if (id == -1 || pos == -1) {
                        return;
                    }
                    Item replaceItem = dao.getItem(id);
                    Refill expiringRefill = dao.getSoonestExpiringRefillOfItemId(replaceItem.getId());
                    replaceItem.setExpiringRefill(expiringRefill);
                    adapter.getItems().set(pos, replaceItem);
                    handler.post(() -> {
                        //UI Thread work here
                        // Add a new item
                        adapter.notifyItemChanged(pos);
                        updateHomeWidget();
                    });
                });
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}