package com.innerCat.pillBox.recyclerViews;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.innerCat.pillBox.R;
import com.innerCat.pillBox.StringFormatter;
import com.innerCat.pillBox.activities.RefillActivity;
import com.innerCat.pillBox.objects.Refill;
import com.innerCat.pillBox.objects.RefillListHeader;
import com.innerCat.pillBox.objects.RefillListObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Create the basic adapter extending from RecyclerView.Adapter
// Note that we specify the custom ViewHolder which gives us access to our views
public class RefillAdapter extends
        RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private List<RefillListObject> refillListObjects = new ArrayList<>();
    private Set<ViewHolderItem> mBoundItemViewHolders = new HashSet<>();

    // Provide a direct reference to each of the views within a data refill
    // Used to cache the views within the refill layout for fast access
    public class ViewHolderItem extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public Refill refill;
        public TextView amountTV;
        public TextView dateTV;
        public Context context;
        public CheckBox deleteCheckBox;

        // We also create a constructor that accepts the entire refill row
        // and does the view lookups to find each subview
        public ViewHolderItem( View refillView, Context context ) {
            // Stores the refillView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(refillView);

            this.context = context;


            amountTV = refillView.findViewById(R.id.amountTV);
            dateTV = refillView.findViewById(R.id.dateTV);
            deleteCheckBox = itemView.findViewById(R.id.checkBox);
            deleteCheckBox.setOnClickListener(v -> {
                if (deleteCheckBox.isChecked() == true) {
                    ((RefillActivity) context).addDeleteRefill(refill);
                } else {
                    ((RefillActivity) context).removeDeleteRefill(refill);
                }
            });
        }

        /**
         * Update the state of the checkboxes in the recyclerview wrt the modes in ArchiveActivity
         */
        public void updateState() {
            if (((RefillActivity) context).getEditMode() == true) {
                deleteCheckBox.setVisibility(View.VISIBLE);
                if (((RefillActivity) context).getSelectAllMode() == true) {
                    deleteCheckBox.setChecked(true);
                    ((RefillActivity) context).addDeleteRefill(refill);
                }
            } else {
                deleteCheckBox.setVisibility(View.GONE);
            }
        }
    }
    public class ViewHolderHeader extends RecyclerView.ViewHolder {
        TextView titleTV;
        public ViewHolderHeader( View itemView ) {
            super(itemView);
            this.titleTV = itemView.findViewById(R.id.titleTV);
        }
    }
    /**
     * Enables deletion of all the tasks
     */
    public void checkDelete( boolean deleteMode ) {
        for (ViewHolderItem viewHolderItem : mBoundItemViewHolders) {
            if (deleteMode == true) {
                viewHolderItem.deleteCheckBox.setVisibility(View.VISIBLE);
                viewHolderItem.deleteCheckBox.setChecked(false);
            } else {
                viewHolderItem.deleteCheckBox.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Pass in the refills array into the Adapter
     *
     * @param expired the expired
     * @param future  the future
     */
    public RefillAdapter( List<? extends RefillListObject> expired, List<? extends RefillListObject> future ) {
        refillListObjects.addAll(future);
        if (expired.isEmpty() == false) {
            refillListObjects.add(new RefillListHeader("Expired Refills"));
            refillListObjects.addAll(expired);
        }
    }


    /**
     * Select all the refills in the RecyclerView
     *
     * @param context the context (RefillActivity instance)
     */
    public void selectAll( Context context ) {
        for (ViewHolderItem viewHolderItem : mBoundItemViewHolders) {
            viewHolderItem.deleteCheckBox.setChecked(true);
        }
        for (RefillListObject refillObject : refillListObjects) {
            if (refillObject instanceof Refill) {
                ((RefillActivity) context).addDeleteRefill((Refill)refillObject);
            }
        }
    }

    /**
     * Add a refill
     *
     * @param context  the context
     * @param refill     the Refill to add
     * @param position the position of the new Refill in the List
     */
    public void addRefill( Context context, Refill refill, int position) {
        refillListObjects.add(position, refill);
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
        refillListObjects.remove(refill);
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
    public RecyclerView.ViewHolder onCreateViewHolder( ViewGroup parent, int viewType ) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View refillView;
        if (viewType == TYPE_HEADER) {
            refillView = inflater.inflate(R.layout.refill_rv_header, parent, false);
            // Return a new holder instance
            ViewHolderHeader viewHolderHeader = new ViewHolderHeader(refillView);
            return viewHolderHeader;
        } else {
            refillView = inflater.inflate(R.layout.refill_rv_item, parent, false);
            // Return a new holder instance
            ViewHolderItem viewHolderItem = new ViewHolderItem(refillView, context);
            return viewHolderItem;
        }

    }

    // Involves populating data into the refill through holder
    @Override
    public void onBindViewHolder( RecyclerView.ViewHolder holder, int position ) {
        if (holder instanceof ViewHolderHeader) {
            //if it is a viewHolderHeader
            //cast the object to a Header objects
            RefillListHeader header = (RefillListHeader) refillListObjects.get(position);
            ViewHolderHeader headerViewHolder = (ViewHolderHeader) holder;
            //set the title of the viewHolder to the title of the header object
            headerViewHolder.titleTV.setText(header.getTitle());
        } else if (holder instanceof ViewHolderItem) {
            // Get the data model based on position
            ViewHolderItem refillViewHolder = (ViewHolderItem) holder;
            refillViewHolder.refill = (Refill) refillListObjects.get(position);

            // Set refill views based on your views and data model
            TextView amountTV = refillViewHolder.amountTV;
            TextView dateTV = refillViewHolder.dateTV;
            amountTV.setText(String.valueOf(refillViewHolder.refill.getAmount()));
            dateTV.setText(StringFormatter.dateToString(refillViewHolder.refill.getExpiryDate()));
            mBoundItemViewHolders.add(refillViewHolder);
        }
    }


    // Returns the total count of refills in the list
    @Override
    public int getItemCount() {
        return refillListObjects.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (refillListObjects.get(position) instanceof RefillListHeader) {
            return TYPE_HEADER;
        }
        return TYPE_ITEM;
    }

    /**
     * Gets refills.
     *
     * @return the refills
     */
    public List<RefillListObject> getRefillListObjects() {
        return this.refillListObjects;
    }

}
