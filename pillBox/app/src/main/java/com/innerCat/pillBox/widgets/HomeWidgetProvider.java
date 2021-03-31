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

    private static ItemDatabase itemDatabase;
    private final String DECREMENT = "decrement";

    /**
     * Broadcast an update to all the widgets
     * @param context the context from which the update should be broadcast
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

    /**
     * Internal update widgets
     * @param context the context from which to update
     * @param appWidgetManager the appWidgetManager
     * @param appWidgetId the appWidgetId
     */
    static void updateAppWidget( Context context, AppWidgetManager appWidgetManager,
                                 int appWidgetId ) {

        // Get the layout for the App Widget and attach an on-click listener
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.home_widget);
        setRemoteAdapter(context, views);

        if (itemDatabase == null) {
            initDatabase(context);
        }

        appWidgetManager.updateAppWidget(appWidgetId, views);

    }


    @Override
    public void onUpdate( Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds ) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.home_widget);

            Intent postIntent = new Intent(context, HomeWidgetProvider.class);
            postIntent.setAction(DECREMENT);
            PendingIntent postPendingIntent = PendingIntent.getBroadcast(context,
                    0, postIntent, 0);
            views.setPendingIntentTemplate(R.id.widgetGridView, postPendingIntent );

            appWidgetManager.updateAppWidget(appWidgetId, views);
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widgetGridView);

            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(DECREMENT)){

            //Passed info from WidgetService.java
            int id = intent.getIntExtra("id", -1);
            int position = intent.getIntExtra("pos", -1);

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
                    // Notify the adapter that an item was changed at position
                    HomeWidgetProvider.broadcastUpdate(context);
                });
            });


            //Show information
            System.out.println("ID: " + id + ", POS: " + position);
            Toast.makeText(context,
                    "ID: " + id + ", POS: " + position,
                    Toast.LENGTH_LONG).show();
        }
        super.onReceive(context, intent);
    }

    @Override
    public void onEnabled( Context context ) {
        // Enter relevant functionality for when the first widget is created
        initDatabase(context);
    }

    public static void initDatabase( Context context ) {
        //initialise the database
        itemDatabase = ItemDatabaseFactory.getItemDatabase(context);
    }

    @Override
    public void onDisabled( Context context ) {
        // Enter relevant functionality for when the last widget is disabled
        itemDatabase = null;
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