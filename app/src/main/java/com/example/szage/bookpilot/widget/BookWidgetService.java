package com.example.szage.bookpilot.widget;

import android.app.Notification;
import android.appwidget.AppWidgetManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.AppWidgetTarget;
import com.bumptech.glide.request.target.NotificationTarget;
import com.bumptech.glide.request.target.ViewTarget;
import com.example.szage.bookpilot.R;
import com.example.szage.bookpilot.data.BookContract;


/**
 * Created by szage on 2018. 06. 05..
 */

public class BookWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new BookRemoteViewsFactory(intent, this.getApplicationContext());
    }
}

class BookRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private Context mContext;
    private Cursor cursor;

    // Constructor
    public BookRemoteViewsFactory(Intent intent, Context applicationContext) {
        mContext = applicationContext;
        Log.i("BookWidgetService", "is working");
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        Log.i("BookWidgetService", "onDataSetChanged");
        // Get the uri
        Uri uri = BookContract.BookEntry.CONTENT_URI;

        // Select id, title, authors
        String[] projection = {
                BookContract.BookEntry._ID,
                BookContract.BookEntry.COLUMN_TITLE,
                BookContract.BookEntry.COLUMN_AUTHORS
        };

        // Criteria to filter category 1 in the database
        String selection = BookContract.BookEntry.COLUMN_CATEGORY_ID + "=?";
        String[] selectionArgs = {"1"};

        // Get the content resolver
        ContentResolver cr = mContext.getContentResolver();
        // Check the android version of device
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            // Query for wish list
            cursor = cr.query(
                    uri,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null);

        }
    }

    @Override
    public void onDestroy() {
        // In case cursor is not null
        if (cursor != null) {
            // close it
            cursor.close();
        }
    }

    @Override
    public int getCount() {
        // In case cursor is not null
       if (cursor != null) {
           // return the number of cursor contents
           return cursor.getCount();
           // Otherwise return 0
       } else return 0;
    }

    @Override
    public RemoteViews getViewAt(int i) {
        // Move the cursor to current position
        cursor.moveToPosition(i);

        // Check that position is valid, cursor is not null and it's set to the right position
        if (i == AdapterView.INVALID_POSITION || cursor == null || !cursor.moveToPosition(i)) {
            // If not, return null
            return null;
        }
        // Instantiate Remote View
        RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.widget_book_item);

        // Get the title's, authors' book cover's column index
        int titleColumnIndex = cursor.getColumnIndexOrThrow(BookContract.BookEntry.COLUMN_TITLE);
        int authorsColumnIndex = cursor.getColumnIndexOrThrow(BookContract.BookEntry.COLUMN_AUTHORS);

        // Get title and authors by column index
        String title = cursor.getString(titleColumnIndex);
        String authors = cursor.getString(authorsColumnIndex);

        // Set these values to matching views
        remoteViews.setTextViewText(R.id.widget_title, title);
        remoteViews.setTextViewText(R.id.widget_author, authors);

        // return the remote views
        return remoteViews;
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
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }
}
