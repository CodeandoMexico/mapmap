package org.codeandomexico.mapmap;

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

        TextView txt = findViewById(R.id.userNameText);
        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/bebasneue_bold.ttf");
        txt.setTypeface(font);

        Intent serviceIntent = new Intent(this, CaptureService.class);
        startService(serviceIntent);
        bindService(serviceIntent, caputreServiceConnection, Context.BIND_AUTO_CREATE);
        CaptureService.boundToService = true;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (v.getId() == R.id.RegisterButton) {
                    EditText userNameET = findViewById(R.id.userNameTextBox);
                    final String userName = userNameET.getText().toString().trim();

                    RequestParams params = new RequestParams();
                    params.put("imei", CaptureService.imei);
                    params.put("userName", userName);

                    AsyncHttpClient client = new AsyncHttpClient();
                    client.setUserAgent("tw");
                    String registerURL = CaptureService.URL_BASE + "register";
                    client.get(registerURL, params, new JsonHttpResponseHandler() {

                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            try {
                                String unitId = response.getString("unitId");
                                Toast.makeText(RegisterActivity.this, "Teléfono Registrado", Toast.LENGTH_SHORT).show();
                                SharedPreferences prefsManager = PreferenceManager.getDefaultSharedPreferences(RegisterActivity.this);
                                prefsManager.edit().putBoolean("registered", true).putString("unitId", unitId).putString("userName", userName).apply();
                                Intent uploadIntent = new Intent(RegisterActivity.this, UploadActivity.class);
                                startActivity(uploadIntent);
                                RegisterActivity.this.finish();
                            } catch (Exception e) {
                                Toast.makeText(RegisterActivity.this, "No se ha podido registrar el teléfono", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            Toast.makeText(RegisterActivity.this, "No se ha podido registrar el teléfono", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        };

        ImageButton registerButton = findViewById(R.id.RegisterButton);
        registerButton.setOnClickListener(listener);

    }
}
