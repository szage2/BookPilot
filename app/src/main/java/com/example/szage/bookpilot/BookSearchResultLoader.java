package com.example.szage.bookpilot;

import android.content.AsyncTaskLoader;
import android.content.Context;

import com.example.szage.bookpilot.model.Book;

import java.util.ArrayList;

/**
 *  This Loader loads a list of Book objects by using Async Task
 *  that performs the network request to the given url
 */

public class BookSearchResultLoader extends AsyncTaskLoader<ArrayList<Book>> {


    // String query url
    private String mUrl;

    // Constructor
    public BookSearchResultLoader(Context context, String url) {
        super(context);
        mUrl = url;
    }


    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }

    /**
     * Retrieves Book objects
     *
     * @return a list of Book objects
     */
    @Override
    public ArrayList<Book> loadInBackground() {
        if (mUrl == null || mUrl.isEmpty()) {
            return null;
        }

        // Request and extract book data, create a list of Book objects
        ArrayList<Book> books = QueryUtils.fetchBookData(mUrl);
        // Return the list
        return books;
    }
}
