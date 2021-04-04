package com.innerCat.pillBox.widgets;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import androidx.core.content.ContextCompat;

import com.innerCat.pillBox.Item;
import com.innerCat.pillBox.R;
import com.innerCat.pillBox.StringFormatter;
import com.innerCat.pillBox.factories.DatabaseFactory;
import com.innerCat.pillBox.room.Database;

import java.util.ArrayList;
import java.util.List;


public class DataProvider implements RemoteViewsService.RemoteViewsFactory {

    List<Item> items = new ArrayList<>();
    Database database;
    Context context;

    public DataProvider(Context context, Intent intent) {
        this.context = context;
    }

    @Override
    public void onCreate() {
        database = DatabaseFactory.create(context);
    }

    /**
     * When the dataset is changed
     */
    @Override
    public void onDataSetChanged() {
        items = database.getDao().getAllWidgetItems();
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews widgetGridViewHolder = new RemoteViews(context.getPackageName(),
                R.layout.widget_grid_item);
        Item thisItem = items.get(position);
        widgetGridViewHolder.setTextViewText(R.id.widgetNameTV, thisItem.getName());
        int stock = thisItem.getStock();
        if (stock < 10) {
            SpannableString redStockText = new SpannableString(String.valueOf(thisItem.getStock()));
            redStockText.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.primaryColor)), 0, redStockText.length(), 0);
            widgetGridViewHolder.setTextViewText(R.id.widgetStockTV, redStockText);
        } else {
            widgetGridViewHolder.setTextViewText(R.id.widgetStockTV, Integer.toString(thisItem.getStock()));
        }
        widgetGridViewHolder.setTextViewText(R.id.widgetLastTakenTV, StringFormatter.getLastTakenText(thisItem));

        // Create an Intent to launch update the item by sending the id
        Bundle extras = new Bundle();
        extras.putInt("id", thisItem.getId());
        Intent intent = new Intent();
        intent.putExtras(extras);
        widgetGridViewHolder.setOnClickFillInIntent(R.id.widgetRelativeLayout, intent);

        return widgetGridViewHolder;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

}