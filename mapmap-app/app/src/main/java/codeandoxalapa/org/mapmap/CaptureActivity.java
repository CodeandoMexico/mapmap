package codeandoxalapa.org.mapmap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.os.Vibrator;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;

public class CaptureActivity extends Activity implements ICaptureActivity {

    private static Intent serviceIntent;
    private CaptureService captureService;

    private Vibrator vibratorService;

    private static final String LOGTAG = "LogsAndroid";
    private CheckBox checkStopValidator;

    private final ServiceConnection caputreServiceConnection = new ServiceConnection() {

        public void onServiceDisconnected(ComponentName name) {
            captureService = null;
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
            captureService = ((CaptureService.CaptureServiceBinder) service).getService();
            captureService.setCaptureActivity(CaptureActivity.this);
            initButtons();
            updateRouteName();
            updateDistance();
            updateDuration();
            updateStopCount();
            updatePassengerCountDisplay();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);
        serviceIntent = new Intent(this, CaptureService.class);
        startService(serviceIntent);

        bindService(serviceIntent, caputreServiceConnection, Context.BIND_AUTO_CREATE);
        CaptureService.boundToService = true;

        vibratorService = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);

        Typeface fontBebasNeueBold = Typeface.createFromAsset(getAssets(), "fonts/bebasneue_bold.ttf");
        Typeface fontBebasNeue = Typeface.createFromAsset(getAssets(), "fonts/bebasneue_regular.ttf");

        TextView nombreRutaLabel = (TextView) findViewById(R.id.nombreRutaLabel);
        nombreRutaLabel.setTypeface(fontBebasNeueBold);
        TextView estatusGpsLabel = (TextView) findViewById(R.id.estatusGpsLabel);
        estatusGpsLabel.setTypeface(fontBebasNeueBold);

        TextView duracionLabel = (TextView) findViewById(R.id.duracionLabel);
        duracionLabel.setTypeface(fontBebasNeueBold);
        TextView cronometroDuracion = (TextView) findViewById(R.id.cronometroDuracion);
        cronometroDuracion.setTypeface(fontBebasNeue);

        TextView distanciaLabel = (TextView) findViewById(R.id.distanciaLabel);
        distanciaLabel.setTypeface(fontBebasNeueBold);
        TextView distanciaText = (TextView) findViewById(R.id.distanciaText);
        distanciaText.setTypeface(fontBebasNeue);

        TextView paradaLabel = (TextView) findViewById(R.id.paradaLabel);
        paradaLabel.setTypeface(fontBebasNeueBold);
        TextView contadorParadasText = (TextView) findViewById(R.id.contadorParadasText);
        contadorParadasText.setTypeface(fontBebasNeue);

        TextView marcarParadaLabel = (TextView) findViewById(R.id.marcarParadaLabel);
        marcarParadaLabel.setTypeface(fontBebasNeueBold);
        TextView paradaNoSenalizadaLabel = (TextView) findViewById(R.id.paradaNoSenalizadaLabel);
        paradaNoSenalizadaLabel.setTypeface(fontBebasNeueBold);
        TextView pasajerosLabel = (TextView) findViewById(R.id.pasajerosLabel);
        pasajerosLabel.setTypeface(fontBebasNeueBold);

        TextView subieronPasajerosText = (TextView) findViewById(R.id.subieronPasajerosText);
        subieronPasajerosText.setTypeface(fontBebasNeueBold);
        TextView totalPasajerosText = (TextView) findViewById(R.id.totalPasajerosText);
        totalPasajerosText.setTypeface(fontBebasNeueBold);
        TextView bajaronpasajerosText = (TextView) findViewById(R.id.bajaronpasajerosText);
        bajaronpasajerosText.setTypeface(fontBebasNeueBold);

