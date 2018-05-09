package com.example.szage.bookpilot.ui;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.szage.bookpilot.BookAdapter;
import com.example.szage.bookpilot.BookSearchResultLoader;
import com.example.szage.bookpilot.QueryUtils;
import com.example.szage.bookpilot.R;
import com.example.szage.bookpilot.model.Book;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<ArrayList<Book>>{

    // Base Url for searching certain books (google books api)
    String mBaseUrl = "https://www.googleapis.com/books/v1/volumes?q=";
    // This variable's going to get the value of the google books api + the search query
    private String mQueryUrl = "";

    // Constant value for the book loader ID.
    private static final int BOOK_LOADER_ID = 13;
    private BookAdapter mBookAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Get the Edit Text
        final EditText searchField = findViewById(R.id.search_field);
        // Get the Image View
        ImageView searchImage = findViewById(R.id.search_btn);
        // Get the Text View that informs about the results
        final TextView resultTextView = findViewById(R.id.search_result);

        // Get the intent that launched the activity
        Intent launcherIntent = getIntent();
        String barcode;
        // Get extra data from it
        Bundle extra = launcherIntent.getExtras();
        // If activity got extra data
        if (extra != null) {
            // Get the barcode
            barcode = extra.getString(String.valueOf(R.string.barcode));
            Log.i("SearchActivity", "barcode is " + barcode);
            // Set the barcode on the edit text as query
            searchField.setText(barcode);
        }

        // make invisible this Text View as default
        resultTextView.setVisibility(View.GONE);
        // Set a click listener on the search image
        searchImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Call method that handel onClick
              searchGetPressed(resultTextView, searchField);
              searchField.setText(null);
            }
        });

        // Get the network info by method call
        NetworkInfo networkInfo = QueryUtils.getNetworkInfo(this);

        // If there's connection, get the loader
        if (networkInfo != null && networkInfo.isConnected()) {
            LoaderManager loaderManager = getLoaderManager();
            // Initialize loader
            loaderManager.initLoader(BOOK_LOADER_ID, null, this);
        } else resultTextView.setText(R.string.connectivity_error);
    }

    /**
     * Make header and search result appear in case the user typed valid query
     *
     * @param header is the header for search results
     * @param field is the search field Edit Text where the user typed a query
     */
    private void searchGetPressed(TextView header, TextView field) {

        // Get the text from Edit Text and replace spaces and "+"
        String searchQuery = field.getText().toString().replaceAll(" ", "+");

        // If the Edit had no value
        if (searchQuery.isEmpty()) {
            // Notify the user with a Toast message
            Toast.makeText(SearchActivity.this, R.string.search_empty,
                    Toast.LENGTH_SHORT).show();
        } else {
            // when it gets clicked set make it appear.
            header.setVisibility(View.VISIBLE);

            // Add the search query to the base url and reduce the nr of results to be 20
            mQueryUrl = mBaseUrl + searchQuery + "&maxResults=20";

            // Restart LoaderManager
            getLoaderManager().restartLoader(BOOK_LOADER_ID, null, SearchActivity.this);

            // Make query variable ready for the next search (reset)
            mQueryUrl = "";
        }
    }

    /**
     * Display the result of the search
     *
     * @param books is a list of Book Objects
     */
    private void setUpResultList(ArrayList<Book> books) {
        // Category of the list - 0 means Search result(s)
        int category = 0;
        // Get the Search Recycler View
        RecyclerView searchRecyclerView = findViewById(R.id.search_recycler_view);
        // Set the Grid Layout Manager on it with 2 spans
        searchRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        // Call the Book Adapter's constructor
        mBookAdapter = new BookAdapter(this, books, category);
        // Set the adapter on Recycler View
        searchRecyclerView.setAdapter(mBookAdapter);
    }

    /**
     * Creating the loader
     *
     * @param i is the unique id of the loader
     * @param bundle is empty argument
     * @return List of Book objects
     */
    @Override
    public Loader<ArrayList<Book>> onCreateLoader(int i, Bundle bundle) {
        // Create a new loader for the new query
        return new BookSearchResultLoader(this, mQueryUrl);
    }

    /**
     * Display search result when loading is finished
     *
     * @param loader is responsible for notifying changes in data
     * @param books is the list of Book objects
     */
    @Override
    public void onLoadFinished(Loader<ArrayList<Book>> loader, ArrayList<Book> books) {
        // If the book list has valid value
        if (books != null && !books.isEmpty()) {
            // Call method that gets views and displays it with adapter
            setUpResultList(books);
        }
    }

    /**
     * Update the adapter
     *
     * @param loader  holds the data
     */
    @Override
    public void onLoaderReset(Loader<ArrayList<Book>> loader) {
        mBookAdapter.notifyDataSetChanged();
    }
}
