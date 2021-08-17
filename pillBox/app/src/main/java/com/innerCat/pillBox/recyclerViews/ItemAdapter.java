package com.innerCat.pillBox.recyclerViews;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.innerCat.pillBox.R;
import com.innerCat.pillBox.util.StringFormatter;
import com.innerCat.pillBox.activities.MainActivity;
import com.innerCat.pillBox.databinding.MainRvItemBinding;
import com.innerCat.pillBox.factories.ColorFactory;
import com.innerCat.pillBox.factories.SharedPreferencesFactory;
import com.innerCat.pillBox.objects.ColorItem;
import com.innerCat.pillBox.objects.Item;
import com.innerCat.pillBox.room.Converters;

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

        public ViewHolder( Context context, MainRvItemBinding g ) {
            super(g.getRoot());
            this.g = g;
            this.context = context;

            g.refillButton.setOnClickListener(v -> {
                ((MainActivity) context).refillItem(item, getAdapterPosition());
            });

            g.colorDot.setOnClickListener(v -> {
                if (focusColor != item.getColor()) {
                    ((MainActivity) context).focusOnColor(item.getColor());
                } else {
                    ((MainActivity) context).resetColorFocus();
                }
            });

            g.getRoot().setOnClickListener(this);
            g.getRoot().setOnLongClickListener(this);
        }


        /**
         * Handles the row being clicked
         *
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
         *
         * @param view
         * @return
         */
        @Override
        public boolean onLongClick( View view ) {
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
        //weak link because no color focus
        this.visibleItems = allItems;
    }

    /**
     * Empty item adapter.
     *
     * @return the item adapter
     */
    public static ItemAdapter empty() {
        return new ItemAdapter(new ArrayList<>());
    }


    /**
     * Focus on color.
     */
    public void focusOnColor() {
        if (focusColor == ColorItem.NO_COLOR) {
            visibleItems = allItems;
            return;
        }
        //clearing
        for (int i = 0; i < visibleItems.size(); i++) {
            notifyItemRemoved(0);
        }

        visibleItems = new ArrayList<>();
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
    public void setFocusColor( int color ) {
        this.focusColor = color;
        focusOnColor();
    }

    /**
     * Reset.
     */
    public void reset() {
        focusColor = ColorItem.NO_COLOR;
        visibleItems.forEach(x -> notifyItemRemoved(0));
        visibleItems = allItems;
        allItems.forEach(x -> notifyItemInserted(0));
    }

    /**
     * Sets item.
     *
     * @param item     the item
     * @param position the position
     */
    public void setItem( Item item, int position ) {
        int visibleItemsPosition = visibleItems.indexOf(allItems.get(position));
        visibleItems.set(visibleItemsPosition, item);
        allItems.set(position, item);
        focusOnColor();
    }

    /**
     * Add a item
     *
     * @param context  the context
     * @param item     the Item to add
     */
    public void addItem( Context context, Item item ) {
        allItems.add(0, item);
        //if it's color selection mode and we're focused on the wrong color
        if (focusColor != ColorItem.NO_COLOR && item.getColor() != focusColor) {
            //reset the color focus
            ((MainActivity) context).resetColorFocus();
            notifyInserted(context, 0);
        } else { //otherwise just add as normal
            //if allItems isn't the same as visibleItems (because in focus color)
            if (allItems != visibleItems) {
                visibleItems.add(0, item);
            }
            notifyItemInserted(0);
            updateIndexesInRange(context, 0);
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
    public void removeItem( Context context, Item item, int position ) {
        allItems.remove(item);
        visibleItems.remove(item);
        notifyRemoved(context, position);
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
            System.out.println("TRIED TO SET IN UPDATE: " + thisItem.getName() + " to " + thisItem.getViewHolderPosition());
            updated.add(thisItem);
        }
        ((MainActivity) context).updateMultipleInBackground(updated);
    }

    /**
     * Notify that an item has been moved. Calls the super method notifyItemMoved
     *
     * @param context      the context
     * @param fromPosition the from position
     * @param toPosition   the to position
     */
    public void notifyMoved( Context context, int fromPosition, int toPosition ) {
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
    public void notifyChanged( Context context, int position ) {
        super.notifyItemChanged(position);
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


        int stock = holder.item.getCalculatedStock();

        g.nameTV.setText(holder.item.getName());

        //Getting the margin on the NameTV
        ConstraintLayout.LayoutParams params = ((ConstraintLayout.LayoutParams) g.nameTV.getLayoutParams());
        int defMargin = params.topMargin;
        int rightMargin = params.rightMargin;

        //set the colorDot and NameTV margin parameters
        if (holder.item.getColor() != ColorItem.NO_COLOR) {
            g.colorDot.setVisibility(VISIBLE);
            g.colorDot.setBackgroundColor(holder.item.getColor());

            params.setMargins(0, defMargin, rightMargin, 0);
        } else {
            g.colorDot.setVisibility(GONE);

            params.setMargins(defMargin, defMargin, rightMargin, 0);
        }
        g.nameTV.setLayoutParams(params);

        //If autodec
        if (holder.item.isAutoDec()) {
            g.cardView.setCardBackgroundColor(ColorFactory.getAttrColor(holder.context, R.attr.autoDecBgColor));
            g.cardView.setStrokeColor(ColorFactory.getAttrColor(holder.context, R.attr.autoDecBorderColor));
            g.cardView.setStrokeWidth(Converters.fromDpToPixels(2, holder.context.getResources()));
        } else {
            g.cardView.setCardBackgroundColor(ColorFactory.getAttrColor(holder.context, R.attr.colorOnCard));
            //no stroke width
            g.cardView.setStrokeWidth(0);
        }

        //Set the text of the stockTV
        g.stockTV.setText(String.valueOf(stock));
        //Set the color if the stock is low
        if (stock <= getStockThreshold(holder.context)) {
            g.stockTV.setTextColor(ContextCompat.getColor(holder.context, R.color.primaryColor));
            g.stockTV.setTypeface(null, Typeface.BOLD);
        } else {
            //get the default color
            g.stockTV.setTextColor(ColorFactory.getDefaultTextColor(holder.context));
            g.stockTV.setTypeface(null, Typeface.NORMAL);
        }

        //set the text of the expiryTV
        if (holder.item.getExpiringRefill() != null) {
            /*
            At 4 weeks or 28 days, show warning
             */
            LocalDate expiringDate = holder.item.getExpiringRefill().getExpiryDate();
            long daysTillExpiry = DAYS.between(LocalDate.now(), expiringDate);
            if (daysTillExpiry <= getWarningDayThreshold(holder.context)) {
                g.expiryTV.setVisibility(VISIBLE);
                //Set the text of the stockTV
                g.expiryTV.setText(StringFormatter.getExpiryText(holder.item.getExpiringRefill()));

                /*
                At red day threshold, make the text red
                 */
                if (daysTillExpiry <= getRedDayThreshold(holder.context)) {
                    g.expiryTV.setTextColor(ContextCompat.getColor(holder.context, R.color.primaryColor));
                } else {
                    //get the default color
                    int color = ColorFactory.getAttrColor(holder.context, android.R.attr.textColor);
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
     * Gets stock threshold.
     *
     * @param context the context
     * @return the stock threshold
     */
    private int getStockThreshold(Context context) {
        int defaultRedThreshold = context.getResources().getInteger(R.integer.default_red_stock_threshold);
        String stockThresholdString = SharedPreferencesFactory.getSP(context)
                .getString(context.getString(R.string.sp_red_stock_threshold), null);
        return Converters.getIntFromStringSharedPreferences(context, stockThresholdString, defaultRedThreshold);
    }

    /**
     * Gets warning day threshold.
     *
     * @param context the context
     * @return the warning day threshold
     */
    private int getWarningDayThreshold(Context context) {
        int defaultWarningDayThreshold = context.getResources().getInteger(R.integer.default_warning_day_threshold);
        String warningDayThresholdString = SharedPreferencesFactory.getSP(context)
                .getString(context.getString(R.string.sp_warning_day_threshold), null);
        return Converters.getIntFromStringSharedPreferences(context, warningDayThresholdString, defaultWarningDayThreshold);
    }

    /**
     * Gets red day threshold.
     *
     * @param context the context
     * @return the red day threshold
     */
    private int getRedDayThreshold(Context context) {
        int defaultRedDayThreshold = context.getResources().getInteger(R.integer.default_red_day_threshold);
        String redDayThresholdString = SharedPreferencesFactory.getSP(context)
                .getString(context.getString(R.string.sp_red_day_threshold), null);
        return Converters.getIntFromStringSharedPreferences(context, redDayThresholdString, defaultRedDayThreshold);
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
