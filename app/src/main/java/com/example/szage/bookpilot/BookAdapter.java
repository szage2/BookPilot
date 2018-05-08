package com.example.szage.bookpilot;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.szage.bookpilot.model.Book;
import com.example.szage.bookpilot.ui.DetailActivity;

import java.util.ArrayList;


/**
 * Book Adapter creates view holders for each list item
 */

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.ViewHolder> {

    private Context mContext;
    private int mSumItem = 8;
    private LayoutInflater mLayoutInflater;
    private ArrayList<Book> mBooks = new ArrayList<>();
    private int mCategory;

    // Constructor
    public BookAdapter(Context context, ArrayList<Book> list, int category) {
        super();
        mLayoutInflater = LayoutInflater.from(context);
        mContext = context;
        mBooks = list;
        mCategory = category;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView bookTitle;
        TextView bookAuthor;
        ImageView bookCover;

        private ViewHolder(View itemView) {
            super(itemView);
            // Get the views
            bookTitle = itemView.findViewById(R.id.book_title);
            bookAuthor = itemView.findViewById(R.id.book_author);
            bookCover = itemView.findViewById(R.id.book_cover);
            // Set a click listener on the book cover image
            bookCover.setOnClickListener(this);
        }

        /**
         * Click listener that launched Detail Activity
         *
         * @param view is the clicked image view of the book cover
         */
        @Override
        public void onClick(View view) {

            // Get the position of the selected view
            int position = getLayoutPosition();
            // Create an intent that launches Detail Activity
            Intent detailIntent = new Intent(mContext, DetailActivity.class);
            if (mBooks != null && !mBooks.isEmpty()) {
                /* put book list and selected object's position
                as extra data in the intent */
                detailIntent.putParcelableArrayListExtra(String.valueOf(R.string.books), mBooks);
                detailIntent.putExtra(String.valueOf(R.string.position), position);
            }
            detailIntent.putExtra(String.valueOf(R.string.category), mCategory);
            // Launch Detail Activity
            mContext.startActivity(detailIntent);
        }
    }


    /**
     * Create new View Holder
     *
     * @param parent is the view group that the view will be added to
     * @param viewType the type of view
     * @return new viewHolder
     */
    @Override
    public BookAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.book_list_item, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Updates the contents of item at the given position
     *
     * @param holder that has the views to be recycled
     * @param position is the position of listed item
     */
    @Override
    public void onBindViewHolder(BookAdapter.ViewHolder holder, int position) {

        String picture = null;

        // If mBooks list has valid data
        if (mBooks != null && !mBooks.isEmpty()) {
            // Get the book with details
            Book book = mBooks.get(position);
            String title = book.getTitle();
            String authors = book.getAuthor();
            // Split authors and separate them with ","
            String splitAuthors = authors.split(",")[0];
            // Replace unnecessary characters from authors
            String cleanAuthors = splitAuthors
                    .replace("[", " ")
                    .replace("]", " ")
                    .replace('"', ' ');
            picture = book.getImageUrl();
            // Set the content on views, load image
            holder.bookTitle.setText(title);
            holder.bookAuthor.setText(cleanAuthors);

        } else {
            // Create dummy data (will be replaced by real)
            for (int i = 0; i < mSumItem + 1; i++) {
                // Create variables
                String title = String.valueOf(R.string.book_title) + position;
                String author = String.valueOf(R.string.author);
                // Set the content on the views, load image
                holder.bookTitle.setText(title);
                holder.bookAuthor.setText(author);
            }
        }
        Glide.with(mContext).load(picture).placeholder(R.drawable.place_holder).into(holder.bookCover);
    }

    /**
     * Counts the number of items in the list
     *
     * @return number of items
     */
    @Override
    public int getItemCount() {
        // if book list is empty
        if (mBooks == null) {
            // Make fix number of dummy items
            return mSumItem;
            // Otherwise list all book items
        } else return mBooks.size();
    }

}
