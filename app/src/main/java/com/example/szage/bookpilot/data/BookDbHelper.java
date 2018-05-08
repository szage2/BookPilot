package com.example.szage.bookpilot.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Database helper for BookPilot app.
 * Creates database and controls versions.
 */

public class BookDbHelper extends SQLiteOpenHelper {

    // Name of database file
    public static final String DATABASE_NAME = "book.db";

    // Version of database
    public static final int DATABASE_VERSION = 1;


    /**
     * Constructs a new instance of BookDbHelper
     *
     * @param context is current context
     */
    public BookDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Creates a String that contains the SQL statement in order to create the book table
     *
     * @param sqLiteDatabase
     */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String SQL_CREATE_BOOK_TABLE = " CREATE TABLE " + BookContract.BookEntry.TABLE_NAME + " (" +
                        BookContract.BookEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        BookContract.BookEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                        BookContract.BookEntry.COLUMN_AUTHORS + " TEXT NOT NULL, " +
                        BookContract.BookEntry.COLUMN_BOOK_COVER_PATH + " TEXT, " +
                        BookContract.BookEntry.COLUMN_ISBN_NUMBER + " INTEGER, " +
                        BookContract.BookEntry.COLUMN_DESCRIPTION + " TEXT NOT NULL, " +
                        BookContract.BookEntry.COLUMN_CATEGORY_ID + " INTEGER NOT NULL DEFAULT 1, " +
                        BookContract.BookEntry.COLUMN_RATING + " INTEGER NOT NULL DEFAULT 0);";

        // Execute the SQL statement
        sqLiteDatabase.execSQL(SQL_CREATE_BOOK_TABLE);
    }

    // Called when database needs to be upgraded
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        String SQL_DELETE_BOOK_DETAIL_QUERY = "DELETE FROM " + BookContract.BookEntry.TABLE_NAME;
        // Execute the SQL statement
        sqLiteDatabase.execSQL(SQL_DELETE_BOOK_DETAIL_QUERY);
    }
}
