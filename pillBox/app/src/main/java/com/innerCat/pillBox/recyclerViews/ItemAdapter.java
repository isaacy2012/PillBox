package com.innerCat.pillBox.recyclerViews;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.innerCat.pillBox.Item;
import com.innerCat.pillBox.R;
import com.innerCat.pillBox.activities.MainActivity;
import com.innerCat.pillBox.factories.SharedPreferencesFactory;
import com.innerCat.pillBox.factories.TextWatcherFactory;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.time.temporal.ChronoUnit.DAYS;

// Create the basic adapter extending from RecyclerView.Adapter
// Note that we specify the custom ViewHolder which gives us access to our views
public class ItemAdapter extends
        RecyclerView.Adapter<ItemAdapter.ViewHolder> {

    private List<Item> items;
    private Set<ViewHolder> mBoundViewHolders = new HashSet<>();

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView nameTextView;
        public TextView stockTextView;
        public TextView lastTakenTextView;
        public ImageButton refillButton;
        public Item item;
        public Context context;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder( View itemView, Context context ) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);
            this.context = context;


            nameTextView = itemView.findViewById(R.id.nameView);
            lastTakenTextView = itemView.findViewById(R.id.lastTakenTextView);
            stockTextView = itemView.findViewById(R.id.stockTextView);

            refillButton = itemView.findViewById(R.id.refillButton);
            refillButton.setOnClickListener(v -> {
                ((MainActivity) context).refillItem(item, getAdapterPosition());
            });

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        /**
         * Handles the row being clicked
         * @param view the itemView
         */
        @Override
        public void onClick( View view ) {
            if (((MainActivity) context).getEditMode() == false) {
                int position = getAdapterPosition(); // gets item position
                if (position != RecyclerView.NO_POSITION) { // Check if an item was deleted, but the user clicked it before the UI removed it
                    Item item = items.get(position);
                    item.decrementStock();
                    ((MainActivity) context).updateItem(item, position);
                }
            } else {
                int position = getAdapterPosition(); // gets item position
                Item item = items.get(position);
                // Use the Builder class for convenient dialog construction
                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context, R.style.MaterialAlertDialog_Rounded);

                //get the UI elements
                ExtendedFloatingActionButton fab = ((MainActivity) context).findViewById(R.id.floatingActionButton);
                fab.setVisibility(View.INVISIBLE);
                View editTextView = LayoutInflater.from(context).inflate(R.layout.text_input, null);
                EditText input = editTextView.findViewById(R.id.editName);

                //Set the capitalisation
                input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

                input.requestFocus();

                builder.setMessage("Name")
                        .setView(editTextView)
                        .setPositiveButton("Ok", ( dialog, id ) -> {
                            //get the name of the Task to edit
                            String newName = input.getText().toString();
                            //edit the item
                            item.setName(newName);
                            ((MainActivity) context).updateItem(item, position);
                        })
                        .setNegativeButton("Cancel", ( dialog, id ) -> {
                            // User cancelled the dialog
                            fab.setVisibility(View.VISIBLE);
                        })
                        .setNeutralButton("Delete", ( dialog, id ) -> {
                            items.remove(item);
                            ((MainActivity) context).deleteItem(item, position);
                            fab.setVisibility(View.VISIBLE);
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
                input.addTextChangedListener(TextWatcherFactory.getNonEmptyTextWatcher(input, okButton));
            }
        }

        /**
         * To ensure that onClick is not activated at the same time
         * @param view
         * @return
         */
        @Override
        public boolean onLongClick(View view) {
            return true; // or false
        }

    }

    /**
     * Pass in the tasks array into the Adapter
     *
     * @param items the list of Tasks
     */
    public ItemAdapter( List<Item> items ) {
        this.items = items;
    }

    /**
     * Add a item
     *
     * @param position the position of the new Item in the List
     * @param item     the Item to add
     */
    public void addItem( int position, Item item ) {
        items.add(position, item);
    }

    // Usually involves inflating a layout from XML and returning the holder
    @NonNull
    @Override
    public ItemAdapter.ViewHolder onCreateViewHolder( ViewGroup parent, int viewType ) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View taskView = inflater.inflate(R.layout.recycler_view_item_main, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(taskView, context);
        return viewHolder;
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder( ItemAdapter.ViewHolder holder, int position ) {
        // Get the data model based on position
        holder.item = items.get(position);

        // Set item views based on your views and data model
        TextView nameTextView = holder.nameTextView;
        TextView stockTextView = holder.stockTextView;
        TextView lastTakenTextView = holder.lastTakenTextView;
        nameTextView.setText(holder.item.getName());
        int stock = holder.item.getStock();
        stockTextView.setText(String.valueOf(stock));

        //Set the color if the stock is low
        int stockThreshold = SharedPreferencesFactory.getSP(holder.context)
                .getInt("stockThreshold", 10);
        if (stock <= stockThreshold) {
            stockTextView.setTextColor(ContextCompat.getColor(holder.context, R.color.primaryColor));
        } else {
            //get the default color
            int[] attribute = new int[] { android.R.attr.textColor };
            TypedArray array = holder.context.getTheme().obtainStyledAttributes(attribute);
            int color = array.getColor(0, Color.TRANSPARENT);
            stockTextView.setTextColor(color);
        }

        //set the text of the last taken text view
        setLastTakenText( lastTakenTextView, holder.item );

        mBoundViewHolders.add(holder);
    }

    /**
     * Sets last taken text.
     *
     * @param lastTakenTextView the last taken text view
     * @param item              the item
     */
    public void setLastTakenText( TextView lastTakenTextView, Item item ) {
        LocalDate lastUsed = item.getLastUsed();
        if (lastUsed != null) {
            int daysBetween = (int) DAYS.between(lastUsed, LocalDate.now());
            StringBuilder sb = new StringBuilder();
            sb.append("Last taken ");
            if (daysBetween == 0) {
                sb.append("today");
            } else if (daysBetween == 1) {
                sb.append("yesterday");
            } else {
                sb.append(daysBetween).append(" days ago");
            }
            lastTakenTextView.setText(sb.toString());
        } else {
            lastTakenTextView.setText("");
        }
    }

    /**
     * Check last taken.
     */
    public void checkLastTaken() {
        for (ViewHolder viewHolder : mBoundViewHolders) {
            setLastTakenText(viewHolder.lastTakenTextView, viewHolder.item);
        }
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return items.size();
    }

    public List<Item> getTasks() {
        return this.items;
    }


}
