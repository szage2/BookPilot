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
import com.example.szage.bookpilot.ui.DetailActivity;


/**
 * Book Adapter creates view holders for each list item
 */

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.ViewHolder> {

    private Context mContext;
    private int mSumItem = 8;
    private LayoutInflater mLayoutInflater;

    public BookAdapter(Context context) {
        super();
        mLayoutInflater = LayoutInflater.from(context);
        mContext = context;

    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView bookTitle;
        TextView bookAuthor;
        ImageView bookCover;

        public ViewHolder(View itemView) {
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
            // Create an intent that launches Detail Activity
            Intent detailIntent = new Intent(mContext, DetailActivity.class);
            // Launch Detail Activity
            mContext.startActivity(detailIntent);
        }
    }

    @Override
    public BookAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.book_list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(BookAdapter.ViewHolder holder, int position) {

        // Create dummy data (will be replaced by real)
        for (int i = 0; i < mSumItem + 1; i++) {
            // Create variables
            String title = "Book Title " + position;
            String author = "Author";
            // Set the content on the views, load image
            holder.bookTitle.setText(title);
            holder.bookAuthor.setText(author);
            Glide.with(mContext).load("").placeholder(R.drawable.place_holder).into(holder.bookCover);
        }
    }

    @Override
    public int getItemCount() {
        return mSumItem;
    }

}