        // setup button listeners
        View.OnClickListener listener = v -> {

            switch (v.getId()) {

                case R.id.botonIniciarCaptura:
                    if (!captureService.capturing) {
                        startCapture();
                    }
                    break;

                case R.id.botonFinalizarCaptura:
                    if (captureService.capturing) {
                        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    stopCapture();
                                    break;
                                case DialogInterface.BUTTON_NEGATIVE:
                                    //No button clicked
                                    break;
                            }
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(CaptureActivity.this);
                        builder.setMessage("¿Quieres finalizar el mapeo de esta ruta?")
                                .setPositiveButton("Sí", dialogClickListener)
                                .setNegativeButton("No", dialogClickListener).show();
                    }
                    break;
                case R.id.botonMarcarParada:
                    transitStop();
                    break;
                case R.id.checkParadaNoSenalizada:
                    checkStopSignal();
                    break;
                case R.id.botonSubieronPasajeros:
                    passengerBoard();
                    break;
                case R.id.botonBajaronPasajeros:
                    passengerAlight();
                    break;
                default:
                    break;
            }
        };


        checkStopValidator = (CheckBox) findViewById(R.id.checkParadaNoSenalizada);
        checkStopValidator.setOnClickListener(listener);
        checkStopValidator.setEnabled(false);

        ImageButton botonIniciarCaptura = (ImageButton) findViewById(R.id.botonIniciarCaptura);
        botonIniciarCaptura.setOnClickListener(listener);

        ImageButton botonFinalizarCaptura = (ImageButton) findViewById(R.id.botonFinalizarCaptura);
        botonFinalizarCaptura.setOnClickListener(listener);

        ImageButton botonMarcarParada = (ImageButton) findViewById(R.id.botonMarcarParada);
        botonMarcarParada.setOnClickListener(listener);

        ImageButton botonSubieronPasajeros = (ImageButton) findViewById(R.id.botonSubieronPasajeros);
        botonSubieronPasajeros.setOnClickListener(listener);
        ImageButton botonBajaronPasajeros = (ImageButton) findViewById(R.id.botonBajaronPasajeros);
        botonBajaronPasajeros.setOnClickListener(listener);

        //ImageView passengerImageView = (ImageView) findViewById(R.id.passengerImageView);
        //passengerImageView.setAlpha(128);

        initButtons();

        updateRouteName();
        updateDistance();
        updateStopCount();
        updatePassengerCountDisplay();

    }


    private void initButtons() {
        if (captureService == null) {
            ImageButton botonIniciarCaptura = (ImageButton) findViewById(R.id.botonIniciarCaptura);
            botonIniciarCaptura.setImageResource(R.drawable.start_button);
            ImageButton botonFinalizarCaptura = (ImageButton) findViewById(R.id.botonFinalizarCaptura);
            botonFinalizarCaptura.setImageResource(R.drawable.stop_button_gray);
        } else if (captureService.capturing) {
            ImageButton botonIniciarCaptura = (ImageButton) findViewById(R.id.botonIniciarCaptura);
            botonIniciarCaptura.setImageResource(R.drawable.start_button_gray);
            ImageButton botonFinalizarCaptura = (ImageButton) findViewById(R.id.botonFinalizarCaptura);
            botonFinalizarCaptura.setImageResource(R.drawable.stop_button);
        } else if (!captureService.capturing && captureService.currentCapture == null) {
            ImageButton botonIniciarCaptura = (ImageButton) findViewById(R.id.botonIniciarCaptura);
            botonIniciarCaptura.setImageResource(R.drawable.start_button_gray);
            ImageButton botonFinalizarCaptura = (ImageButton) findViewById(R.id.botonFinalizarCaptura);
            botonFinalizarCaptura.setImageResource(R.drawable.stop_button_gray);
        }
        if (!captureService.capturing && captureService.currentCapture == null && captureService.atStop()) {
            ImageButton botonMarcarParada = (ImageButton) findViewById(R.id.botonMarcarParada);
            botonMarcarParada.setImageResource(R.drawable.transit_stop_button_red);
        }
    }

    private void startCapture() {

        if (captureService != null) {
            try {
                captureService.startCapture();
            } catch (NoCurrentCaptureException e) {
                Intent settingsIntent = new Intent(CaptureActivity.this, NewActivity.class);
                startActivity(settingsIntent);
                return;
            }
            vibratorService.vibrate(100);
            Toast.makeText(CaptureActivity.this, "Iniciando trazado", Toast.LENGTH_SHORT).show();
            ((Chronometer) findViewById(R.id.cronometroDuracion)).setBase(SystemClock.elapsedRealtime());
            ((Chronometer) findViewById(R.id.cronometroDuracion)).start();
            initButtons();
        }
    }

    private void stopCapture() {
        if (captureService != null) {
            vibratorService.vibrate(100);
            if (captureService.currentCapture.points.size() > 0) {
                Toast.makeText(CaptureActivity.this, "Trazado completo", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(CaptureActivity.this, "No se generó ningún dato", Toast.LENGTH_SHORT).show();
            }
            captureService.stopCapture();
        }
        ((Chronometer) findViewById(R.id.cronometroDuracion)).stop();
        initButtons();
        Intent finishCaptureIntent = new Intent(CaptureActivity.this, MainActivity.class);
        finishCaptureIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(finishCaptureIntent);
    }

    private void passengerAlight() {
        if (captureService != null && captureService.capturing) {
            if (captureService.currentCapture.totalPassengerCount == 0) {
                return;
            }
            vibratorService.vibrate(5);
            captureService.currentCapture.totalPassengerCount--;
            captureService.currentCapture.alightCount++;
            updatePassengerCountDisplay();
        }
    }

    private void passengerBoard() {
        if (captureService != null && captureService.capturing) {
            captureService.currentCapture.totalPassengerCount++;
            vibratorService.vibrate(5);
            captureService.currentCapture.boardCount++;
            updatePassengerCountDisplay();
        }
    }

    public void triggerTransitStopDepature() {
        if (captureService != null) {
            if (captureService.atStop()) {
                transitStop();
            }
        }
    }

    private void transitStop() {

        if (captureService != null && captureService.capturing) {
            try {
                if (captureService.atStop()) {
                    checkStopValidator.setEnabled(false);
                    boolean banSignal = checkStopValidator.isChecked();
                    captureService.departStopStop(captureService.currentCapture.boardCount, captureService.currentCapture.alightCount, banSignal);
                    captureService.currentCapture.alightCount = 0;
                    captureService.currentCapture.boardCount = 0;

                    ImageButton botonMarcarParada = (ImageButton) findViewById(R.id.botonMarcarParada);
                    botonMarcarParada.setImageResource(R.drawable.transit_stop_button);
                    vibratorService.vibrate(25);

                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    vibratorService.vibrate(25);
                    Toast.makeText(CaptureActivity.this, "Salida de la parada", Toast.LENGTH_SHORT).show();
                } else {
                    checkStopValidator.setEnabled(true);
                    captureService.ariveAtStop();

                    ImageButton botonMarcarParada = (ImageButton) findViewById(R.id.botonMarcarParada);
                    botonMarcarParada.setImageResource(R.drawable.transit_stop_button_red);

                    vibratorService.vibrate(25);
                    Toast.makeText(CaptureActivity.this, "Llegada a la parada", Toast.LENGTH_SHORT).show();
                }

                updatePassengerCountDisplay();
                updateStopCount();
                checkStopValidator.setChecked(false);
            } catch (NoGPSFixException e) {
                Toast.makeText(CaptureActivity.this, "GPS bloqueado no puede marcar paradas.", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void checkStopSignal() {
        if (captureService != null && captureService.capturing) {
            StringBuffer OUTPUT = new StringBuffer();
            if (checkStopValidator.isChecked()) {
                OUTPUT.append("Parada marcada");
            } else {
                OUTPUT.append("Parada no marcada");
            }
            Toast.makeText(CaptureActivity.this, OUTPUT.toString(), Toast.LENGTH_LONG).show();
        }
    }

    private void updateRouteName() {
        TextView nombreRutaLabel = (TextView) findViewById(R.id.nombreRutaLabel);
        nombreRutaLabel.setText(String.format("Trazo: %s", CaptureService.currentCapture.name));
    }

    @SuppressLint("SetTextI18n")
    private void updateStopCount() {
        TextView contadorParadasText = (TextView) findViewById(R.id.contadorParadasText);
        contadorParadasText.setText(Integer.valueOf(captureService.currentCapture.stops.size()).toString());
    }

    public void updateDistance() {
        TextView distanciaText = (TextView) findViewById(R.id.distanciaText);
        DecimalFormat distanceFormat = new DecimalFormat("#,##0.00");
        distanciaText.setText(String.format("%skm", distanceFormat.format((double) (captureService.currentCapture.distance) / 1000)));
    }

    public void updateDuration() {
        if (captureService.capturing) {
            ((Chronometer) findViewById(R.id.cronometroDuracion)).setBase(captureService.currentCapture.startMs);
            ((Chronometer) findViewById(R.id.cronometroDuracion)).start();
        } else {
            ((Chronometer) findViewById(R.id.cronometroDuracion)).setBase(SystemClock.elapsedRealtime());
        }
    }

    public void updateGpsStatus() {
        TextView estatusGpsLabel = (TextView) findViewById(R.id.estatusGpsLabel);
        estatusGpsLabel.setText(captureService.getGpsStatus());
    }

    private void updatePassengerCountDisplay() {

        if (captureService != null && captureService.capturing) {

            TextView subieronPasajerosText = (TextView) findViewById(R.id.subieronPasajerosText);
            TextView totalPasajerosText = (TextView) findViewById(R.id.totalPasajerosText);
            TextView bajaronpasajerosText = (TextView) findViewById(R.id.bajaronpasajerosText);

            if (captureService.currentCapture.boardCount > 0) {
                subieronPasajerosText.setText("+" + captureService.currentCapture.boardCount.toString());
            } else {
                subieronPasajerosText.setText("0");
            }

            totalPasajerosText.setText(CaptureService.currentCapture.totalPassengerCount.toString());

            if (captureService.currentCapture.alightCount > 0) {
                bajaronpasajerosText.setText(String.format("-%d", captureService.currentCapture.alightCount));
            } else {
                bajaronpasajerosText.setText("0");
            }
        }
    }
}
