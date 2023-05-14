package org.codeandomexico.mapmap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONObject;

public class RegisterActivity extends Activity {

    private static Intent serviceIntent;

    private CaptureService captureService;

    private final ServiceConnection caputreServiceConnection = new ServiceConnection() {

        public void onServiceDisconnected(ComponentName name) {
            captureService = null;
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            captureService = ((CaptureService.CaptureServiceBinder) service).getService();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        TextView usuarioLabel = (TextView) findViewById(R.id.usuarioLabel);
        usuarioLabel.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/bebasneue_bold.ttf"));

        serviceIntent = new Intent(this, CaptureService.class);
        startService(serviceIntent);

        bindService(serviceIntent, caputreServiceConnection, Context.BIND_AUTO_CREATE);
        CaptureService.boundToService = true;

        @SuppressLint("HardwareIds") View.OnClickListener listener = v -> {

            if (v.getId() == R.id.botonregistro) {
                if (CaptureService.imei == null || CaptureService.imei.length() == 0) {
                    CaptureService.imei = Settings.Secure.getString(getBaseContext().getContentResolver(), Settings.Secure.ANDROID_ID);
                }

                EditText usuarioInputText = (EditText) findViewById(R.id.usuarioInputText);
                final String userName = usuarioInputText.getText().toString().trim();

                RequestParams params = new RequestParams();
                params.put("imei", CaptureService.imei);
                params.put("userName", userName);

                AsyncHttpClient client = new AsyncHttpClient();
                client.setTimeout(10 * 1000);
                client.setUserAgent("tw");
                client.get(CaptureService.URL_BASE + "register", params, new JsonHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        try {
                            Toast.makeText(RegisterActivity.this, "Teléfono Registrado", Toast.LENGTH_SHORT).show();
                            SharedPreferences prefsManager = PreferenceManager.getDefaultSharedPreferences(RegisterActivity.this);
                            prefsManager.edit()
                                    .putBoolean("registered", true)
                                    .putString("unitId", response.getString("unitId"))
                                    .putString("userName", response.getString("userName"))
                                    .apply();
                            Intent uploadIntent = new Intent(RegisterActivity.this, UploadActivity.class);
                            startActivity(uploadIntent);
                            RegisterActivity.this.finish();
                        } catch (Exception e) {
                            Toast.makeText(RegisterActivity.this, "No se ha podido registrar el teléfono", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        Toast.makeText(RegisterActivity.this, "No se ha podido registrar el teléfono", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };

        ImageButton registerButton = (ImageButton) findViewById(R.id.botonregistro);
        registerButton.setOnClickListener(listener);

    }
}
