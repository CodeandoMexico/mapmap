package org.codeandomexico.mapmap;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.text.DecimalFormat;

import org.codeandomexico.mapmap.protobuffer.TransitWandProtos.Upload;

public class UploadActivity extends Activity {

    private int _contadorRutasEnviadasOk = 0;
    private int _contadorRutasEnviadasNok = 0;
    private int _contadorRutasEnviadas = 0;
    private int _contadorRutasTotales = 0;
    private String _phoneId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        // Si no hay rutas, mostramos un mensaje y regresamos a la pantalla anterior
        if (RouteCapture.checkNumberListFiles(UploadActivity.this) == 0) {
            Toast.makeText(UploadActivity.this, "No hay rutas en el teléfono pendientes de enviar.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        final FilenameFilter fileNameFilter = RouteCapture.getFileNameFilterRoute();
        final File[] routeFiles = getFilesDir().listFiles(fileNameFilter);
        _contadorRutasTotales = routeFiles.length;

        // Obtenemos el Phone ID, que puede ser el IMEI o el UnitID
        if (CaptureService.imei == null || CaptureService.imei.length() == 0) {
            CaptureService.imei = Settings.Secure.getString(getBaseContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        }
        _phoneId = CaptureService.imei;
        if(_phoneId == null) {
            SharedPreferences _prefsManager = PreferenceManager.getDefaultSharedPreferences(this);
            if(_prefsManager != null && _prefsManager.getBoolean("registered", false)) {
                _phoneId = _prefsManager.getString("unitId", "no registrada");
            }
        }

        // Campo con la etiqueta Rutas
        TextView textViewRutas = findViewById(R.id.textView2);
        textViewRutas.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/bebasneue_bold.ttf"));
        // Campo con la etiqueta Tamaño
        TextView textViewTamanyo = findViewById(R.id.textView3);
        textViewTamanyo.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/bebasneue_bold.ttf"));
        // Campo con el texto del número de rutas
        TextView textViewRouteCount = findViewById(R.id.routeCountText);
        textViewRouteCount.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/bebasneue_bold.ttf"));
        textViewRouteCount.setText(String.valueOf(routeFiles.length));
        // Campo con el tamaño total de las rutas
        TextView textViewDataSize = findViewById(R.id.dataSizeText);
        textViewDataSize.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/bebasneue_bold.ttf"));
        textViewDataSize.setText(formatedDataSize(routeFiles));


        View.OnClickListener listener = v -> {
            if (R.id.uploadButton == v.getId()) {
                // Ocultamos el botón de subir ficheros
                ImageButton uploadButton = findViewById(R.id.uploadButton);
                uploadButton.setVisibility(View.GONE);
                // Mostramos el icono giratorio de "En progreso".
                ProgressBar progressSpinner = findViewById(R.id.progressBar);
                progressSpinner.setVisibility(View.VISIBLE);

                // Vamos a enviar cada ruta de manera individual
                for (File routeFile : routeFiles) {
                    Upload.Builder uploadBuilder = Upload.newBuilder();
                    uploadBuilder.setUnitId(0L);
                    uploadBuilder.setUploadId(0);
                    try {
                        DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(routeFile)));
                        Upload.Route pbRouteData = Upload.Route.parseDelimitedFrom(dataInputStream);
                        dataInputStream.close();
                        uploadBuilder.addRoute(pbRouteData);
                        ByteArrayInputStream dataStream = new ByteArrayInputStream(uploadBuilder.build().toByteArray());

                        // Inicializamos los parametros de la petición
                        RequestParams params = new RequestParams();
                        params.put("imei", _phoneId);
                        params.put("data", dataStream);

                        AsyncHttpClient client = new AsyncHttpClient();
                        client.setTimeout(10 * 1000); // 10 * 1000 ms = 10 segundos
                        client.setUserAgent("tw");
                        client.post(CaptureService.URL_BASE + "upload", params, new AsyncHttpResponseHandler() {

                            @Override
                            public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
                                _contadorRutasEnviadasOk++;
                                _contadorRutasEnviadas++;
                                Log.d("UploadActivity", "-RUTA-----------------------------");
                                Log.d("UploadActivity", "Ruta enviada: " + pbRouteData.getRouteName());
                                Log.d("UploadActivity", "\tDescripción: " + pbRouteData.getRouteDescription());
                                Log.d("UploadActivity", "\tNotas: " + pbRouteData.getRouteNotes());
                                deleteRouteFile(routeFile);
                                if(_contadorRutasEnviadas >= _contadorRutasTotales) {
                                    terminarActividad();
                                }
                            }

                            @Override
                            public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
                                _contadorRutasEnviadasNok++;
                                _contadorRutasEnviadas++;
                                Log.e("UploadActivity", "-RUTA-----------------------------");
                                Log.e("UploadActivity", "Error al subir la ruta: " + pbRouteData.getRouteName());
                                Log.e("UploadActivity", "\tDescripción: " + pbRouteData.getRouteDescription());
                                Log.e("UploadActivity", "\tNotas: " + pbRouteData.getRouteNotes());
                                if(_contadorRutasEnviadas >= _contadorRutasTotales) {
                                    terminarActividad();
                                }
                            }
                        });
                    } catch (Exception e) {
                        _contadorRutasEnviadasNok++;
                        Log.e("UploadActivity", "Excepción: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        };

        ImageButton uploadButton = findViewById(R.id.uploadButton);
        uploadButton.setOnClickListener(listener);
    }

    private String formatedDataSize(File[] routeFiles) {
        long dataSize = 0L;
        for (File routeFile : routeFiles) {
            dataSize += routeFile.length();
        }
        DecimalFormat distanceFormat = new DecimalFormat("#,##0.00");
        if (dataSize / 1024 / 1024 > 1) {
            return distanceFormat.format(dataSize / 1024 / 1024) + "Mb";
        }
        return distanceFormat.format(dataSize / 1024) + "Kb";
    }

    private void deleteRouteFile(File routeFile) {
        if (routeFile.exists()) {
            if (routeFile.delete()) {
                Log.i("UploadActivity", "Ruta borrada");
            } else {
                Log.i("UploadActivity", "Ruta no se pudo borrar");
            }
        }
    }

    private void terminarActividad() {
        if (_contadorRutasEnviadasOk > 0) {
            Toast.makeText(UploadActivity.this, "Se enviaron correctamente " + _contadorRutasEnviadasOk + " ruta/s", Toast.LENGTH_LONG).show();
        }
        if (_contadorRutasEnviadasNok > 0) {
            Toast.makeText(UploadActivity.this, "No se pudieron enviar " + _contadorRutasEnviadasNok + " ruta/s", Toast.LENGTH_LONG).show();
        }
        ProgressBar progressSpinner = findViewById(R.id.progressBar);
        progressSpinner.setVisibility(View.GONE);
        ImageButton uploadButton = findViewById(R.id.uploadButton);
        uploadButton.setVisibility(View.VISIBLE);
        UploadActivity.this.finish();
    }

}