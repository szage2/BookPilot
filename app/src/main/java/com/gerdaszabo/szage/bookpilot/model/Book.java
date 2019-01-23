package com.gerdaszabo.szage.bookpilot.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Book Object
 */

public class Book implements Parcelable{

    private String mTitle;
    private String mAuthor;
    private String mIsbn;
    private String mImageUrl;
    private String mDescription;

    // Constructor
    public Book(String title, String author, String isbn, String imageUrl, String description) {
        mTitle = title;
        mAuthor = author;
        mIsbn = isbn;
        mImageUrl = imageUrl;
        mDescription = description;
    }

    // Constructor
    protected Book(Parcel in) {
        mTitle = in.readString();
        mAuthor = in.readString();
        mIsbn = in.readString();
        mImageUrl = in.readString();
        mDescription = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mTitle);
        parcel.writeString(mAuthor);
        parcel.writeString(mIsbn);
        parcel.writeString(mImageUrl);
        parcel.writeString(mDescription);
    }

    public static final Creator<Book> CREATOR = new Creator<Book>() {
        @Override
        public Book createFromParcel(Parcel in) {
            return new Book(in);
        }

        @Override
        public Book[] newArray(int size) {
            return new Book[size];
        }
    };

    /* Getter methods */

    public String getTitle() {
        return mTitle;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public String getIsbn() {
        return mIsbn;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public String getDescription() {
        return mDescription;
    }
}
