package com.example.szage.bookpilot.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Contract for the books.
 * Defines table and column names for book database.
 */

public class BookContract {

    public BookContract() {
        super();
    }

    // Content Provider
    public static final String CONTENT_AUTHORITY = "com.example.szage.bookpilot";

    // Creating base uri that will be used to reach content provider
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_BOOK = "bookpilot";

    /**
     * Inner class that defines constant values for the book table.
     * Each entry in the database represents a book.
     */
    public static final class BookEntry implements BaseColumns {

        // Let to access the book data in the provider
        public static final Uri CONTENT_URI = BASE_CONTENT_URI
                .buildUpon()
                .appendPath(PATH_BOOK)
                .build();


        /* Content types */

        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOK;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOK;

        /* Table name and Columns */

        // Name of the table
        public static final String TABLE_NAME = "book";

        /**
         * Unique ID number for the product (only for use in the database table).
         *
         * Type: INTEGER
         */
        public static final String _ID = BaseColumns._ID;


        /** Title for each book
         * Type: TEXT
         */
        public static final String COLUMN_TITLE = "title";

        /** Authors for each book
         * Type: TEXT
         */
        public static final String COLUMN_AUTHORS = "authors";

        /** Book cover path for each book
         * Type: TEXT
         */
        public static final String COLUMN_BOOK_COVER_PATH = "book_cover_path";

        /** ISBN number for each book
         * Type: INTEGER
         */
        public static final String COLUMN_ISBN_NUMBER = "isbn_number";

        /** Description for each book
         * Type: TEXT
         */
        public static final String COLUMN_DESCRIPTION = "description";

        /** Identifies which of the categories belong the each book
         *  Default is 1.
         * Type: INTEGER
         */
        public static final String COLUMN_CATEGORY_ID = "category_id";

        /**
         * Possible values for the category of the book.
         *
         * 0 category is when the user search for a book (not accepted for the database),
         * but when it's saved, it will be added to Wish list, so it's category is 1.
         */
        public static final int WISH_LIST = 1;
        public static final int UNREAD_LIST = 2;
        public static final int READ_LIST = 3;


        /** Rating for each book
         * Default is 0.
         * Type: INTEGER
         */
        public static final String COLUMN_RATING = "rating";

        /**
         * Possible values for the rating of the book.
         */
        public static final int DEFAULT_STAR = 0;
        public static final int ONE_STAR = 1;
        public static final int TWO_STARS = 2;
        public static final int THREE_STARS = 3;
        public static final int FOUR_STARS = 4;
        public static final int FIVE_STARS = 5;

        /**
         * Returns whether the book's category is from the given ones (valid or not).
         *
         * @param category is the category of book (Wish list/ Unread list/ Read list of books)
         * @return is the boolean that identifies the validity of the variable
         */
        public static boolean isValidCategory(int category) {
            return category == WISH_LIST || category == UNREAD_LIST || category == READ_LIST;
        }

        /**
         * Returns whether the book's rating is from the given ones (valid or not).
         *
         * @param rating is the rating of the book (0-5)
         * @return is the boolean that identifies the validity of the variable
         */
        public static boolean isValidRating(int rating) {
            return rating == DEFAULT_STAR || rating == ONE_STAR || rating == TWO_STARS
                    || rating == THREE_STARS || rating == FOUR_STARS || rating == FIVE_STARS;

        }
    }
}
