package org.codeandomexico.mapmap;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class MainActivity extends Activity {

    public static Boolean registered = false;
    private SharedPreferences prefsManager = null;

    SharedPreferences.OnSharedPreferenceChangeListener prefListener = (prefs, key) -> {
        if (key.equals("registered")) {
            updateRegistrationData();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefsManager = PreferenceManager.getDefaultSharedPreferences(this);
        prefsManager.registerOnSharedPreferenceChangeListener(prefListener);
        updateRegistrationData();

        View.OnClickListener listener = v -> {
            if (v.getId() == R.id.botonTrazar) {
                Intent settingsIntent = new Intent(MainActivity.this, NewActivity.class);
                startActivity(settingsIntent);
            } else if (v.getId() == R.id.botonRecorridos) {
                Intent mailIntent = new Intent(MainActivity.this, ReviewActivity.class);
                startActivity(mailIntent);
            } else if (v.getId() == R.id.botonEnviar) {
                if (registered) {
                    Intent uploadIntent = new Intent(MainActivity.this, UploadActivity.class);
                    startActivity(uploadIntent);
                } else {
                    Intent regiseterIntent = new Intent(MainActivity.this, RegisterActivity.class);
                    startActivity(regiseterIntent);
                }
            }
        };

        ImageButton wandButton = (ImageButton) findViewById(R.id.botonTrazar);
        wandButton.setOnClickListener(listener);

        ImageButton reviewButton = (ImageButton) findViewById(R.id.botonRecorridos);
        reviewButton.setOnClickListener(listener);

        ImageButton uploadButton = (ImageButton) findViewById(R.id.botonEnviar);
        uploadButton.setOnClickListener(listener);

        Typeface fuenteBebeasNeueBold = Typeface.createFromAsset(getAssets(), "fonts/bebasneue_bold.ttf");
        TextView usuarioLabel = (TextView) findViewById(R.id.usuarioLabel);
        usuarioLabel.setTypeface(fuenteBebeasNeueBold);
        TextView unitIdLabel = (TextView) findViewById(R.id.unitIdLabel);
        unitIdLabel.setTypeface(fuenteBebeasNeueBold);
    }


    public void updateRegistrationData() {
        if (prefsManager != null) {
            registered = prefsManager.getBoolean("registered", false);
            TextView usuarioLabel = (TextView) findViewById(R.id.usuarioLabel);
            usuarioLabel.setText(prefsManager.getString("userName", ""));
            TextView unitIdLabel = (TextView) findViewById(R.id.unitIdLabel);
            unitIdLabel.setText(String.format("Unidad %s", prefsManager.getString("unitId", "no registrada")));
        }
    }

}
