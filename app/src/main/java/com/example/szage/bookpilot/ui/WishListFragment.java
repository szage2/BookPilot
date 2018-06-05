package com.example.szage.bookpilot.ui;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.Loader;
import android.util.Log;
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
public class WishListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int ID_BOOK_DATABASE_LOADER = 23;
    private BookCursorAdapter mCursorAdapter;
    private long firstItemsId;
    private boolean isTwoPanes;
    private FloatingActionButton shareFab;
    private String books = "";

    public WishListFragment() {
        // Required empty public constructor
    }

    // Define a new interface that triggers a callback in the host activity
    public OnNewItemSelectedListener mListener;

    /**
     * Interface that listens for item clicks in the list view,
     * passes the position, uri and itemId of first book in the list to activity.
     */
    public interface OnNewItemSelectedListener {
        void OnNewItemClicked(int position, Uri bookUri, long itemId);
    }

    public static WishListFragment newInstance(OnNewItemSelectedListener listener) {
        WishListFragment wishListFragment = new WishListFragment();
        wishListFragment.mListener = listener;
        return wishListFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_wish_list, container, false);

        // Get the GridView
        GridView wishGridView = rootView.findViewById(R.id.wish_grid_view);

        // Get the share button
        shareFab = rootView.findViewById(R.id.fab);

        // Detect the type of device: phone or tablet
        isTwoPanes = getResources().getBoolean(R.bool.has_two_panes);

        // In tablet mode
        if (isTwoPanes) {
            // Hide share button
            shareFab.setVisibility(View.GONE);
        }

        // Create final variable "category"
        final int category = 1;

        // Get the Cursor Adapter
        mCursorAdapter = new BookCursorAdapter(getActivity(), null);
        // Set the adapter on the Grid View
        wishGridView.setAdapter(mCursorAdapter);

        // Set Item Click Listener on the grid view
        wishGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                // Get the selected book's uri
                Uri currentBookUri = ContentUris.withAppendedId(BookContract.BookEntry.CONTENT_URI, id);

                // In case the first item is being selected, get the id of the second item
                // If the first item will be replaced of the category, that will be the first one.
                if (id == firstItemsId) {
                    firstItemsId = mCursorAdapter.getItemId(1);
                }

                if (!isTwoPanes) {
                    // if user run the app on a phone
                    // Create an intent
                    Intent detailIntent = new Intent(getActivity(), DetailActivity.class);
                    // Attach the id to the intent as extra data
                    detailIntent.putExtra(String.valueOf(R.string.id), id);
                    // Attach extra data to the intent: category and the book uri
                    detailIntent.putExtra(String.valueOf(R.string.category), category);
                    detailIntent.setData(currentBookUri);
                    // Start detail Activity
                    startActivity(detailIntent);
                } else {
                    // In In tablet layout
                    // Pass selected item's position to the listener
                    mListener.OnNewItemClicked(position, currentBookUri, firstItemsId);
                }
            }
        });
        // Make the loader work
        getLoaderManager().initLoader(ID_BOOK_DATABASE_LOADER, null, this);

        return rootView;
    }

    /**
     * Create the loader
     *
     * @param loaderId is the unique id that identifies the loader
     * @param bundle holds the arguments
     * @return Cursor Loader
     */
    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
        switch (loaderId) {
            case ID_BOOK_DATABASE_LOADER:
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
                // Criteria to filter category 1 in the database
                String selection = BookContract.BookEntry.COLUMN_CATEGORY_ID + "=?";
                String[] selectionArgs = {"1"};
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
     * Update variables according to cursor position
     *
     * @param loader is the Cursor Loader
     * @param cursor holds the data
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Set the cursor on the right position
        mCursorAdapter.swapCursor(cursor);
        // Get the item id of first row in the database
        firstItemsId = mCursorAdapter.getItemId(0);
        // Create the uri for that book, using item's id
        Uri currentBookUri = ContentUris.withAppendedId(BookContract.BookEntry.CONTENT_URI, firstItemsId);
        // Call method that make sure to load appropriate book in Detail Activity
        mListener.OnNewItemClicked(0, currentBookUri, firstItemsId);

        // If the cursor holds any value
        if (mCursorAdapter.getCount() != 0) {
            // Loop through cursor data
            for (int i = 0; i < mCursorAdapter.getCount(); i++) {

                // Get the column Indexes
                int titleColumnIndex = cursor.getColumnIndexOrThrow(BookContract.BookEntry.COLUMN_TITLE);
                int authorsColumnIndex = cursor.getColumnIndexOrThrow(BookContract.BookEntry.COLUMN_AUTHORS);
                // Get Book details from the cursor
                String title = cursor.getString(titleColumnIndex);
                String authors = cursor.getString(authorsColumnIndex);

                // Create a String of authors and title of book
                // and a line break
                String book = authors + ": " + title + "\n";
                // Add the book string to books string
                books = books + book;
                Log.i("WishListFragment", "books is " + books);
                // Move the cursor to next book
                cursor.moveToNext();
            }
        }

        // Set click listener on share button
        shareFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Call share method
                shareWishList(books);
            }
        });
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

    /**
     * Attach WishListFragment to context
     *
     * @param context is the activity context
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            // Parse the context
            mListener = (OnNewItemSelectedListener) context;
        } catch (ClassCastException e) {
            // Catch the exception if parsing didn't work
            throw new ClassCastException(context.toString());
        }
    }

    /**
     * Enables to share Wish List
     *
     * @param shareBody
     */
    private void shareWishList(String shareBody) {
        // Create an intent
        Intent shareIntent = new Intent();
        // set send action on it
        shareIntent.setAction(Intent.ACTION_SEND);
        // Set the type
        shareIntent.setType("text/plain");
        // put subject and body as extra data
        shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "My desired books");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        // Start the activity
        getActivity().startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.wish_list)));
    }
}
