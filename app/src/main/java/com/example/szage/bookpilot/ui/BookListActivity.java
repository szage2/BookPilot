package com.example.szage.bookpilot.ui;

import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.szage.bookpilot.R;

public class BookListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list);

        // Instantiate Wish List Fragment
        WishListFragment wishListFragment = new WishListFragment();
        // Make Wish List Fragment as default Fragment by starting fragment transaction
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.fragment_content, wishListFragment).commit();

        // Detect the type of device: phone or tablet
        boolean isTwoPanes = getResources().getBoolean(R.bool.has_two_panes);

        if (isTwoPanes) {
            // If it's a tablet, instantiate Detail Fragment
            DetailFragment detailFragment = new DetailFragment();
            // Start fragment transaction and launch Detail Fragment
            fragmentManager.beginTransaction().replace(R.id.detail_fragment, detailFragment).commit();
        }

        /**
         * Intents launching activities
         *
         * TODO: Build proper navigation and use below intents
         *
         * Intent searchIntent = new Intent
         * BookListActivity.this, SearchActivity.class);
         * startActivity(searchIntent);
         *
         * Intent barcodeIntent = new Intent
         * (BookListActivity.this, BarcodeActivity.class);
         * startActivity(barcodeIntent);
         *
         * Intent storeIntent = new Intent
         * (BookListActivity.this, StoreFinderActivity.class);
         * startActivity(storeIntent);
         */
    }
}
