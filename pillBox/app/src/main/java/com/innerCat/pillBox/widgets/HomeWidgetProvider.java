package com.innerCat.pillBox.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.innerCat.pillBox.Item;
import com.innerCat.pillBox.R;
import com.innerCat.pillBox.factories.ItemDatabaseFactory;
import com.innerCat.pillBox.room.ItemDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Implementation of App Widget functionality.
 */
public class HomeWidgetProvider extends AppWidgetProvider {

    public final String DECREMENT = "decrement";

    /**
     * Broadcast an update to all the widgets
     * @param context the context from which the update should be broadcast
     *
     */
    public static void broadcastUpdate( Context context ) {
        Intent intent = new Intent(context, HomeWidgetProvider.class);
        //update intent
        intent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
        //ids of widgets
        int[] ids = AppWidgetManager.getInstance(context.getApplicationContext()).getAppWidgetIds(new ComponentName(context.getApplicationContext(), HomeWidgetProvider.class));
        ;
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        context.sendBroadcast(intent);
    }


    @Override
    public void onUpdate( Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds ) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.home_widget);
            setRemoteAdapter(context, views);

            Intent decrementIntent = new Intent(context, HomeWidgetProvider.class);
            decrementIntent.setAction(DECREMENT);
            PendingIntent decrementPendingIntent = PendingIntent.getBroadcast(context,
                    0, decrementIntent, 0);
            views.setPendingIntentTemplate(R.id.widgetGridView, decrementPendingIntent );

            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widgetGridView);
            appWidgetManager.updateAppWidget(appWidgetId, views);

        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(DECREMENT)){

            ItemDatabase itemDatabase = ItemDatabaseFactory.getItemDatabase(context);

            //Passed info from WidgetService.java
            int id = intent.getIntExtra("id", -1);

            //ROOM Threads
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());
            executor.execute(() -> {
                //Background work here
                Item item = itemDatabase.itemDao().getItem(id);
                item.decrementStock();
                itemDatabase.itemDao().update(item);

                handler.post(() -> {
                    //UI Thread work here
                    //notify the widget that there is an update
                    HomeWidgetProvider.broadcastUpdate(context);
                });
            });


        }
        //Show information
        super.onReceive(context, intent);
    }

    @Override
    public void onEnabled( Context context ) {
        // Enter relevant functionality for when the first widget is created
    }


    @Override
    public void onDisabled( Context context ) {
        // Enter relevant functionality for when the last widget is disabled
    }

    /**
     * Set the remote adapter
     * @param context the context from which to create the intent
     * @param views the remoteViews to set the remoteAdapter of
     */
    private static void setRemoteAdapter( Context context, @NonNull final RemoteViews views ) {
        views.setRemoteAdapter(R.id.widgetGridView,
                new Intent(context, WidgetService.class));
    }
}