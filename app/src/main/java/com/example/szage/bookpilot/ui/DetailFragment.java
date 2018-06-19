package com.example.szage.bookpilot.ui;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.szage.bookpilot.R;
import com.example.szage.bookpilot.data.BookContract;
import com.example.szage.bookpilot.data.BookDbHelper;
import com.example.szage.bookpilot.model.Book;
import com.example.szage.bookpilot.widget.BookWidgetProvider;

import java.util.ArrayList;

import static com.example.szage.bookpilot.widget.BookWidgetProvider.ACTION_UPDATE;

/**
 *
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();

    private static final int EXISTING_BOOK_LOADER = 23;

    private ArrayList<Book> mBooks;

    private String mTitle, mAuthors, mBookCover, mDescription, mIsbn;
    private int mRating, mCategory, mPosition;
    private Uri mCurrentUri;
    private Button mAddButton, mDeleteButton, rateButton, bookListButton;
    private RatingBar ratingBar;

    private TextView bookTitle, bookAuthor, isbnTextView, descriptionTextView, bookRating;
    private ImageView bookCover;

    private boolean isInDatabase, isTwoPanes;

    private FloatingActionButton shareFab;

    private static DetailFragment detailFragment;

    private ArrayList<String> isbnNumbers = new ArrayList<>();

    private Intent detailLauncher;
    private Bundle extra;

    private String activityTitle;

    public DetailFragment() {
        // Required empty public constructor
    }

    // Define a new interface that triggers a callback in the host activity
    public OnItemRemovedFromCategoryListener mListener;

    /**
     * Interface that listens for changes (update of category or deletion of book)
     * passes the category to activity.
     */
    public interface OnItemRemovedFromCategoryListener {
        void OnItemRemovedFromList(int category);
    }

    public static DetailFragment newInstance(OnItemRemovedFromCategoryListener listener) {
        detailFragment = new DetailFragment();
        detailFragment.mListener = listener;
        return detailFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Detect the type of device: phone or tablet
        isTwoPanes = getResources().getBoolean(R.bool.has_two_panes);
        // In tablet mode and if context is OnItemRemovedFromCategoryListener
        if (isTwoPanes && context instanceof OnItemRemovedFromCategoryListener) {
            try{
                // Try to cast the context
                mListener = (OnItemRemovedFromCategoryListener) context;
            } catch (ClassCastException e) {
                // Throw ClassCastException
                throw new ClassCastException(context.toString());
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        // Get the intent that launched the Activity
        detailLauncher = getActivity().getIntent();
        // Get extra data from intent
        extra = detailLauncher.getExtras();
        // Check if it has valid value
        getDataFromActivity(isTwoPanes);
        // Get the activity's title
        activityTitle = String.valueOf(getActivity().getTitle());

        // Get share button
        shareFab = rootView.findViewById(R.id.fab_detail);
        // Set share button't visibility to invisible as default
        shareFab.setVisibility(View.GONE);

        // Get the views, buttons and rating bar
        bookTitle = rootView.findViewById(R.id.detail_title);
        bookAuthor = rootView.findViewById(R.id.detail_author);
        isbnTextView = rootView.findViewById(R.id.isbn_nr);
        descriptionTextView = rootView.findViewById(R.id.description);
        bookCover = rootView.findViewById(R.id.detail_book_cover);
        bookRating = rootView.findViewById(R.id.rating_text);
        mDeleteButton = rootView.findViewById(R.id.delete_btn);
        mAddButton = rootView.findViewById(R.id.add_btn);
        rateButton = rootView.findViewById(R.id.rate_button);
        ratingBar = rootView.findViewById(R.id.ratingBar);
        bookListButton = rootView.findViewById(R.id.book_list_button);

        // Setup details and buttons
        setupBookDetails();

        // Set the Book's title as Activity's title
        getActivity().setTitle(mTitle);

        // call method that set visibility and text of buttons
        handleButtonsAndRatingBar();

        // Set click listener on share button
        shareFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create the string that will be shared of book
                String sharedText = mAuthors + ": " + mTitle;
                // If there's rating to the book
                if (mRating != 0) {
                    // Get the rating and convert it to string
                    String rateValue = String.valueOf((ratingBar.getRating()));
                    // Add rating value to shareable string
                    sharedText = sharedText + String.valueOf(R.string.rated) + rateValue + String.valueOf(R.string.rate_value);
                }
                // Log the shareable text
                Log.i(LOG_TAG, String.valueOf(R.string.share_value) + sharedText);
                // Call method that shares book
                shareReadBook(sharedText);
            }
        });

        return rootView;
    }

    /**
     * Get intent extra/ arguments of activity
     * and initialize loader
     *
     * @param isTablet determines whether device is a tablet
     */
    private void getDataFromActivity(boolean isTablet) {

        // Get the category first (needed for conditions)
        // If the Fragment get arguments
        if (getArguments() != null) {
            // Get the category
            mCategory = getArguments().getInt(String.valueOf(R.string.category));
            // Otherwise get the category from intent extra
        } else mCategory = extra.getInt(String.valueOf(R.string.category));

        // In two panes mode and category is 0
        if (isTablet & mCategory != 0 & extra == null) {
            // Get the uri of current book
            mCurrentUri = Uri.parse(getArguments().getString(String.valueOf(R.string.current_uri)));
            // Make LoaderManager work
            getLoaderManager()
                    .initLoader(EXISTING_BOOK_LOADER, null, this);
            // In case intent extra contains data (category 0 and !isTablet)
        } else if (extra != null) {
            // Get category first
            mCategory = extra.getInt(String.valueOf(R.string.category));
            // Also get the current book uri
            mCurrentUri = detailLauncher.getData();
            // Finally extract the rest of the data
            getExtraData(extra);
            // Clear the bundle
            extra.clear();
        } else {
            // In case category is 0 and isTablet=true
            // Make category and position 1
            mCategory = 1;
            mPosition = 1;
            // Create uri with position
            mCurrentUri = ContentUris.withAppendedId(BookContract.BookEntry.CONTENT_URI, mPosition);
            // Make LoaderManager work
            getLoaderManager()
                    .initLoader(EXISTING_BOOK_LOADER, null, this);
        }
    }

    /**
     * Extract the data from launcher intent
     *
     * @param extra is a bundle containing extra data of launcher intent
     */
    private void getExtraData(Bundle extra) {
        if (mCategory == 0) {
            // If the category is 0 (Search Activity)
            // Get the list of books
            mBooks = extra.getParcelableArrayList(String.valueOf(R.string.books));
            // Finally get the position of the book
            mPosition = extra.getInt(String.valueOf(R.string.position));
            // Otherwise make the loader work
        } else getLoaderManager()
                .initLoader(EXISTING_BOOK_LOADER, null, this);
    }

    /**
     * Extract book details from Book object and prepare book data.
     */
    private void setupBookDetails() {

        if (mCategory == 0) {
            // If it's Search Activity's Detail, get the selected book Object
            Book book = mBooks.get(mPosition);

            /* And get details of that book*/

            String rawAuthors = book.getAuthor();
            // Split authors and separate them with ","
            String splitAuthors = rawAuthors.split(",")[0];
            // Replace unnecessary characters from authors
            mAuthors = splitAuthors
                    .replace("[", " ")
                    .replace("]", " ")
                    .replace('"', ' ');
            // Get the rest of the book details
            mTitle = book.getTitle();
            mIsbn = book.getIsbn();
            mDescription = book.getDescription();
            mBookCover = book.getImageUrl();
            // Display values in views
            setValuesOnViews();
        }
    }


    /**
     * Set the values on certain vies and load image
     */
    private void setValuesOnViews() {
        bookTitle.setText(mTitle);
        bookAuthor.setText(mAuthors);
        isbnTextView.setText(mIsbn);
        descriptionTextView.setText(mDescription);
        Glide.with(getContext()).load(mBookCover).placeholder(R.drawable.place_holder).into(bookCover);

        // If the book category is Read
        if (mCategory == 3) {
            // If app runs on phone
            if (!isTwoPanes) {
                // Make share button visible
                shareFab.setVisibility(View.VISIBLE);
            }
            // And user already rated the book
            if (mRating != 0) {
                // Inform the user of existing rating
                bookRating.setText(R.string.already_rated);
                // Change text on rating button
                rateButton.setText(R.string.change_rating);
                // If it's not rated yet
            } else {
                // Set text on text view and button accordingly
                bookRating.setText(R.string.rate_this_book);
                rateButton.setText(R.string.rate);
            }
            // Set that data on rating bar
            ratingBar.setRating(mRating);
        }
    }

    /**
     * Launching Detail Activity from different activities
     * will influence the buttons
     */
    private void handleButtonsAndRatingBar() {
        // Make all the views disappear regards to rating
        hideViewsForRating();

        switch (mCategory) {

            // If it started from Search Activity
            case 0:
                // make delete button disappear
                mDeleteButton.setVisibility(View.GONE);
                // set text on addition button (add to wish list)
                mAddButton.setText(R.string.search_list_btn_txt);
                break;

            // If it started from Book List Activity
            case 1:
                // and from Wish List Fragment,
                // set text on addition button (mark as unread) and make it visible
                mAddButton.setVisibility(View.VISIBLE);
                mAddButton.setText(R.string.wish_list_btn_txt);
                break;
            case 2:
                // and from Unread List Fragment,
                // set text on addition button (mark as read) and make it visible
                mAddButton.setVisibility(View.VISIBLE);
                mAddButton.setText(R.string.unread_list_btn_txt);
                break;
            case 3:
                // and from Read List Fragment,
                // cut the description text to make other elements visible
                if (isTwoPanes) {
                    descriptionTextView.setMaxLines(10);
                } else descriptionTextView.setMaxLines(3);
                // make addition button disappear
                mAddButton.setVisibility(View.GONE);
                // display necessary views
                displayViewsForRating();
                break;
            // set the default text as the same as Wsh List Fragment's
            default:
                mAddButton.setText(R.string.wish_list_btn_txt);
                break;
        }
        setUpClickListeners();
    }

    /**
     * Display rating bar, rating text view and rate button
     */
    private void hideViewsForRating() {
        bookRating.setVisibility(View.GONE);
        rateButton.setVisibility(View.GONE);
        ratingBar.setVisibility(View.GONE);
    }

    /**
     * Hide rating bar, rating text view and rate button
     */
    private void displayViewsForRating() {
        bookRating.setVisibility(View.VISIBLE);
        rateButton.setVisibility(View.VISIBLE);
        ratingBar.setVisibility(View.VISIBLE);
    }

    /**
     * Make addition button invisible
     * and navigation button (book list) visible
     */
    private void afterAdditionClicked() {
        mAddButton.setVisibility(View.GONE);
        bookListButton.setVisibility(View.VISIBLE);
    }

    /**
     * Set OnClickListeners on buttons
     */
    private void setUpClickListeners() {

        // In Read Category
        if (mCategory == 3) {
            // Set OnClickListener on the rating button
            rateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    float rating = ratingBar.getRating();
                    updateBookRating(rating);
                }
            });
        } else {
            // In the rest of the categories
            // Set click listener on the addition button
            mAddButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Check whether book already added to database
                    isInDatabase = databaseChecker();
                    // If it's not added
                    if (!isInDatabase) {
                        // Add the book to database
                        insertBook();
                        broadcastUpdateActionToWidget();
                        // Setup buttons
                        afterAdditionClicked();
                    } else if (mCategory == 1 || mCategory == 2){
                        // If it's already in database and category is 1 / 2
                        // change the category
                        changeCategory(mCategory);
                        broadcastUpdateActionToWidget();
                    } else {
                        // Otherwise (already in database and category is 3)
                        // setup buttons, make notification and log
                        afterAdditionClicked();
                        Toast.makeText(getActivity(), R.string.book_already_added_toast,
                                Toast.LENGTH_SHORT).show();
                        Log.i(LOG_TAG, String.valueOf(R.string.book_already_added_log));
                    }
                }
            });
        }

        // Set a click listener on the deletion button
        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Delete selected book
                deleteBook();
                broadcastUpdateActionToWidget();
            }
        });

        // Set a click listener on the book list button
        bookListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start Book List Activity to check out newly added books
                Intent bookListIntent = new Intent(getActivity(), BookListActivity.class);
                startActivity(bookListIntent);
            }
        });
    }


    private boolean databaseChecker() {
        BookDbHelper dbHelper = new BookDbHelper(getContext());

        SQLiteDatabase database = dbHelper.getReadableDatabase();

        Cursor cursor = database.query(BookContract.BookEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null);

        while (cursor.moveToNext()) {
            int idIndex = cursor.getColumnIndex(BookContract.BookEntry.COLUMN_ISBN_NUMBER);
            String isbn = cursor.getString(idIndex);
            isbnNumbers.add(isbn);
        }

        if (isbnNumbers.contains(mIsbn)) {
            Log.i(LOG_TAG, String.valueOf(R.string.book_already_added_log));
            isInDatabase = true;

        } else {
            Log.i(LOG_TAG, String.valueOf(R.string.new_book));
            isInDatabase = false;
        }

        // Close the cursor and the database helper
        cursor.close();
        dbHelper.close();

        return isInDatabase;
    }

    private void updateBookRating(float rating) {
        ContentValues values = new ContentValues();
        values.put(BookContract.BookEntry.COLUMN_RATING, rating);

        // Otherwise Update it
        int rowsAffected = getActivity().getContentResolver()
                .update(mCurrentUri, values, null, null);

        if (rowsAffected == 0) {
            // If no rows were affected, then there was an error with the update.
            Toast.makeText(getActivity(), R.string.rating_failed, Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the update was successful and we can display a toast.
            Toast.makeText(getActivity(), R.string.rating_updated, Toast.LENGTH_SHORT).show();
        }
    }

    private void changeCategory(int category) {
        if (mCurrentUri != null) {

            if (category == 1 || category == 2){
                // Increase category variable by 1
                mCategory++;
            }
            // Get the content values
            ContentValues categoryValues = new ContentValues();
            // attach the new category to it
            categoryValues.put(BookContract.BookEntry.COLUMN_CATEGORY_ID, mCategory);

            //Update current book's category
            int rowsAffected = getActivity().getContentResolver()
                    .update(mCurrentUri, categoryValues, null, null);

            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(getActivity(), R.string.update_failed, Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(getActivity(), R.string.book_updated, Toast.LENGTH_SHORT).show();
            }
        }
        // If it's not tablet mode
        if (!isTwoPanes) {
            // Get back to the Book List Activity
            getActivity().finish();
            // Otherwise
        } else {
            // make category 0
            mCategory = 0;
            // Call the method of interface
            mListener.OnItemRemovedFromList(mCategory);
        }
    }

    /**
     * Method that inserts a new row (selected book) into  to the database
     */
    private void insertBook() {

        if (mCurrentUri == null) {
            mCategory++;
        }

        // Get the Content Values
        ContentValues contentValues = new ContentValues();

        // Put all the book data that needs to be stored into the Content Values
        contentValues.put(BookContract.BookEntry.COLUMN_TITLE, mTitle);
        contentValues.put(BookContract.BookEntry.COLUMN_AUTHORS, mAuthors);
        contentValues.put(BookContract.BookEntry.COLUMN_BOOK_COVER_PATH, mBookCover);
        contentValues.put(BookContract.BookEntry.COLUMN_ISBN_NUMBER, mIsbn);
        contentValues.put(BookContract.BookEntry.COLUMN_DESCRIPTION, mDescription);
        contentValues.put(BookContract.BookEntry.COLUMN_CATEGORY_ID, mCategory);
        int DEFAULT_RATING = 0;
        contentValues.put(BookContract.BookEntry.COLUMN_RATING, DEFAULT_RATING);

        // Get the Content Resolver and insert these a new row
        Uri newUri = getActivity().getContentResolver()
                .insert(BookContract.BookEntry.CONTENT_URI, contentValues);

        if (newUri == null) {
            // Notify user that book is not saved
            Toast.makeText(getActivity(), R.string.addition_failure, Toast.LENGTH_SHORT).show();
            // Notify user that book is added to the database into the Wish List category
        } else Toast.makeText(getActivity(), R.string.book_added, Toast.LENGTH_SHORT).show();
    }

    /**
     * Method that deletes a book from database
     */
    private void deleteBook() {
        if (mCurrentUri != null) {
            // Get the number of deleted rows
            int rowsDeleted = getActivity().getContentResolver().delete(mCurrentUri, null, null );

            // Notify the user if the deletion was unsuccessful and log it
            if (rowsDeleted == 0) {
                Toast.makeText(getActivity(), R.string.deletion_failure_toast, Toast.LENGTH_SHORT).show();
                Log.e(LOG_TAG, String.valueOf(R.string.deletion_failure_log));
            } else {
                if (isbnNumbers.contains(mIsbn)) {
                    isbnNumbers.remove(mIsbn);
                }
                Toast.makeText(getActivity(), R.string.book_deletion_toast, Toast.LENGTH_SHORT).show();
                Log.i(LOG_TAG, String.valueOf(R.string.book_deletion_log));
                //Intent bookListIntent = new Intent(getActivity(), BookListActivity.class);
                //startActivity(bookListIntent);
            }
        }
        // if it's not tablet mode
        if (!isTwoPanes) {
            // Get back to the Book List Activity
            getActivity().finish();
            // Otherwise
        } else {
            // Call method of interface
            mListener.OnItemRemovedFromList(mCategory);
            // Make category 0
            mCategory = 0;
        }
    }

    /**
     * Creating loader to query data
     *
     * @param id of the loader
     * @param args arguments
     * @return loader
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
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

        // Loader executing provider's query
        return new CursorLoader(getActivity(),
                mCurrentUri,
                projection,
                null,
                null,
                null);
    }

    /**
     * Loads queried data
     *
     * @param loader executing query of provider
     * @param data holds all details of the book
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // If cursor does not hold any valid value
        if (data == null || data.getCount() < 1) {
            // Only if it cursor is empty, hide all views
            if (data.getCount() < 1) hideDetailViews();
            // Return early
            return;
        }
        // Show all views for fragment
        showDetailViews();

        int titleIndex, authorsIndex, bookCoverIndex, isbnIndex, descriptionIndex, categoryIndex, ratingIndex;
        // Move to the first row of the cursor, it should be the only one
        if (data.moveToFirst()) {

            // Read data from this row
            titleIndex = data.getColumnIndex(BookContract.BookEntry.COLUMN_TITLE);
            authorsIndex = data.getColumnIndex(BookContract.BookEntry.COLUMN_AUTHORS);
            bookCoverIndex = data.getColumnIndex(BookContract.BookEntry.COLUMN_BOOK_COVER_PATH);
            isbnIndex = data.getColumnIndex(BookContract.BookEntry.COLUMN_ISBN_NUMBER);
            descriptionIndex = data.getColumnIndex(BookContract.BookEntry.COLUMN_DESCRIPTION);
            categoryIndex = data.getColumnIndex(BookContract.BookEntry.COLUMN_CATEGORY_ID);
            ratingIndex = data.getColumnIndex(BookContract.BookEntry.COLUMN_RATING);

            // Extract the value from the cursor
            mTitle = data.getString(titleIndex);
            mAuthors = data.getString(authorsIndex);
            mBookCover = data.getString(bookCoverIndex);
            mIsbn = data.getString(isbnIndex);
            mDescription = data.getString(descriptionIndex);
            mRating = data.getInt(ratingIndex);
            mCategory = data.getInt(categoryIndex);

            setValuesOnViews();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    /**
     * Updates the view once change occured
     * (change of category / deletion)
     *
     * @param details is a bundle containing data of book
     */
    public void updateDetailFragment(Bundle details) {
        // in case bundle holds valid value
        if (details != null) {
            // Get current book's uri
            mCurrentUri = Uri.parse(details.getString(String.valueOf(R.string.current_uri)));
        }
        // Restart loader to update views
        getLoaderManager().restartLoader(EXISTING_BOOK_LOADER, null, this);

        // If current title of activity is null
        if (getActivity().getTitle() == null) {
            // Leave activityTitle variable's value as it is, log it
            Log.i(LOG_TAG, String.valueOf(R.string.title_info_log) + activityTitle);
            // Otherwise, change it to current title
        } else activityTitle = String.valueOf(getActivity().getTitle());

        // Check the vale of activityTitle
        switch (activityTitle) {
            case "Wish list":
                // Change category to 1
                mCategory = 1;
                break;
            case "Unread" :
                // Change category to 2
                mCategory = 2;
                break;
            case "Read" :
                // Change category to 3
                mCategory = 3;
                break;
        }
        // Handle this change by calling that takes care of buttons, and rating bar
        handleButtonsAndRatingBar();
    }

    /**
     * Hide all views
     */
    private void hideDetailViews() {
        bookTitle.setVisibility(View.GONE);
        bookAuthor.setVisibility(View.GONE);
        isbnTextView.setVisibility(View.GONE);
        descriptionTextView.setVisibility(View.GONE);
        bookCover.setVisibility(View.GONE);
        mAddButton.setVisibility(View.GONE);
        mDeleteButton.setVisibility(View.GONE);
        // In read category
        if (mCategory == 3) {
            // hide views in connection with rating
            ratingBar.setVisibility(View.GONE);
            rateButton.setVisibility(View.GONE);
            bookRating.setVisibility(View.GONE);
        }
    }

    /**
     * Display all views
     */
    private void showDetailViews() {
        bookTitle.setVisibility(View.VISIBLE);
        bookAuthor.setVisibility(View.VISIBLE);
        isbnTextView.setVisibility(View.VISIBLE);
        descriptionTextView.setVisibility(View.VISIBLE);
        bookCover.setVisibility(View.VISIBLE);
        mDeleteButton.setVisibility(View.VISIBLE);
        // In read category
        if (mCategory == 3) {
            // set views visible in connection with rating
            ratingBar.setVisibility(View.VISIBLE);
            rateButton.setVisibility(View.VISIBLE);
            bookRating.setVisibility(View.VISIBLE);
        } else mAddButton.setVisibility(View.VISIBLE);
    }

    /**
     * Share read book data
     *
     * @param text is String variable containing book's author, title and rating
     */
    private void shareReadBook(String text) {
        // Create an intent
        Intent shareDetails = new Intent();
        // set send action on it
        shareDetails.setAction(Intent.ACTION_SEND);
        // Set the type
        shareDetails.setType("text/plain");
        // put subject and body as extra data
        shareDetails.putExtra(android.content.Intent.EXTRA_SUBJECT, String.valueOf(R.string.read_this_book));
        shareDetails.putExtra(Intent.EXTRA_TEXT, text);
        // Start the activity
        getActivity().startActivity(Intent.createChooser(shareDetails, getResources().getText(R.string.read_list)));
    }

    /**
     *  Broadcast update action to widget provider
     */
    private void broadcastUpdateActionToWidget() {
        if (mCategory == 1) {
            Intent updateWidget = new Intent(getActivity(), BookWidgetProvider.class);
            updateWidget.setAction(ACTION_UPDATE);
            getActivity().sendBroadcast(updateWidget);
        }
    }
}