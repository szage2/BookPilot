package com.example.szage.bookpilot.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

import com.example.szage.bookpilot.R;

public class BarcodeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode);

        // Get Buttons of the layout
        Button okButton = findViewById(R.id.ok_button);
        Button againButton = findViewById(R.id.again_button);

        /**
         * Intent launching Search Activity (will be used after navigation solved)
         *
         * TODO: After proper navigation has built, use below intent in an OnClickListener
         * Intent searchIntent = new Intent
         * (BarcodeActivity.this, SearchActivity.class);
         * startActivity(searchIntent);
         */
    }
}
