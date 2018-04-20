package com.example.szage.bookpilot.ui;

import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.szage.bookpilot.R;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Instantiate Detail Fragment
        DetailFragment detailFragment = new DetailFragment();
        // Start Fragment Transaction for starting Detail Fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.detail_fragment, detailFragment).commit();
    }
}
