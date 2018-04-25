package com.example.szage.bookpilot.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.szage.bookpilot.BookAdapter;
import com.example.szage.bookpilot.R;
import com.example.szage.bookpilot.model.Book;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * interface
 * to handle interaction events.
 * Use the {@link DetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DetailFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final String SEARCH_BTN = "Add to Wish List";
    private static final String WISH_LIST_BTN = "Mark as Unread";
    private static final String UNREAD_LIST_BTN = "Mark as Read";
    private static final String READ_LIST_BTN = "Rate";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private int mCategory;
    private ArrayList<Book> mBooks;
    private int mPosition;

    private Button mDeleteButton;
    private Button mAddButton;

    //private OnFragmentInteractionListener mListener;

    public DetailFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DetailFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DetailFragment newInstance(String param1, String param2) {
        DetailFragment fragment = new DetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        // Get the intent that launched the Activity
        Intent detailLauncher = getActivity().getIntent();
        // Get extra data from intent
        Bundle extra = detailLauncher.getExtras();
        // Check if it has valid value
        if (extra != null) {
            // Get category first
            mCategory = extra.getInt(BookAdapter.CATEGORY);
            // If the category is 0 (Search Activity), it has more extra
            if (mCategory == 0) {
                // Get the list of books and it's position in the list
                mBooks = extra.getParcelableArrayList(BookAdapter.BOOK_LIST);
                mPosition =  extra.getInt(BookAdapter.POSITION);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        // Get the views
        TextView bookTitle = rootView.findViewById(R.id.detail_title);
        TextView bookAuthor = rootView.findViewById(R.id.detail_author);
        TextView isbnTextView = rootView.findViewById(R.id.isbn_nr);
        TextView descriptionTextView = rootView.findViewById(R.id.description);
        ImageView bookCover = rootView.findViewById(R.id.detail_book_cover);

        String picture = null;

        if (mCategory == 0) {
            // Get the selected book Object
            Book book = mBooks.get(mPosition);
            // And get details of that book
            String title = book.getTitle();
            String authors = book.getAuthor();
            // Split authors and separate them with ","
            String splitAuthors = authors.split(",")[0];
            // Replace unnecessary characters from authors
            String cleanAuthors = splitAuthors
                    .replace("[", " ")
                    .replace("]", " ")
                    .replace('"', ' ');
            String isbnNumber = book.getIsbn();
            String description = book.getDescription();
            picture = book.getImageUrl();

            // Set details on views
            bookTitle.setText(title);
            bookAuthor.setText(cleanAuthors);
            isbnTextView.setText(isbnNumber);
            descriptionTextView.setText(description);

            // Set the Book's title as Activity's title
            getActivity().setTitle(title);
        }
        // Load image
        Glide.with(getActivity()).load(picture).placeholder(R.drawable.place_holder).into(bookCover);


        // Get the deletion and addition buttons
        mDeleteButton = rootView.findViewById(R.id.delete_btn);
        mAddButton = rootView.findViewById(R.id.add_btn);

        // call method that set visibility and text of buttons
        handleButtons();

        return rootView;
    }

    /**
     * Launching Detail Activity from different activities
     * will influence the buttons
     */
    private void handleButtons() {
        switch (mCategory) {

            // If it started from Search Activity
            case 0:
                // make delete button disappear
                mDeleteButton.setVisibility(View.GONE);
                // set text on the other button (add to wish list)
                mAddButton.setText(SEARCH_BTN);
                break;

            // If it started from Book List Activity
            case 1:
                // and from Wish List Fragment
                //set text on the other button (mark as unread)
                mAddButton.setText(WISH_LIST_BTN);
                break;
            case 2:
                // and from Unread List Fragment
                //set text on the other button (mark as read)
                mAddButton.setText(UNREAD_LIST_BTN);
                break;
            case 3:
                // and from Read List Fragment
                //set text on the other button (rate)
                mAddButton.setText(READ_LIST_BTN);
                break;
            // set the default text as the same as Wsh List Fragment's
            default:
                mAddButton.setText(WISH_LIST_BTN);
                break;
        }
    }

    /*
    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }*/

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    /*public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }*/
}
