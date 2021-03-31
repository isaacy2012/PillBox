package com.innerCat.pillBox.widgets;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;

import com.innerCat.pillBox.R;
import com.innerCat.pillBox.factories.ItemDatabaseFactory;
import com.innerCat.pillBox.room.ItemDatabase;


/**
 * Implementation of App Widget functionality.
 */
public class HomeWidgetProvider extends AppWidgetProvider {

    private static ItemDatabase itemDatabase;

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
//            Intent intent = new Intent(context, MainActivity.class);
//            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
//
//            // Get the layout for the App Widget and attach an on-click listener
//            views.setPendingIntentTemplate(R.id.widgetListView, pendingIntent);
            appWidgetManager.updateAppWidget(appWidgetId, views);
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widgetGridView);
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
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