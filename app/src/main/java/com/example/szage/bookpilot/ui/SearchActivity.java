package com.example.szage.bookpilot.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.szage.bookpilot.BookAdapter;
import com.example.szage.bookpilot.R;

public class SearchActivity extends AppCompatActivity {

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

        // make invisible this Text View as default
        resultTextView.setVisibility(View.GONE);
        // Set a click listener on the search image
        searchImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Call method that handel onClick
              searchGetPressed(resultTextView, searchField);
            }
        });
    }

    /**
     * Make header and search result appear in case the user typed valid query
     *
     * @param header is the header for search results
     * @param field is the search field Edit Text where the user typed a query
     */
    private void searchGetPressed(TextView header, TextView field) {

        // Get the text from Edit Text
        String searchQuery = field.getText().toString();

        // If the Edit had no value
        if (searchQuery.isEmpty()) {
            // Notify the user with a Toast message
            Toast.makeText(SearchActivity.this, "Enter valid query",
                    Toast.LENGTH_SHORT).show();
        } else {
            // when it gets clicked set make it appear.
            header.setVisibility(View.VISIBLE);
            // Call method that creates the list of Books
            setUpResultList();
        }
    }

    private void setUpResultList() {
        // Get the Search Recycler View
        RecyclerView searchRecyclerView = findViewById(R.id.search_recycler_view);
        // Set the Grid Layout Manager on it with 2 spans
        searchRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        // Call the Book Adapter's constructor
        BookAdapter bookAdapter = new BookAdapter(this);
        // Set the adapter on Recycler View
        searchRecyclerView.setAdapter(bookAdapter);
    }
}
