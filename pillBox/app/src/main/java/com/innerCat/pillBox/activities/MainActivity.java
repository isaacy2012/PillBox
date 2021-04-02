package com.innerCat.pillBox.activities;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
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
import androidx.room.ColumnInfo;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.innerCat.pillBox.Item;
import com.innerCat.pillBox.R;
import com.innerCat.pillBox.factories.ItemDatabaseFactory;
import com.innerCat.pillBox.factories.TextWatcherFactory;
import com.innerCat.pillBox.recyclerViews.ItemAdapter;
import com.innerCat.pillBox.room.Converters;
import com.innerCat.pillBox.room.ItemDao;
import com.innerCat.pillBox.room.ItemDatabase;
import com.innerCat.pillBox.widgets.HomeWidgetProvider;

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
    public static ItemDatabase itemDatabase;
    RecyclerView rvItems;
    ItemAdapter adapter;
    SharedPreferences sharedPreferences;

    boolean editMode = false;

    @ColumnInfo(defaultValue = "0")

    private final int LIST_TASK_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //constants
        ANIMATION_DURATION = getResources().getInteger(R.integer.animation_duration);

        //streak
        sharedPreferences = getSharedPreferences("preferences", Context.MODE_PRIVATE);

        //setUpdateUnseen("update_1_dot_1");

        //if the user hasn't seen the update dialog yet, then show it
//        if (sharedPreferences.getBoolean("update_1_dot_0", false) == false) {
//            showUpdateDialog();
//        }

        //initialise the database
        itemDatabase = ItemDatabaseFactory.getItemDatabase(this);

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
            List<Item> items = itemDatabase.itemDao().getAllItems();


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
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());
            executor.execute(() -> {
                //Background work here
                //NB: This is the new thread in which the database stuff happens
                //today rvItem
                List<Item> items = itemDatabase.itemDao().getAllItems();
                adapter.setItems(items);

                handler.post(() -> {
                    adapter.notifyDataSetChanged();
                });
            });
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
            itemDatabase.itemDao().update(item);
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
            ItemDao dao = itemDatabase.itemDao();
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
            itemDatabase.itemDao().removeById(item.getId());
            handler.post(() -> {
                //UI Thread work here
                // Notify the adapter that an item was changed at position
                adapter.notifyRemoved( this, position);
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
        int normal = Converters.fromDpToPixels(10, getResources());
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
        // Use the Builder class for convenient dialog construction
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialog_Rounded);

        //get the UI elements
        ExtendedFloatingActionButton fab = findViewById(R.id.floatingActionButton);
        fab.setVisibility(View.INVISIBLE);
        View editTV = LayoutInflater.from(this).inflate(R.layout.refill_input, null);
        EditText refillInput = editTV.findViewById(R.id.editRefill);

        refillInput.requestFocus();

        builder.setMessage("Refill Amount")
                .setView(editTV)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //get the name of the Item to add
                        int refillAmount = Integer.parseInt(refillInput.getText().toString().trim());
                        //add the item
                        item.refill(refillAmount);
                        updateItem(item, position);
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
        refillInput.addTextChangedListener(TextWatcherFactory.getNonEmptyRefillWatcher(refillInput, okButton));

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
            long id = itemDatabase.itemDao().insert(item);
            item.setId((int) id);
            handler.post(() -> {
                //UI Thread work here
                // Add a new item
                adapter.addItem(0, item);
                // Notify the adapter that an item was inserted at position 0
                adapter.notifyInserted(this, 0);
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
            params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
                    | AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED);

            editButton.setImageResource(R.drawable.ic_baseline_close_24);
        } else {
            //set the toolbar to clear
            colorAnimator.setIntValues(primaryColor, transparent);
            params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL);

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

        // Use the Builder class for convenient dialog construction
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialog_Rounded);

        //get the UI elements
        ExtendedFloatingActionButton fab = findViewById(R.id.floatingActionButton);
        fab.setVisibility(View.INVISIBLE);
        View editView = LayoutInflater.from(this).inflate(R.layout.add_item_input, null);
        EditText nameInput = editView.findViewById(R.id.editName);
        EditText stockInput = editView.findViewById(R.id.editStock);
        SwitchMaterial showInWidgetSwitch = editView.findViewById(R.id.widgetSwitch);

        //Set the capitalisation
        nameInput.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

        nameInput.requestFocus();

        builder.setMessage("Name")
                .setView(editView)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //get the name of the Item to add
                        String name = nameInput.getText().toString();
                        int stock = 0;
                        try {
                            stock = Integer.parseInt(stockInput.getText().toString().trim());
                        } catch (NumberFormatException ignored) {}

                        //add the item
                        addItem(name, stock, showInWidgetSwitch.isChecked());
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
        nameInput.addTextChangedListener(TextWatcherFactory.getNonEmptyTextWatcher(nameInput, okButton));
    }

}