package com.innerCat.pillBox.recyclerViews;

import android.animation.ValueAnimator;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.innerCat.pillBox.R;
import com.innerCat.pillBox.activities.FormActivity;
import com.innerCat.pillBox.objects.ColorItem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Create the basic adapter extending from RecyclerView.Adapter
// Note that we specify the custom ViewHolder which gives us access to our views
public class ColorAdapter extends
        RecyclerView.Adapter<ColorAdapter.ViewHolder> {

    private final float UNSELECTED_ALPHA = 0.3f;
    private final float SELECTED_ALPHA = 1f;
    private final int ANIMATION_LENGTH = 150;

    private final List<ColorItem> colors;
    private final Set<ViewHolder> mBoundViewHolders = new HashSet<>();
    private int selectedColor = ColorItem.NO_COLOR;

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public Button colorButton;
        public Context context;
        public ColorItem colorItem;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder( View itemView, Context context ) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);
            this.context = context;


            colorButton = itemView.findViewById(R.id.colorButton);
            colorButton.setOnClickListener(v -> {
                colorItem.toggleSelected();
                if (colorItem.isSelected()) {
                    deselectAllExcept(colorItem);
                    colorButton.setAlpha(1f);
                    ((FormActivity) context).setSelectedColor(colorItem.getColor());
                } else {
                    deselectAll();
                    ((FormActivity) context).setSelectedColor(ColorItem.NO_COLOR);
                }
            });

        }


    }

    /**
     * Animate color button.
     *
     * @param fromAlpha the from alpha
     * @param toAlpha   the to alpha
     */
    public void animateColorButton(float fromAlpha, float toAlpha, Button colorButton) {
        ValueAnimator va = ValueAnimator.ofFloat(fromAlpha, toAlpha);
        va.addUpdateListener(valueAnimator -> colorButton.setAlpha((Float) valueAnimator.getAnimatedValue()));
        va.setDuration(ANIMATION_LENGTH);
        va.start();
    }

    /**
     * Deselect all except.
     */
    public void deselectAllExcept(ColorItem exceptColorItem) {
        for (ViewHolder viewHolder : mBoundViewHolders) {
            if (viewHolder.colorItem.equals(exceptColorItem) == false) {
                viewHolder.colorItem.setSelected(false);
                float currentAlpha = viewHolder.colorButton.getAlpha();
                animateColorButton(currentAlpha, UNSELECTED_ALPHA, viewHolder.colorButton);
            }
        }
    }

    /**
     * Deselect all.
     */
    public void deselectAll() {
        for (ViewHolder viewHolder : mBoundViewHolders) {
            viewHolder.colorItem.setSelected(false);
            float currentAlpha = viewHolder.colorButton.getAlpha();
            animateColorButton(currentAlpha, SELECTED_ALPHA, viewHolder.colorButton);
        }
    }

    /**
     * Select color.
     *
     * @param selectedColor the selected color
     */
    public void setSelectedColor(int selectedColor) {
        this.selectedColor = selectedColor;
    }

    /**
     * Instantiates a new Color adapter.
     */
    public ColorAdapter() {
        colors = new ArrayList<>();
        colors.add(new ColorItem("#b71c1c")); //red
        colors.add(new ColorItem("#d81b60")); //pink
        colors.add(new ColorItem("#1e88e5")); //blue
        colors.add(new ColorItem("#00acc1")); //turquoise
        colors.add(new ColorItem("#43a047")); //green
        colors.add(new ColorItem("#ffb300")); //yellow-orange
        colors.add(new ColorItem("#f4511e")); //orange
        colors.add(new ColorItem("#000000")); //black
        colors.add(new ColorItem("#9e9e9e")); //gray
    }



    // Usually involves inflating a layout from XML and returning the holder
    @NonNull
    @Override
    public ColorAdapter.ViewHolder onCreateViewHolder( ViewGroup parent, int viewType ) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View itemView = inflater.inflate(R.layout.color_rv_item, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(itemView, context);
        return viewHolder;
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder( ColorAdapter.ViewHolder holder, int position ) {
        // Get the data model based on position
        holder.colorItem = colors.get(position);
        Button colorButton = holder.colorButton;

        // Set item views based on your views and data model
        colorButton.setBackgroundColor(holder.colorItem.getColor());
        if (holder.colorItem.getColor() == selectedColor) {
            holder.colorItem.setSelected(true);
            holder.colorButton.setAlpha(SELECTED_ALPHA);
        } else if (selectedColor != ColorItem.NO_COLOR) {
            holder.colorButton.setAlpha(UNSELECTED_ALPHA);
        }

        mBoundViewHolders.add(holder);
    }



    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return colors.size();
    }
}
