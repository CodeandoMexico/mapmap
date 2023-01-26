package codeandoxalapa.org.mapmap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class NewActivity extends Activity implements OnClickListener {

    private CaptureService captureService;
    public ImageView img;
    RouteImage routeImage = null;

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
        setContentView(R.layout.activity_new);

        Intent serviceIntent = new Intent(this, CaptureService.class);
        startService(serviceIntent);

        bindService(serviceIntent, caputreServiceConnection, Context.BIND_AUTO_CREATE);

        // Botón continuar
        ImageButton botonContinuar = (ImageButton) findViewById(R.id.botonContinuar);
        botonContinuar.setOnClickListener(this);

        // Crear foto
        ImageButton botonCapturaImagen = (ImageButton) findViewById(R.id.botonCapturaImagen);
        botonCapturaImagen.setOnClickListener(this);
        img = (ImageView) findViewById(R.id.imagenBus);
        routeImage = new RouteImage();

        // Typeface
        Typeface fuenteBebeasNeueBold = Typeface.createFromAsset(getAssets(), "fonts/bebasneue_bold.ttf");

        TextView descripcionLabel = (TextView) findViewById(R.id.descripcionLabel);
        descripcionLabel.setTypeface(fuenteBebeasNeueBold);

        TextView notasLabel = (TextView) findViewById(R.id.notasLabel);
        notasLabel.setTypeface(fuenteBebeasNeueBold);

        TextView tipoVehiculoLabel = (TextView) findViewById(R.id.tipoVehiculoLabel);
        tipoVehiculoLabel.setTypeface(fuenteBebeasNeueBold);

        TextView capacidadVehiculoLabel = (TextView) findViewById(R.id.capacidadVehiculoLabel);
        capacidadVehiculoLabel.setTypeface(fuenteBebeasNeueBold);

        TextView adjuntarFotoLabel = (TextView) findViewById(R.id.adjuntarFotoLabel);
        adjuntarFotoLabel.setTypeface(fuenteBebeasNeueBold);

    }

    @Override
    public void onClick(View v) {
        int id;
        id = v.getId();
        switch (id) {
            case R.id.botonCapturaImagen:

                final CharSequence[] option = {"Tomar Foto", "Escoger desde galería", "Cancelar"};
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Selecciona una");
                builder.setItems(option, (dialog, which) -> {
                    Object[] arr = null;
                    if (option[which] == "Tomar Foto") {
                        arr = routeImage.openCamera();
                        if ((Boolean) arr[0]) {
                            startActivityForResult((Intent) arr[1], (Integer) arr[2]);
                        }

                    } else if (option[which] == "Escoger desde galería") {
                        arr = routeImage.openGallery();
                        startActivityForResult((Intent) arr[1], (Integer) arr[2]);
                    } else {
                        dialog.dismiss();
                    }
                });
                builder.show();
                break;

            case R.id.botonContinuar:
                if (!captureService.capturing)
                    createNewCapture();
                else {
                    updateCapture();
                }
                Intent settingsIntent = new Intent(NewActivity.this, CaptureActivity.class);
                startActivity(settingsIntent);
                break;

            default:
                break;

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            routeImage = routeImage.typeActionPhoto(requestCode, getBaseContext(), data);
            img.setImageBitmap(routeImage.bitmap);
        }
    }

    private void createNewCapture() {
        synchronized (this) {
            EditText routeDescription = (EditText) findViewById(R.id.descripcionInputText);
            EditText fieldNotes = (EditText) findViewById(R.id.notasInputText);
            EditText vehicleCapacity = (EditText) findViewById(R.id.capacidadVehiculoInputText);
            EditText vehicleType = (EditText) findViewById(R.id.tipoVehiculoInputText);

            captureService.newCapture(
                    "",
                    routeDescription.getText().toString(),
                    fieldNotes.getText().toString(),
                    vehicleType.getText().toString(),
                    vehicleCapacity.getText().toString(),
                    routeImage.pathImage);

        }
    }

    private void updateCapture() {

        synchronized (this) {

            // EditText routeName = (EditText) findViewById(R.id.routeName);
            EditText descripcionInputText = (EditText) findViewById(R.id.descripcionInputText);
            EditText notasInputText = (EditText) findViewById(R.id.notasInputText);
            EditText tipoVehiculoInputText = (EditText) findViewById(R.id.tipoVehiculoInputText);
            EditText capacidadVehiculoInputText = (EditText) findViewById(R.id.capacidadVehiculoInputText);

            captureService.currentCapture.setRouteName("");
            captureService.currentCapture.description = descripcionInputText.getText().toString();
            captureService.currentCapture.notes = notasInputText.getText().toString();
            captureService.currentCapture.vehicleType = tipoVehiculoInputText.getText().toString();
            captureService.currentCapture.vehicleCapacity = capacidadVehiculoInputText.getText().toString();
            captureService.currentCapture.path = routeImage.pathImage;
            captureService.currentCapture.imei = captureService.imei;

        }
    }
}
