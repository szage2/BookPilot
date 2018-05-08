package com.example.szage.bookpilot.ui;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.example.szage.bookpilot.BookCursorAdapter;
import com.example.szage.bookpilot.R;
import com.example.szage.bookpilot.data.BookContract;

/**
 *
 */
public class UnreadListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int ID_BOOK_DATABASE_LOADER = 24;
    private BookCursorAdapter mCursorAdapter;

    public UnreadListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_unread_list, container, false);

        // Get the GridView
        GridView unreadGridView = rootView.findViewById(R.id.unread_grid_view);

        // Create final variable "category"
        final int category = 2;

        // Get the Cursor Adapter
        mCursorAdapter = new BookCursorAdapter(getActivity(), null);
        // Set the adapter on the Grid View
        unreadGridView.setAdapter(mCursorAdapter);

        // Set Item Click Listener on the grid view
        unreadGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long id) {
                // Create an intent
                Intent detailIntent = new Intent(getActivity(), DetailActivity.class);
                // Attach the id to the intent as extra data
                detailIntent.putExtra(String.valueOf(R.string.id), id);
                // Get the selected book's uri
                Uri currentBookUri = ContentUris.withAppendedId(BookContract.BookEntry.CONTENT_URI, id);
                // Attach extra data to the intent: category and the book uri
                detailIntent.putExtra(String.valueOf(R.string.category), category);
                detailIntent.setData(currentBookUri);
                // Start detail Activity
                startActivity(detailIntent);
            }
        });
        // Make the loader work
        getActivity().getSupportLoaderManager().initLoader(ID_BOOK_DATABASE_LOADER, null, this);

        return rootView;
    }

    /**
     * Create the loader
     *
     * @param loaderId is the unique id that identifies the loader
     * @param args holds the arguments
     * @return Cursor Loader
     */
    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        switch (loaderId) {
            case ID_BOOK_DATABASE_LOADER :
                Uri bookQueryUri = BookContract.BookEntry.CONTENT_URI;
                // Defining projection that contains all columns
                String[] projection = {
                        BookContract.BookEntry._ID,
                        BookContract.BookEntry.COLUMN_TITLE,
                        BookContract.BookEntry.COLUMN_AUTHORS,
                        BookContract.BookEntry.COLUMN_BOOK_COVER_PATH,
                        BookContract.BookEntry.COLUMN_DESCRIPTION,
                        BookContract.BookEntry.COLUMN_ISBN_NUMBER,
                        BookContract.BookEntry.COLUMN_CATEGORY_ID,
                        BookContract.BookEntry.COLUMN_RATING
                };
                // Criteria to filter category 2 in the database
                String selection = BookContract.BookEntry.COLUMN_CATEGORY_ID + "=?";
                String[] selectionArgs = {"2"};
                // Loader executing provider's query
                return new CursorLoader(
                        getActivity(),
                        bookQueryUri,
                        projection,
                        selection,
                        selectionArgs,
                        null);
            default:
                throw new RuntimeException(String.valueOf(R.string.loader_error + loaderId));
        }
    }

    /**
     * Set the cursor on the right position
     *
     * @param loader is the Cursor Loader
     * @param cursor holds the data
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mCursorAdapter.swapCursor(cursor);
    }

    /**
     * Reset the Cursor
     *
     * @param loader is the Cursor Loader
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }
}
