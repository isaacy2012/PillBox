package com.innerCat.pillBox.recyclerViews;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.innerCat.pillBox.R;
import com.innerCat.pillBox.Refill;
import com.innerCat.pillBox.StringFormatter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Create the basic adapter extending from RecyclerView.Adapter
// Note that we specify the custom ViewHolder which gives us access to our views
public class RefillAdapter extends
        RecyclerView.Adapter<RefillAdapter.ViewHolder> {

    private List<Refill> refills;
    private Set<ViewHolder> mBoundViewHolders = new HashSet<>();

    // Provide a direct reference to each of the views within a data refill
    // Used to cache the views within the refill layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public Refill refill;
        public TextView amountTV;
        public TextView dateTV;
        public Context context;

        // We also create a constructor that accepts the entire refill row
        // and does the view lookups to find each subview
        public ViewHolder( View refillView, Context context ) {
            // Stores the refillView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(refillView);
            this.context = context;


            amountTV = refillView.findViewById(R.id.amountTV);
            dateTV = refillView.findViewById(R.id.dateTV);

        }
    }

    /**
     * Pass in the tasks array into the Adapter
     *
     * @param refills the list of Tasks
     */
    public RefillAdapter( List<Refill> refills ) {
        this.refills = refills;
    }


    /**
     * Add a refill
     *
     * @param context  the context
     * @param refill     the Refill to add
     * @param position the position of the new Refill in the List
     */
    public void addRefill( Context context, Refill refill, int position) {
        refills.add(position, refill);
        notifyInserted(context, position);
    }

    /**
     * Remove an refill.
     *
     * @param context  the context
     * @param refill     the refill to remove
     * @param position the position of the Refill in the List
     */
    public void removeRefill( Context context, Refill refill, int position) {
        refills.remove(refill);
        notifyRemoved( context, position );
    }

    /**
     * Notify that an refill has been changed. Calls the super method notifyItemChanged
     *
     * @param context  the context
     * @param position the position
     */
    public void notifyChanged( Context context, int position ) {
        super.notifyItemChanged( position );
    }

    /**
     * Notify that an refill has been inserted. Calls the super method notifyItemInserted
     *
     * @param context  the context
     * @param position the position
     */
    public void notifyInserted( Context context, int position ) {
        super.notifyItemInserted(position);
    }

    /**
     * Notify that an refill has been removed. Calls the super method notifyItemRemoved
     *
     * @param context  the context
     * @param position the position
     */
    public void notifyRemoved( Context context, int position ) {
        super.notifyItemRemoved(position);
    }

    // Usually involves inflating a layout from XML and returning the holder
    @NonNull
    @Override
    public RefillAdapter.ViewHolder onCreateViewHolder( ViewGroup parent, int viewType ) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View refillView = inflater.inflate(R.layout.refill_rv_item, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(refillView, context);
        return viewHolder;
    }

    // Involves populating data into the refill through holder
    @Override
    public void onBindViewHolder( RefillAdapter.ViewHolder holder, int position ) {
        // Get the data model based on position
        holder.refill = refills.get(position);

        // Set refill views based on your views and data model
        TextView amountTV = holder.amountTV;
        TextView dateTV = holder.dateTV;
        amountTV.setText(String.valueOf(holder.refill.getAmount()));
        dateTV.setText(StringFormatter.dateToString(holder.refill.getExpiryDate()));

        mBoundViewHolders.add(holder);
    }


    // Returns the total count of refills in the list
    @Override
    public int getItemCount() {
        return refills.size();
    }

    /**
     * Gets refills.
     *
     * @return the refills
     */
    public List<Refill> getRefills() {
        return this.refills;
    }

    /**
     * Sets refills.
     *
     * @param refills the refills
     */
    public void setRefills( List<Refill> refills ) {
        this.refills = refills;
    }
}
