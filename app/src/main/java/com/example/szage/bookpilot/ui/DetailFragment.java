package com.example.szage.bookpilot.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.szage.bookpilot.R;

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

    private boolean mIsSearchDetail;
    private int mCategory;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        Log.i("DetailFragment ", "is started");

        // Get the deletion button
        Button deleteButton= rootView.findViewById(R.id.delete_btn);
        // Get the addition button
        Button addButton = rootView.findViewById(R.id.add_btn);

        /*
        * Launching Detail Activity from different activities
        * will influence the buttons
        *
        * If it started from Search Activity
        */
        if (mIsSearchDetail) {
            // make delete button disappear
            deleteButton.setVisibility(View.GONE);
            // set text on the other button (add to wish list)
            addButton.setText(SEARCH_BTN);
        } else {
            // If it started from Book List Activity
            switch (mCategory) {
                case 0:
                    // and from Wish List Fragment
                    //set text on the other button (mark as unread)
                    addButton.setText(WISH_LIST_BTN);
                    break;
                case 1:
                    // and from Unread List Fragment
                    //set text on the other button (mark as read)
                    addButton.setText(UNREAD_LIST_BTN);
                    break;
                case 2:
                    // and from Read List Fragment
                    //set text on the other button (rate)
                    addButton.setText(READ_LIST_BTN);
                    break;
                    // set the default text as the same as Wsh List Fragment's
                default: addButton.setText(WISH_LIST_BTN);
            }
        }
        return rootView;
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
