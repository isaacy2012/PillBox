package com.innerCat.pillBox.activities;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.ColumnInfo;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.innerCat.pillBox.Item;
import com.innerCat.pillBox.R;
import com.innerCat.pillBox.factories.ItemDatabaseFactory;
import com.innerCat.pillBox.factories.TextWatcherFactory;
import com.innerCat.pillBox.recyclerViews.ItemAdapter;
import com.innerCat.pillBox.room.Converters;
import com.innerCat.pillBox.room.ItemDatabase;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


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
        itemDatabase = ItemDatabaseFactory.getTaskDatabase(this);

        //get the recyclerview in activity layout
        rvItems = findViewById(R.id.rvTasks);

        // Extend the Callback class
        ItemTouchHelper.Callback callback = new ItemTouchHelper.Callback() {
            //and in your imlpementaion of
            public boolean onMove( @NonNull RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                // get the viewHolder's and target's positions in your adapter data, swap them
                Collections.swap(adapter.getTasks(), viewHolder.getAdapterPosition(), target.getAdapterPosition());
                // and notify the adapter that its dataset has changed
                adapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
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
            //today rvTask
            List<Item> items = itemDatabase.itemDao().getAllItems();

            handler.post(() -> {
                // Create adapter passing in the sample user data
                adapter = new ItemAdapter(items);
                // Attach the adapter to the recyclerview to populate items
                rvItems.setAdapter(adapter);
                // Set layout manager to position the items
                rvItems.setLayoutManager(new GridLayoutManager(this, 2));
            });
        });

        //set timer to refresh at 12:00
        Handler timerHandler = new Handler();
        Runnable runTask = () -> {
            // Execute tasks on main thread
            newDay();
        };
        timerHandler.postDelayed(runTask, getDelayToStartOfTomorrow());
    }

    /**
     * When the view is resumed
     */
    public void onResume() {
        super.onResume();
    }

    /**
     * Called at 00:00, moves all Tasks in "Tomorrow" to "Today" and checks the visibility of the RecyclerViews
     */
    public void newDay() {
        //adapter.updateDays();
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
                adapter.notifyItemChanged(position);
            });
        });
    }

    /**
     * Remove an existing item in the database
     * @param item the item to remove
     * @param position its position in the RecyclerView
     */
    public void deleteItem( Item item, int position ) {
        //ROOM Threads
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            //Background work here
            itemDatabase.itemDao().removeById(item.getId());
            handler.post(() -> {
                //UI Thread work here
                // Notify the adapter that an item was changed at position
                adapter.notifyItemRemoved(position);
            });
        });
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
        View editTextView = LayoutInflater.from(this).inflate(R.layout.refill_input, null);
        EditText refillInput = editTextView.findViewById(R.id.editRefill);

        refillInput.requestFocus();

        builder.setMessage("Refill Amount")
                .setView(editTextView)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //get the name of the Task to add
                        int refillAmount = Integer.parseInt(refillInput.getText().toString().trim());
                        //add the task
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
    private void addItem( String name, int stock ) {
        //ROOM Threads
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            //Background work here
            Item item = new Item(name, stock);
            long id = itemDatabase.itemDao().insert(item);
            item.setId((int) id);
            handler.post(() -> {
                //UI Thread work here
                // Add a new task
                adapter.addItem(0, item);
                // Notify the adapter that an item was inserted at position 0
                adapter.notifyItemInserted(0);
                //rvTasks.scheduleLayoutAnimation();
                rvItems.scrollToPosition(0);
                //rvTasks.scheduleLayoutAnimation();
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
        if (editMode == true) {
            editButton.setImageResource(R.drawable.ic_baseline_close_24);
        } else {
            editButton.setImageResource(R.drawable.ic_baseline_edit_24);
        }
    }

    /** Called when the user taps the FAB button */
    public void fabButton(View view) {

        // Use the Builder class for convenient dialog construction
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialog_Rounded);

        //get the UI elements
        ExtendedFloatingActionButton fab = findViewById(R.id.floatingActionButton);
        fab.setVisibility(View.INVISIBLE);
        View editTextView = LayoutInflater.from(this).inflate(R.layout.add_item_input, null);
        EditText nameInput = editTextView.findViewById(R.id.editName);
        EditText stockInput = editTextView.findViewById(R.id.editStock);

        //Set the capitalisation
        nameInput.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

        nameInput.requestFocus();

        builder.setMessage("Name")
                .setView(editTextView)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //get the name of the Task to add
                        String name = nameInput.getText().toString();
                        int stock = Integer.parseInt(stockInput.getText().toString().trim());
                        //add the task
                        addItem(name, stock);
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
        nameInput.addTextChangedListener(TextWatcherFactory.getNonEmptyTextAndStockWatcher(nameInput, stockInput, okButton));
        stockInput.addTextChangedListener(TextWatcherFactory.getNonEmptyTextAndStockWatcher(nameInput, stockInput, okButton));
    }

}