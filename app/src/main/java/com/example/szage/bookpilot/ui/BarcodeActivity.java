package com.example.szage.bookpilot.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.szage.bookpilot.R;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

public class BarcodeActivity extends AppCompatActivity {

    private static final String LOG_TAG = BarcodeActivity.class.getSimpleName();

    private BarcodeDetector mDetector;
    private SurfaceView mCameraView;
    private CameraSource mCameraSource;
    private Barcode mBarcode;
    private TextView readBarcodeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode);

        // Get Buttons of the layout
        Button okButton = findViewById(R.id.ok_button);
        Button againButton = findViewById(R.id.again_button);

        // Get the surface
        mCameraView = findViewById(R.id.surfaceView);

        // Setting up the mBarcode detector
        mDetector = new BarcodeDetector
                // Identify the code format
                //.setBarcodeFormats(Barcode.DATA_MATRIX | Barcode.ISBN)
                .Builder(getApplicationContext())
                .setBarcodeFormats(Barcode.ALL_FORMATS)
                .build();

        // Check if mBarcode detector is operational
        if (!mDetector.isOperational()) {
            Log.i(LOG_TAG, String.valueOf(R.string.barcode_decoder_error));
        }

        // Display camera preview
        getCameraSource();

        // Set click listener On Ok button
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start Search Activity
                Intent searchIntent = new Intent
                        (BarcodeActivity.this, SearchActivity.class);
                // Get the string value of the barcode
                String barcodeString = String.valueOf(readBarcodeText.getText());
                // Attach it to the intent
                searchIntent.putExtra(String.valueOf(R.string.barcode), barcodeString);
                startActivity(searchIntent);
            }
        });

        // Set click listener On Ok button
        againButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // make the camera view visible
                mCameraView.setVisibility(View.VISIBLE);
                readBarcodeText.setText("");
                // Retry to read barcode
                getCameraSource();
            }
        });
    }

    /**
     *  Creating camera preview
     */
    private void getCameraSource() {

        // Get camera preview with camera source
        mCameraSource = new CameraSource
                .Builder(BarcodeActivity.this, mDetector)
                .setRequestedPreviewSize(1600, 1024)
                .setAutoFocusEnabled(true)
                .build();

        // Add callback to the surface holder
        mCameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                try {
                    if (ActivityCompat.checkSelfPermission(BarcodeActivity.this,
                            Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        // Request permission
                        ActivityCompat.requestPermissions(BarcodeActivity.this,
                                new String[]{Manifest.permission.CAMERA},
                                33);
                        return;
                    }
                    // Start drawing the preview frames
                    mCameraSource.start(mCameraView.getHolder());
                    Log.i(LOG_TAG, String.valueOf(R.string.camera_source_starts));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            // Stop the preview
            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                Log.i(LOG_TAG, String.valueOf(R.string.camera_source_destroyed));
                mCameraSource.stop();
            }
        });

        // Set processor on the mBarcode detector
        mDetector.setProcessor(new Detector.Processor<Barcode>() {

            // Release processor
            @Override
            public void release() {
                Log.i(LOG_TAG, String.valueOf(R.string.barcode_processor));
            }

            // Get the mBarcode from the preview captured
            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                //finish();
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();

                // In case any mBarcode found
                if (barcodes.size() != 0) {
                    // get the very first
                    mBarcode = barcodes.valueAt(0);

                    if (mBarcode != null) {
                        // make it display on the text view
                        readBarcodeText = findViewById(R.id.read_barcode);
                        // make it display on the text view
                        readBarcodeText.post(new Runnable() {
                            @Override
                            public void run() {
                                readBarcodeText.setText(mBarcode.displayValue);
                                // Stop working the camera source and hide it's view
                                //mCameraSource.stop();
                                //mCameraView.setVisibility(View.GONE);
                                barcodes.clear();
                            }
                        });
                    }
                }
            }
        });
    }

    /**
     * Once activity is destroyed
     */
    @Override
    protected void onDestroy() {
        // Release the camera source and the mBarcode detector
        mCameraSource.release();
        mDetector.release();
        super.onDestroy();
    }

    /**
     * Gets the result of request permission
     *
     * @param requestCode is the code of the permission request
     * @param permissions is requested permissions
     * @param grantResults whether it's granted or denied
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            // In case of camera permission request
            case 33:
                // If permission granted
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Log it
                    Log.i(LOG_TAG, String.valueOf(R.string.permission_granted));
                } else {
                    // Otherwise log that permission is denied
                    Log.i(LOG_TAG, String.valueOf(R.string.permission_denied));
                    // Inform the user with a toast message
                    Toast.makeText(this, String.valueOf(R.string.permission_denied), Toast.LENGTH_SHORT).show();
                }
                break;
            default: super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
