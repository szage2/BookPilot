package com.example.szage.bookpilot.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.szage.bookpilot.R;


/**
 * Serves content provider for the Book Pilot app data.
 * Allows inserting, querying, deleting, updating data.
 */

public class BookProvider extends ContentProvider {

    // URI matcher code for the content URI in case of the whole book table
    public static final int ALL_BOOKS = 100;
    // URI matcher code for the content URI in case of a single book
    public static final int BOOK_ID = 101;

    private static final UriMatcher mUriMatcher = buildUriMatcher();
    private BookDbHelper mdbHelper;

    /**
     * Builds the Uri Matcher that match the content uri to the corresponding data.
     *
     * @return Uri matcher
     */
    private static UriMatcher buildUriMatcher() {

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = BookContract.CONTENT_AUTHORITY;

        // Provides access to multiple rows of the table
        matcher.addURI(authority, BookContract.PATH_BOOK, ALL_BOOKS);

        // Provides access to a single row of the table
        matcher.addURI(authority, BookContract.PATH_BOOK + "/#", BOOK_ID);

        return matcher;
    }


    /**
     *  Initialize the provider
     *
     * @return true
     */
    @Override
    public boolean onCreate() {
        mdbHelper = new BookDbHelper(getContext());
        return true;
    }

    /**
     * Returns data according to call
     *
     * @param uri is the route to queried data
     * @param projection is the list of queried data
     * @param selection is a filter that determines returned rows
     * @param selectionArgs is the condition by which rows are filtered
     * @param sortOrder is how to order rows
     * @return cursor that holds the result of the query
     */
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        // Get readable database
        SQLiteDatabase database = mdbHelper.getReadableDatabase();
        // Cursor
        Cursor cursor;

        int match = mUriMatcher.match(uri);
        switch (match) {
            case ALL_BOOKS :
                cursor = database.query(BookContract.BookEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case BOOK_ID :

                // Extract the id from the uri "_id=?"
                // For every question mark, we need to have a selection arg, in this case it's one
                selection = BookContract.BookEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                cursor = database.query(
                        BookContract.BookEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new  IllegalArgumentException(String.valueOf(R.string.unknown_uri) + uri);
        }
        // In case of data change, update the cursor
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }


    /**
     * Returns the type of data in the content provider
     *
     * @param uri
     * @return type of data
     */
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {

        int match = mUriMatcher.match(uri);
        switch (match) {
            case ALL_BOOKS :
                // All books in the database
                return BookContract.BookEntry.CONTENT_LIST_TYPE;
            case BOOK_ID :
                // A single book in the database
                return BookContract.BookEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException((String.valueOf(R.string.unknown_uri)) + uri);
        }
    }

    /**
     * Inserts new data into the content provider
     *
     * @param uri is path for the book
     * @param contentValues book data
     * @return
     */
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        int match = mUriMatcher.match(uri);
        switch (match) {
            case ALL_BOOKS :
                return insertBook(uri, contentValues);
            default:
                throw new IllegalArgumentException(String.valueOf(R.string.not_supported_uri) + uri);
        }
    }

