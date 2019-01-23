package com.gerdaszabo.szage.bookpilot;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.gerdaszabo.szage.bookpilot.data.BookContract;


/**
 * This adapter is for a list of grid views that uses a Cursor holding Book data of the database
 */

public class BookCursorAdapter extends CursorAdapter {

    private Context mContext;

    // Constructor
    public BookCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
        mContext = context;
    }

    /**
     * Makes new list item view
     *
     * @param context is the app context
     * @param cursor that holds the data
     * @param viewGroup is that new views attached to
     * @return new list item
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        // Inflate a list item view
        return LayoutInflater.from(context).inflate(R.layout.book_list_item, viewGroup, false);
    }

    /**
     * Binds book data to the given list item layout
     *
     * @param view that created as a list item
     * @param context app context
     * @param cursor that holds the data
     */
    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {

        // Get the position of the cursor pointing to the current book
        int mPosition = cursor.getPosition();
        cursor.moveToPosition(mPosition);

        // Get the views
        TextView bookTitle = view.findViewById(R.id.book_title);
        TextView bookAuthor = view.findViewById(R.id.book_author);
        ImageView bookCover = view.findViewById(R.id.book_cover);

        // Get the column index for each detail regarding the book
        int titleColumnIndex = cursor.getColumnIndexOrThrow(BookContract.BookEntry.COLUMN_TITLE);
        int authorsColumnIndex = cursor.getColumnIndexOrThrow(BookContract.BookEntry.COLUMN_AUTHORS);
        int bookCoverColumnIndex = cursor.getColumnIndexOrThrow(BookContract.BookEntry.COLUMN_BOOK_COVER_PATH);

        // Get Book details from the cursor
        String title = cursor.getString(titleColumnIndex);
        String authors = cursor.getString(authorsColumnIndex);
        String bookCoverPath = cursor.getString(bookCoverColumnIndex);

        // Set the details data on the views and display the book cover
        bookTitle.setText(title);
        bookAuthor.setText(authors);
        if (mContext != null) {
            Glide.with(mContext).load(bookCoverPath).placeholder(R.drawable.place_holder).into(bookCover);
        }
    }
}
