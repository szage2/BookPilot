package com.example.szage.bookpilot.ui;

import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.example.szage.bookpilot.R;
import com.example.szage.bookpilot.data.BookContract;
import com.example.szage.bookpilot.widget.BookWidgetProvider;

import static com.example.szage.bookpilot.widget.BookWidgetProvider.ACTION_UPDATE;

public class BookListActivity extends AppCompatActivity implements WishListFragment.OnNewItemSelectedListener,
        DetailFragment.OnItemRemovedFromCategoryListener, UnreadListFragment.OnNewItemSelectedListener2,
        ReadListFragment.OnNewItemSelectedListener3 {

    NavigationView mNavigationView;
    private DrawerLayout mDrawer;
    private boolean mIsTwoPanes;
    private static int mPosition;
    private static int mCategory;
    private static String mStringBookUri;
    private DetailFragment mDetailFragment;
    private long firstItemsId;
    private boolean isItemRemoved;
    private Bundle detailBundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list);

        // Get the Drawer Layout
        mDrawer = findViewById(R.id.drawer_layout);
        // Get the Navigation View
        mNavigationView = findViewById(R.id.nav_view);
        // Set up the Drawer by calling method (passing the Navigation view as the argument)
        setUpDrawerContent(mNavigationView);

        // Make Wish List Fragment as default Fragment
        // Create an instance of it
        WishListFragment wishListFragment = new WishListFragment();
        // Begin the fragment transaction by calling general method
        beginTheFragmentTransaction(wishListFragment);

        // Get wish list item of the menu and get it's title
        String defaultTitle = getResources().getString(R.string.menu_item_wish_list);
        // Set it as title of the Activity
        setTitle(defaultTitle);

        // Detect the type of device: phone or tablet
        mIsTwoPanes = getResources().getBoolean(R.bool.has_two_panes);

        // In tablet layout
        if (mIsTwoPanes) {
            setUpLayoutForTwoPanes();
        }
    }

    /**
     * Setting up Navigation Drawer and Detail Fragment
     * to fit tablet layout
     */
    private void setUpLayoutForTwoPanes() {
        // If it's a tablet, instantiate Detail Fragment
        mDetailFragment = new DetailFragment();
        // Get the Fragment Manager
        FragmentManager fragmentManager = getSupportFragmentManager();
        // Start fragment transaction and launch Detail Fragment
        fragmentManager.beginTransaction().replace(R.id.detail_fragment, mDetailFragment).commit();
        // Keep the drawer menu open, even if an item is being selected
        mDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN, mNavigationView);
        // Make drawer's background transparent, fragments will be visible
        mDrawer.setScrimColor(0x00000000);
        if (mIsTwoPanes) {
            sendDataToDetailFragment();
        }
    }

    /**
     * @param item selected options item
     * @return option is selected / not
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawer.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Setting up the Navigation Drawer's content
     *
     * @param navigationView is container view for drawer items
     */
    private void setUpDrawerContent(NavigationView navigationView) {

        // Set a Listener on Navigation View
        navigationView.setNavigationItemSelectedListener
                (new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // method call
                selectDrawerItem(item);
                return true;
            }
        });
    }

    /**
     * Identifies the selected item of the Navigation Drawer menu
     * and launches selected fragment or activity
     *
     * @param item that is being selected in the drawer menu
     */
    private void selectDrawerItem(MenuItem item) {

        boolean isFragment;
        Fragment fragment = null;
        Class fragmentClass = null;
        Class activityClass = null;

        // Check which view is being selected
        switch (item.getItemId()) {
            case R.id.menu_main_wish_list:
                // Fragment class is WishListFragment
                fragmentClass = WishListFragment.class;
                // It's a Fragment
                isFragment = true;
                break;
            case R.id.menu_main_unread:
                // Fragment class is UnreadListFragment
                fragmentClass = UnreadListFragment.class;
                // It's a Fragment
                isFragment = true;
                break;
            case R.id.menu_main_read:
                // Fragment class is ReadListFragment
                fragmentClass = ReadListFragment.class;
                // It's a Fragment
                isFragment = true;
                break;
            case R.id.menu_main_nav_book:
                // Activity class is SearchActivity
                activityClass = SearchActivity.class;
                // It's an activity
                isFragment = false;
                break;
            case R.id.menu_main_nav_barcode:
                // Activity class is BarcodeActivity
                activityClass = BarcodeActivity.class;
                // It's an activity
                isFragment = false;
                break;
            case R.id.menu_main_nav_store:
                // Activity class is StoreFinderActivity
                activityClass = StoreFinderActivity.class;
                // It's an activity
                isFragment = false;
                break;
            default:
                // Default class is WishListFragment
                fragmentClass = WishListFragment.class;
                // It's a fragment
                isFragment = true;
                setTitle(item.getTitle());
                break;
        }

        // Check whether selected menu item is an activity or a fragment
        if (!isFragment) {
            // If it's an activity launch it
            launchSelectedActivity(activityClass);
        } else {
            // If it's a Fragment
            try {
                // Create a new Instance of the selected fragment;
                fragment = (Fragment) fragmentClass.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
            // Start fragment transaction
            beginTheFragmentTransaction(fragment);
        }

        // Set the selected item checked in the menu
        item.setChecked(true);
        // Get the selected item's title and set it as Activity's title
        setTitle(item.getTitle());
        // In case user run the app on mobile
        if (!mIsTwoPanes) {
            // Close the drawer to make whole selected fragment visible
            mDrawer.closeDrawers();
        }
    }

    /**
     * General method that starts fragment transaction,
     * replacing fragment_content View
     *
     * @param fragment is the selected category (fragment) in the menu
     */
    private void beginTheFragmentTransaction(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.fragment_content, fragment).commit();
    }

    /**
     * General method that starts activity
     *
     * @param activity is the selected item (activity) in the menu
     */
    private void launchSelectedActivity(Class<?> activity) {
        Intent activityIntent = new Intent(BookListActivity.this, activity);
        startActivity(activityIntent);
    }

    /**
     * Method that passes arguments to Detail Fragment
     * once a book is being selected
     */
    private void sendDataToDetailFragment() {
        // Create detail bundle
        detailBundle = new Bundle();
        // put category and position of selected item in the bundle
        detailBundle.putInt(String.valueOf(R.string.category), mCategory);
        detailBundle.putInt(String.valueOf(R.string.position), mPosition);
        detailBundle.putLong("itemId", firstItemsId);
        // if uri is null
        if (mStringBookUri == null) {
            // Create a default uri with id of 0
            mStringBookUri = String.valueOf(ContentUris.withAppendedId(BookContract.BookEntry.CONTENT_URI, 0));
        }
        // add this uri to the bundle
        detailBundle.putString(String.valueOf(R.string.current_uri), mStringBookUri);
        // If the item removed from the list
        if (isItemRemoved) {
            // Change the uri using the first item's id
            mStringBookUri = String.valueOf(ContentUris.withAppendedId(BookContract.BookEntry.CONTENT_URI, firstItemsId));
        }
        // Set the arguments on the fragment
        mDetailFragment.setArguments(detailBundle);

        // set isItemRemoved to false
        isItemRemoved = false;
    }

    /**
     * Returns selected item's position, uti and first item's id in list,
     * that makes it possible to update Detail Fragment
     *
     * @param position is the position of the selected item in the wish list
     */
    @Override
    public void OnNewItemClicked(int position, Uri uri, long itemsId) {
        mStringBookUri = String.valueOf(uri);
        // Pass these variables to Detail Fragment
        // If it's tablet mode and book's uri is not null
        if (mIsTwoPanes && mStringBookUri != null) {
            // Get the position
            mPosition = position;
            // Make category 1
            mCategory = 1;
            // Get the first item's id in the list
            firstItemsId = itemsId;
            // Method call
            makeDetailFragmentUpdate();
        }
    }

    /**
     * In tablet mode updates detail fragment
     * once an item is being removed from current list
     *
     * @param category is current book category
     */
    @Override
    public void OnItemRemovedFromList(int category) {
        // If it's tablet mode
        if (mIsTwoPanes) {
            // Get the category
            mCategory = category;
            // Set isItemRemoved to true as item has been deleted or it's category's changed
            isItemRemoved = true;
            // Method call
            makeDetailFragmentUpdate();
        }
        Log.i("BookListActivity", "category is " + mCategory);
        // If the category equals 1
        if (category == 1) {
            // Create an intent to BookWidgetProvider class
            Intent updateWidget = new Intent(BookListActivity.this, BookWidgetProvider.class);
            // Set an action on the intent
            updateWidget.setAction(ACTION_UPDATE);
            // Broadcast it to the widget
            sendBroadcast(updateWidget);
            Log.i("BookListActivity", "updating widget");
        }
    }

    /**
     *  Updates Detail fragment by passing a bundle
     */
    private void makeDetailFragmentUpdate() {
        // call method
        sendDataToDetailFragment();
        // Create bundle
        detailBundle = new Bundle();
        // add the uri to this bundle
        detailBundle.putString(String.valueOf(R.string.current_uri), mStringBookUri);
        // If detail fragments is already created
        if ( mDetailFragment == null) {
            // Find it by it's id
            mDetailFragment = (DetailFragment)
                    getSupportFragmentManager().findFragmentById(R.id.detail_fragment);
            // And call method that updates it
        } mDetailFragment.updateDetailFragment(detailBundle);
    }
}