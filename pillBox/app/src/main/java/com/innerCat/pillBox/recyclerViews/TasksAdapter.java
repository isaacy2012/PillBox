package com.innerCat.pillBox.recyclerViews;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.innerCat.pillBox.Item;
import com.innerCat.pillBox.R;
import com.innerCat.pillBox.activities.MainActivity;
import com.innerCat.pillBox.factories.SharedPreferencesFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Create the basic adapter extending from RecyclerView.Adapter
// Note that we specify the custom ViewHolder which gives us access to our views
public class TasksAdapter extends
        RecyclerView.Adapter<TasksAdapter.ViewHolder> {

    private List<Item> items;
    private Set<ViewHolder> mBoundViewHolders = new HashSet<>();

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView nameTextView;
        public TextView stockTextView;
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
            stockTextView = itemView.findViewById(R.id.stockTextView);

            refillButton = itemView.findViewById(R.id.refillButton);
            refillButton.setOnClickListener(v -> {
                ((MainActivity) context).refillItem(item, getAdapterPosition());
            });

            itemView.setOnClickListener(this);
        }

        /**
         * Handles the row being clicked
         * @param view the itemView
         */
        @Override
        public void onClick( View view ) {
            int position = getAdapterPosition(); // gets item position
            if (position != RecyclerView.NO_POSITION) { // Check if an item was deleted, but the user clicked it before the UI removed it
                Item item = items.get(position);
                item.decrementStock();
                ((MainActivity) context).updateItem(item, position);
            }
        }

    }

    /**
     * Pass in the tasks array into the Adapter
     *
     * @param items the list of Tasks
     */
    public TasksAdapter( List<Item> items ) {
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
    public TasksAdapter.ViewHolder onCreateViewHolder( ViewGroup parent, int viewType ) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View taskView = inflater.inflate(R.layout.list_item_main, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(taskView, context);
        return viewHolder;
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder( TasksAdapter.ViewHolder holder, int position ) {
        // Get the data model based on position
        holder.item = items.get(position);

        // Set item views based on your views and data model
        TextView nameTextView = holder.nameTextView;
        TextView stockTextView = holder.stockTextView;
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

        mBoundViewHolders.add(holder);
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
