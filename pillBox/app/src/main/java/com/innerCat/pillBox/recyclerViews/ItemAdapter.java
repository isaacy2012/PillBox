package com.innerCat.pillBox.recyclerViews;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.innerCat.pillBox.R;
import com.innerCat.pillBox.StringFormatter;
import com.innerCat.pillBox.activities.MainActivity;
import com.innerCat.pillBox.databinding.MainRvItemBinding;
import com.innerCat.pillBox.factories.SharedPreferencesFactory;
import com.innerCat.pillBox.objects.ColorItem;
import com.innerCat.pillBox.objects.Item;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static java.time.temporal.ChronoUnit.DAYS;

// Create the basic adapter extending from RecyclerView.Adapter
// Note that we specify the custom ViewHolder which gives us access to our views
public class ItemAdapter extends
        RecyclerView.Adapter<ItemAdapter.ViewHolder> {

    private List<Item> visibleItems;
    private List<Item> allItems;
    private Set<ViewHolder> mBoundViewHolders = new HashSet<>();
    private int focusColor = ColorItem.NO_COLOR;

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        MainRvItemBinding g;

        public Item item;
        public Context context;

        public ViewHolder(Context context, MainRvItemBinding g) {
            super(g.getRoot());
            this.g = g;
            this.context = context;

            g.refillButton.setOnClickListener(v -> {
                ((MainActivity) context).refillItem(item, getAdapterPosition());
            });

            g.colorDot.setOnClickListener(v -> {
                ((MainActivity) context).focusOnColor(item.getColor());
            });

            g.getRoot().setOnClickListener(this);
            g.getRoot().setOnLongClickListener(this);
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
                    Item item = visibleItems.get(position);
                    item.decrementStock();
                    ((MainActivity) context).updateItem(item, position);
                }
            } else {
                int position = getAdapterPosition(); // gets item position
                Item item = visibleItems.get(position);
                ((MainActivity) context).toFormUpdate(item, position);
            }
        }

        /**
         * To ensure that onClick is not activated at the same time
         * @param view
         * @return
         */
        @Override
        public boolean onLongClick(View view) {
            int position = getAdapterPosition(); // gets item position
            if (((MainActivity) context).getEditMode() == false) {
                Item item = visibleItems.get(position);
                ((MainActivity) context).toRefill(item, position);
            }
            return true;
        }

    }

    /**
     * Pass in the tasks array into the Adapter
     *
     * @param items the items
     */
    public ItemAdapter( List<Item> items ) {
        this.allItems = new ArrayList<>(items);
        this.visibleItems = new ArrayList<>(items);
    }


    /**
     * Focus on color.
     */
    public void focusOnColor() {
        if (focusColor == ColorItem.NO_COLOR) {
            return;
        }
        for (int i = 0; i < visibleItems.size(); i++) {
            notifyItemRemoved(0);
        }
        visibleItems.clear();
        for (Item item : allItems) {
            if (item.getColor() == focusColor) {
                visibleItems.add(0, item);
                notifyItemInserted(0);
            }
        }
    }

    /**
     * Sets focus color.
     *
     * @param color the color
     */
    public void setFocusColor(int color) {
        this.focusColor = color;
        focusOnColor();
    }

    /**
     * Reset.
     */
    public void reset() {
        focusColor = ColorItem.NO_COLOR;
        List<Item> addBackItems = new ArrayList<>(allItems);
        visibleItems = new ArrayList<>(allItems);
        for (Item removeItem : visibleItems) {
            notifyItemRemoved(0);
            addBackItems.remove(removeItem);
        }
        for (int i = 0; i < addBackItems.size(); i++) {
            notifyItemInserted(0);
        }
    }

    /**
     * Sets item.
     *
     * @param item     the item
     * @param position the position
     */
    @NoColorFocus
    public void setItem( Item item, int position) {
        allItems.set(position, item);
        focusOnColor();
    }

    /**
     * Add a item
     *
     * @param context  the context
     * @param item     the Item to add
     * @param position the position of the new Item in the List
     */
    public void addItem( Context context, Item item, int position) {
        allItems.add(position, item);
        //if it's color selection mode AND its the same color
        if (focusColor != ColorItem.NO_COLOR && item.getColor() == focusColor) {
            visibleItems.add(0, item);
            notifyItemInserted(0);
            updateIndexesInRange(context, position);
        } else { //otherwise, reset
            ((MainActivity)context).resetColorFocus();
            notifyInserted(context, position);
        }
    }

    /**
     * Gets focus color.
     *
     * @return the focus color
     */
    public int getFocusColor() {
        return focusColor;
    }

    /**
     * Remove an item.
     *
     * @param context  the context
     * @param item     the item to remove
     * @param position the position of the Item in the List
     */
    @NoColorFocus
    public void removeItem( Context context, Item item, int position) {
        allItems.remove(item);
        visibleItems.remove(item);
        notifyRemoved( context, position );
    }

    /**
     * Update indexes in range, starting from the item at fromIndex to the end
     *
     * @param context   the context
     * @param fromIndex the from index
     */
    public void updateIndexesInRange( Context context, int fromIndex ) {
        List<Item> updated = new ArrayList<>();
        for (int i = fromIndex; i < allItems.size(); i++) {
            Item thisItem = allItems.get(i);
            thisItem.setViewHolderPosition(allItems.indexOf(thisItem));
            updated.add(thisItem);
        }
        ((MainActivity)context).updateMultipleInBackground(updated);
    }

    /**
     * Notify that an item has been moved. Calls the super method notifyItemMoved
     *
     * @param context      the context
     * @param fromPosition the from position
     * @param toPosition   the to position
     */
    @NoColorFocus
    public void notifyMoved( Context context, int fromPosition, int toPosition) {
        super.notifyItemMoved(fromPosition, toPosition);
        if (fromPosition != toPosition) {
            updateIndexesInRange(context, Math.min(fromPosition, toPosition));
        }
    }

    /**
     * Notify that an item has been changed. Calls the super method notifyItemChanged
     *
     * @param context  the context
     * @param position the position
     */
    @NoColorFocus
    public void notifyChanged( Context context, int position ) {
        super.notifyItemChanged( position );
    }

    /**
     * Notify that an item has been inserted. Calls the super method notifyItemInserted
     *
     * @param context  the context
     * @param position the position
     */
    public void notifyInserted( Context context, int position ) {
        super.notifyItemInserted(position);
        updateIndexesInRange(context, position);
        focusOnColor();
    }

    /**
     * Notify that an item has been removed. Calls the super method notifyItemRemoved
     *
     * @param context  the context
     * @param position the position
     */
    public void notifyRemoved( Context context, int position ) {
        super.notifyItemRemoved(position);
        updateIndexesInRange(context, position);
        focusOnColor();
    }

    // Usually involves inflating a layout from XML and returning the holder
    @NonNull
    @Override
    public ItemAdapter.ViewHolder onCreateViewHolder( ViewGroup parent, int viewType ) {
        Context context = parent.getContext();
        return new ViewHolder(context,
                MainRvItemBinding.inflate(LayoutInflater.from(context), parent, false));
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder( ViewHolder holder, int position ) {
        // Get the data model based on position
        holder.item = visibleItems.get(position);
        MainRvItemBinding g = holder.g;


        int stock = holder.item.getStock();

        g.nameTV.setText(holder.item.getName());

        if (holder.item.getColor() != ColorItem.NO_COLOR) {
            g.colorDot.setVisibility(VISIBLE);
            g.colorDot.setBackgroundColor(holder.item.getColor());

        } else {
            g.colorDot.setVisibility(GONE);

            //Setting the margin on the NameTV
            ConstraintLayout.LayoutParams params = ((ConstraintLayout.LayoutParams)g.nameTV.getLayoutParams());
            int defMargin = params.topMargin;
            int rightMargin = params.rightMargin;
            params.setMargins(defMargin,defMargin,rightMargin,0);
            g.nameTV.setLayoutParams(params);
        }

        //Set the text of the stockTV
        g.stockTV.setText(String.valueOf(stock));
        //Set the color if the stock is low
        int stockThreshold = SharedPreferencesFactory.getSP(holder.context)
                .getInt("stockThreshold", 10);
        if (stock <= stockThreshold) {
            g.stockTV.setTextColor(ContextCompat.getColor(holder.context, R.color.primaryColor));
        } else {
            //get the default color
            int[] attribute = new int[] { android.R.attr.textColor };
            TypedArray array = holder.context.getTheme().obtainStyledAttributes(attribute);
            int color = array.getColor(0, Color.TRANSPARENT);
            g.stockTV.setTextColor(color);
        }

        if (holder.item.getExpiringRefill() != null) {
            //4 weeks show warning
            LocalDate expiringDate = holder.item.getExpiringRefill().getExpiryDate();
            int warningDayThreshold = SharedPreferencesFactory.getSP(holder.context)
                    .getInt("warningDayThreshold", 28);
            long daysTillExpiry = DAYS.between(LocalDate.now(), expiringDate);
            if (daysTillExpiry <= warningDayThreshold) {
                g.expiryTV.setVisibility(VISIBLE);
                //Set the text of the stockTV
                g.expiryTV.setText(StringFormatter.getExpiryText(holder.item.getExpiringRefill()));
                //Set the color of the text if it is close
                int redDayThreshold = SharedPreferencesFactory.getSP(holder.context)
                        .getInt("redDayThreshold", 7);
                if (daysTillExpiry <= redDayThreshold) {
                    g.expiryTV.setTextColor(ContextCompat.getColor(holder.context, R.color.primaryColor));
                } else {
                    //get the default color
                    int[] attribute = new int[]{ android.R.attr.textColor };
                    TypedArray array = holder.context.getTheme().obtainStyledAttributes(attribute);
                    int color = array.getColor(0, Color.TRANSPARENT);
                    g.expiryTV.setTextColor(color);
                }
            } else {
                g.expiryTV.setVisibility(GONE);
            }
        } else {
            g.expiryTV.setVisibility(GONE);
        }

        //set the text of the last taken text view
        g.lastTakenTV.setText(StringFormatter.getLastTakenText(holder.item));

        mBoundViewHolders.add(holder);
    }


    /**
     * Check last taken.
     */
    public void checkLastTaken() {
        for (ViewHolder viewHolder : mBoundViewHolders) {
            viewHolder.g.lastTakenTV.setText(StringFormatter.getLastTakenText(viewHolder.item));
        }
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return visibleItems.size();
    }

    /**
     * Gets items.
     *
     * @return the items
     */
    public List<Item> getItems() {
        return this.visibleItems;
    }

    /**
     * Sets items.
     *
     * @param items the items
     */
    public void setItems( List<Item> items ) {
        this.allItems = items;
        focusOnColor();
    }
}
