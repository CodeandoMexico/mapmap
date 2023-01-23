package org.codeandomexico.mapmap;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends Activity {


    public static Boolean registered = false;
    public static Integer unitId = null;
    public static String userName = null;
    public static final String MAPMAP_CHANNEL_ID = "MapMap";

    private SharedPreferences prefsManager = null;

    SharedPreferences.OnSharedPreferenceChangeListener prefListener = (prefs, key) -> {
        if (key.equals("registered")) {
            updateRegistrationData();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkPermissions();

        setContentView(R.layout.activity_main);
        prefsManager = PreferenceManager.getDefaultSharedPreferences(this);
        prefsManager.registerOnSharedPreferenceChangeListener(prefListener);
        updateRegistrationData();

        Typeface bebasNeueBold = Typeface.createFromAsset(getAssets(), "fonts/bebasneue_bold.ttf");
        Typeface bebasNeueRegular = Typeface.createFromAsset(getAssets(), "fonts/bebasneue_regular.ttf");
        TextView UserNameText = findViewById(R.id.UserNameText);
        UserNameText.setTypeface(bebasNeueRegular);
        TextView UnitIdText = findViewById(R.id.UnitIdText);
        UnitIdText.setTypeface(bebasNeueBold);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switch (v.getId()) {

                    case R.id.WandButton:
                        Intent settingsIntent = new Intent(MainActivity.this, NewActivity.class);
                        startActivity(settingsIntent);
                        break;

                    case R.id.ReviewButton:
                        Intent mailIntent = new Intent(MainActivity.this, ReviewActivity.class);
                        startActivity(mailIntent);
                        break;

                    case R.id.UploadButton:
                        if (registered) {
                            Intent uploadIntent = new Intent(MainActivity.this, UploadActivity.class);
                            startActivity(uploadIntent);
                        } else {
                            Intent regiseterIntent = new Intent(MainActivity.this, RegisterActivity.class);
                            startActivity(regiseterIntent);
                        }

                        break;

                    default:
                        break;
                }
            }
        };

        ImageButton wandButton = findViewById(R.id.WandButton);
        wandButton.setOnClickListener(listener);
        ImageButton reviewButton = findViewById(R.id.ReviewButton);
        reviewButton.setOnClickListener(listener);
        ImageButton uploadButton = findViewById(R.id.UploadButton);
        uploadButton.setOnClickListener(listener);
    }

    public void updateRegistrationData() {
        if (prefsManager != null) {
            registered = prefsManager.getBoolean("registered", false);
            TextView userNameText = findViewById(R.id.UserNameText);
            userNameText.setText(prefsManager.getString("userName", ""));
            TextView unitIdText = findViewById(R.id.UnitIdText);
            unitIdText.setText("Unidad: " + prefsManager.getString("unitId", "no registrada"));
        }
    }

    private static final int PERMISSION_REQUEST_CODE = 200;

    private void checkPermissions() {
        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            requestPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.INTERNET) == PackageManager.PERMISSION_DENIED) {
            requestPermission(Manifest.permission.INTERNET);
        }
    }

    private void requestPermission(String permission) {
        ActivityCompat.requestPermissions(this, new String[]{permission}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {


            }
        }
    }


}
