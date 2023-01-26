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
            CaptureService.setCaptureActivity(CaptureActivity.this);
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
        Intent serviceIntent = new Intent(this, CaptureService.class);
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
                    if (!CaptureService.capturing) {
                        startCapture();
                    }
                    break;

                case R.id.botonFinalizarCaptura:
                    if (CaptureService.capturing) {
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
        } else if (CaptureService.capturing) {
            ImageButton botonIniciarCaptura = (ImageButton) findViewById(R.id.botonIniciarCaptura);
            botonIniciarCaptura.setImageResource(R.drawable.start_button_gray);
            ImageButton botonFinalizarCaptura = (ImageButton) findViewById(R.id.botonFinalizarCaptura);
            botonFinalizarCaptura.setImageResource(R.drawable.stop_button);
        } else if (CaptureService.currentCapture == null) {
            ImageButton botonIniciarCaptura = (ImageButton) findViewById(R.id.botonIniciarCaptura);
            botonIniciarCaptura.setImageResource(R.drawable.start_button_gray);
            ImageButton botonFinalizarCaptura = (ImageButton) findViewById(R.id.botonFinalizarCaptura);
            botonFinalizarCaptura.setImageResource(R.drawable.stop_button_gray);
        }
        if (!CaptureService.capturing && CaptureService.currentCapture == null && captureService.atStop()) {
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
            if (CaptureService.currentCapture.points.size() > 0) {
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
        if (captureService != null && CaptureService.capturing) {
            if (CaptureService.currentCapture.totalPassengerCount == 0) {
                return;
            }
            vibratorService.vibrate(5);
            CaptureService.currentCapture.totalPassengerCount--;
            CaptureService.currentCapture.alightCount++;
            updatePassengerCountDisplay();
        }
    }

    private void passengerBoard() {
        if (captureService != null && CaptureService.capturing) {
            CaptureService.currentCapture.totalPassengerCount++;
            vibratorService.vibrate(5);
            CaptureService.currentCapture.boardCount++;
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

        if (captureService != null && CaptureService.capturing) {
            try {
                if (captureService.atStop()) {
                    checkStopValidator.setEnabled(false);
                    boolean banSignal = checkStopValidator.isChecked();
                    captureService.departStopStop(CaptureService.currentCapture.boardCount, CaptureService.currentCapture.alightCount, banSignal);
                    CaptureService.currentCapture.alightCount = 0;
                    CaptureService.currentCapture.boardCount = 0;

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
        if (captureService != null && CaptureService.capturing) {
            String estatus = checkStopValidator.isChecked() ? "Parada marcada" : "Parada no marcada";
            Toast.makeText(CaptureActivity.this, estatus, Toast.LENGTH_LONG).show();
        }
    }

    private void updateRouteName() {
        TextView nombreRutaLabel = (TextView) findViewById(R.id.nombreRutaLabel);
        nombreRutaLabel.setText(String.format("Trazo: %s", CaptureService.currentCapture.name));
    }

    @SuppressLint("SetTextI18n")
    private void updateStopCount() {
        TextView contadorParadasText = (TextView) findViewById(R.id.contadorParadasText);
        contadorParadasText.setText(Integer.valueOf(CaptureService.currentCapture.stops.size()).toString());
    }

    public void updateDistance() {
        if (CaptureService.capturing) {
            TextView distanciaText = (TextView) findViewById(R.id.distanciaText);
            DecimalFormat distanceFormat = new DecimalFormat("#,##0.00");
            distanciaText.setText(String.format("%s Km", distanceFormat.format((double) (CaptureService.currentCapture.distance) / 1000)));
        }
    }

    public void updateDuration() {
        if (CaptureService.capturing) {
            ((Chronometer) findViewById(R.id.cronometroDuracion)).setBase(CaptureService.currentCapture.startMs);
            ((Chronometer) findViewById(R.id.cronometroDuracion)).start();
        } else {
            ((Chronometer) findViewById(R.id.cronometroDuracion)).setBase(SystemClock.elapsedRealtime());
        }
    }

    public void updateGpsStatus() {
        TextView estatusGpsLabel = (TextView) findViewById(R.id.estatusGpsLabel);
        estatusGpsLabel.setText(captureService.getGpsStatus());
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void updatePassengerCountDisplay() {

        if (captureService != null && CaptureService.capturing) {

            TextView subieronPasajerosText = (TextView) findViewById(R.id.subieronPasajerosText);
            TextView totalPasajerosText = (TextView) findViewById(R.id.totalPasajerosText);
            TextView bajaronpasajerosText = (TextView) findViewById(R.id.bajaronpasajerosText);

            if (CaptureService.currentCapture.boardCount > 0) {
                subieronPasajerosText.setText(String.format("+%s", CaptureService.currentCapture.boardCount.toString()));
            } else {
                subieronPasajerosText.setText("0");
            }

            totalPasajerosText.setText(CaptureService.currentCapture.totalPassengerCount.toString());

            if (CaptureService.currentCapture.alightCount > 0) {
                bajaronpasajerosText.setText(String.format("-%d", CaptureService.currentCapture.alightCount));
            } else {
                bajaronpasajerosText.setText("0");
            }
        }
    }
}