    /**
     * Helper method that insert a book to the database
     *
     * @param uri is path for the book
     * @param values book data
     * @return uri and id of the inserted book
     */
    private Uri insertBook(Uri uri, ContentValues values) {

        /* Check validity of variables: title, authors, description, category, rating
           These are the must have details of a book , if the user want to add it to wish list.
         */

        String title = values.getAsString(BookContract.BookEntry.COLUMN_TITLE);
        if (title == null) {
            throw new  IllegalArgumentException (String.valueOf(R.string.title_requiremnt));
        }

        String authors = values.getAsString(BookContract.BookEntry.COLUMN_AUTHORS);
        if (authors == null) {
            throw new  IllegalArgumentException (String.valueOf(R.string.author_requiremnt));
        }

        String description = values.getAsString(BookContract.BookEntry.COLUMN_DESCRIPTION);
        if (description == null) {
            throw new  IllegalArgumentException (String.valueOf(R.string.description_requiremnt));
        }

        Integer category = values.getAsInteger(BookContract.BookEntry.COLUMN_CATEGORY_ID);
        if (category == null || !BookContract.BookEntry.isValidCategory(category)) {
            throw new  IllegalArgumentException (String.valueOf(R.string.category_requiremnt));
        }

        Integer rating = values.getAsInteger(BookContract.BookEntry.COLUMN_RATING);
        if (rating == null || !BookContract.BookEntry.isValidRating(rating)) {
            throw new  IllegalArgumentException (String.valueOf(R.string.rating_requiremnt));
        }

        // Get writable database
        SQLiteDatabase database = mdbHelper.getWritableDatabase();

        long id = database.insert(BookContract.BookEntry.TABLE_NAME, null, values);
        if (id == -1) {
            throw new IllegalArgumentException(String.valueOf(R.string.id_requiremnt));
        }

        // Notify the Content Resolver of the inserted data
        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Deletes data from the content provider
     *
     * @param uri path for the book
     * @param selection is a filter determines returned rows
     * @param selectionArgs is the condition by which rows are filtered
     * @return rowsDeleted is the number of deleted rows
     */
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {

        // Get writable database
        SQLiteDatabase database = mdbHelper.getWritableDatabase();

        int rowsDeleted;
        final int match = mUriMatcher.match(uri);

        switch (match) {
            case ALL_BOOKS :
                // Delete all rows
                rowsDeleted = database.delete(
                        BookContract.BookEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;

            case BOOK_ID :
                // Delete a single row
                selection = BookContract.BookEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                rowsDeleted = database.delete(BookContract.BookEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;

            default:
                throw new IllegalArgumentException(String.valueOf(R.string.unsupported_operation) + uri);
        }

        // Notify the Content Resolver of the deleted data
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }


    /**
     * Updates data in the content provider with a helper method.
     *
     * @param uri is the path of updated data
     * @param contentValues values that changed
     * @param selection is a filter that determines returned rows
     * @param selectionArgs is the condition by which rows are filtered
     * @return number of updated rows
     */
    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {

        final int match = mUriMatcher.match(uri);

        switch (match) {
            case ALL_BOOKS :
                return updateBook(uri, contentValues, selection, selectionArgs);
            case BOOK_ID :
                // Extract the id from the uri to specify rows to be updated
                selection = BookContract.BookEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateBook(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException(String.valueOf(R.string.unsupported_update) + uri);
        }
    }


    /**
     * Helper method that:
     *
     * I.checks data in cases when data can be updated:
     *      1. User changes the category of the book
     *      2. User adds rating to a read book,
     *
     * II. applies changes to the specified rows in the database.
     *
     * @param uri is the path of updated data
     * @param contentValues values that changed
     * @param selection is a filter that determines returned rows
     * @param selectionArgs is the condition by which rows are filtered
     * @return number of rows that were updated
     */
    private int updateBook(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {

        /* Check the validity of two variables that can be changed: category and rating */

        if (contentValues.containsKey(BookContract.BookEntry.COLUMN_CATEGORY_ID)) {
            Integer category = contentValues.getAsInteger(BookContract.BookEntry.COLUMN_CATEGORY_ID);
            if (category == null || !BookContract.BookEntry.isValidCategory(category)) {
                throw new IllegalArgumentException(String.valueOf(R.string.category_requiremnt));
            }
        }

        if (contentValues.containsKey(BookContract.BookEntry.COLUMN_RATING)) {
            Integer rating = contentValues.getAsInteger(BookContract.BookEntry.COLUMN_RATING);
            if (rating == null || !BookContract.BookEntry.isValidRating(rating)) {
                throw new IllegalArgumentException(String.valueOf(R.string.rating_requiremnt));
            }
        }

        // If there's no changes in data, don't try to update database
        if (contentValues.size() == 0) {
            return 0;
        }

        // Otherwise get writable database
        SQLiteDatabase database = mdbHelper.getWritableDatabase();
        // Get the number of updated rows
        int rowsUpdated = database.update(BookContract.BookEntry.TABLE_NAME,
                contentValues, selection, selectionArgs);
        // If updated rows is not 0, notify the Content Resolver
        // that data has been changed at the given uri
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return that number of updated rows
        return rowsUpdated;
    }
}
