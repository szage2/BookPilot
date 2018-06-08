package com.example.szage.bookpilot.widget;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.szage.bookpilot.R;
import com.example.szage.bookpilot.model.Book;

import java.util.ArrayList;

/**
 * Created by szage on 2018. 06. 05..
 */

public class BookWidgetProvider extends AppWidgetProvider {


    private ArrayList<Book> bookArrayList = new ArrayList<>();
    public static final String ACTION_UPDATE = "android.appwidget.action.APPWIDGET_UPDATE";
    RemoteViews remoteViews;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        // If the list holds valid items
        if (bookArrayList != null && !bookArrayList.isEmpty()) {
            // Loop through each App Widget that belongs to this provider
            for (int appWidgetId : appWidgetIds) {
                // Create intent that starts the RecipeWidgetService
                Intent intent = new Intent(context, BookWidgetService.class);
                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                // When intents are compared, the extras are ignored, so need to embed the extras
                // into the data so that the extras will not be ignored
                intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
                // Instantiate the RemoteViews object for widget layout
                remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_book_list);

                // Setup the collection
                setRemoteAdapter(context, remoteViews, intent);

                // Set label of the widget
                remoteViews.setTextViewText(R.id.widget_label, "My wish list");
                // Otherwise display text as empty view
                remoteViews.setEmptyView(R.id.book_widget_list, R.id.widget_list_empty);

                // notify app widget manager to perform an update on current app widget
                appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
            }
            super.onUpdate(context, appWidgetManager, appWidgetIds);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        // Get an instance of  App Widget Manager
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context.getApplicationContext());
        // Get the App's Widget component
        ComponentName name = new ComponentName(context.getApplicationContext(), BookWidgetProvider.class);
        // Get the  App Widget Ids
        int appWidgetIds[] =appWidgetManager.getAppWidgetIds(name);

        // Get the action
        String action = intent.getAction();
        // if the action is ACTION_APPWIDGET_UPDATE
        if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(action)) {
            // Get the extras of the intent
            Bundle bundle = intent.getExtras();
            // In case that extras are not null
            if (bundle != null) {
                // Get the list of books
                bookArrayList = bundle.getParcelableArrayList("bookList");

                Log.i("BookWidgetProvider", "book list in onReceive is " +bookArrayList);
            }

            appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.book_widget_list);

            // Check that the appWidgetIds is valid
            if (appWidgetIds != null && appWidgetIds.length != 0) {
                // Make sure to update the widget every time a book gets added
                this.onUpdate(context, appWidgetManager, appWidgetIds);
            }

        }

    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void setRemoteAdapter(final Context context, @NonNull final RemoteViews views, Intent intent) {
        views.setRemoteAdapter(R.id.book_widget_list,
                intent);
    }

    @SuppressWarnings("deprecation")
    private void setRemoteAdapterV11(Context context, @NonNull final RemoteViews views, Intent intent) {
        views.setRemoteAdapter(0, R.id.book_widget_list,
                intent);
    }
}
