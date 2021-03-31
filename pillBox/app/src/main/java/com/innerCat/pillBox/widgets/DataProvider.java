package com.innerCat.pillBox.widgets;

import android.content.Context;
import android.content.Intent;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import androidx.core.content.ContextCompat;

import com.innerCat.pillBox.Item;
import com.innerCat.pillBox.R;
import com.innerCat.pillBox.StringFormatter;
import com.innerCat.pillBox.factories.ItemDatabaseFactory;
import com.innerCat.pillBox.room.ItemDatabase;

import java.util.ArrayList;
import java.util.List;


public class DataProvider implements RemoteViewsService.RemoteViewsFactory {

    List<Item> items = new ArrayList<>();
    ItemDatabase itemDatabase;
    Context context;

    public DataProvider(Context context, Intent intent) {
        this.context = context;
    }

    @Override
    public void onCreate() {
        itemDatabase = ItemDatabaseFactory.getItemDatabase(context);
    }

    /**
     * When the dataset is changed
     */
    @Override
    public void onDataSetChanged() {
        items = itemDatabase.itemDao().getAllItems();
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
                R.layout.grid_view_item_widget);
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
        Intent intent = new Intent();
        intent.putExtra("id", thisItem.getId());
        widgetGridViewHolder.setOnClickFillInIntent(R.id.widgetRelativeLayout, intent);
//        widgetGridViewHolder.setOnClickFillInIntent(R.id.listItemWidgetTextView, intent);

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