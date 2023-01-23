package org.codeandomexico.mapmap;

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
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;


public class CaptureActivity extends Activity implements ICaptureActivity {

    private CaptureService captureService;
    private Vibrator vibratorService;
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

        Typeface bebasNeueBold = Typeface.createFromAsset(getAssets(), "fonts/bebasneue_bold.ttf");
        Typeface bebasNeueRegular = Typeface.createFromAsset(getAssets(), "fonts/bebasneue_regular.ttf");

        // Informacion ruta y GPS
        TextView routeNameText = findViewById(R.id.routeNameText);
        routeNameText.setTypeface(bebasNeueBold);
        TextView gpsStatus = findViewById(R.id.gpsStatus);
        gpsStatus.setTypeface(bebasNeueBold);
        // Informacion de captura
        TextView duracionText = findViewById(R.id.duracionText);
        duracionText.setTypeface(bebasNeueBold);
        TextView distanciaText = findViewById(R.id.distanciaText);
        distanciaText.setTypeface(bebasNeueBold);
        TextView paradaText = findViewById(R.id.paradaText);
        paradaText.setTypeface(bebasNeueBold);
        TextView captureChronometer = findViewById(R.id.captureChronometer);
        captureChronometer.setTypeface(bebasNeueRegular);
        TextView distanceText = findViewById(R.id.distanceText);
        distanceText.setTypeface(bebasNeueRegular);
        TextView stopsText = findViewById(R.id.stopsText);
        stopsText.setTypeface(bebasNeueRegular);
        // Parada no senalizada
        TextView marcarParadaText = findViewById(R.id.marcarParadaText);
        marcarParadaText.setTypeface(bebasNeueRegular);
        TextView paradaNoSenalizadaText = findViewById(R.id.paradaNoSenalizadaText);
        paradaNoSenalizadaText.setTypeface(bebasNeueRegular);
        // Contador de pasajeros
        TextView pasajerosText = findViewById(R.id.pasajeros);
        pasajerosText.setTypeface(bebasNeueBold);
        TextView totalPasssengerCount = findViewById(R.id.totalPasssengerCount);
        totalPasssengerCount.setTypeface(bebasNeueBold);
        TextView boardingPassengerCount = findViewById(R.id.boardingPassengerCount);
        boardingPassengerCount.setTypeface(bebasNeueBold);
        TextView alightingPassengerCount = findViewById(R.id.alightingPassengerCount);
        alightingPassengerCount.setTypeface(bebasNeueBold);

        // setup button listeners
        View.OnClickListener listener = v -> {

            if (v.getId() == R.id.StartCaptureButton && !CaptureService.capturing) {
                startCapture();
            } else if (v.getId() == R.id.StopCaptureButton && CaptureService.capturing) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                stopCapture();
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(CaptureActivity.this);
                builder.setMessage("¿Quieres finalizar el mapeo de esta ruta?")
                        .setPositiveButton("Sí", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
            } else if (v.getId() == R.id.transitStopButton) {
                transitStop();
            } else if (v.getId() == R.id.checkStopValidator) {
                checkStopSignal();
            } else if (v.getId() == R.id.PassengerAlightButton) {
                passengerAlight();
            } else if (v.getId() == R.id.PassengerBoardButton) {
                passengerBoard();
            }
        };


        checkStopValidator = findViewById(R.id.checkStopValidator);
        checkStopValidator.setOnClickListener(listener);
        checkStopValidator.setEnabled(false);
        ImageButton startCaptureButton = findViewById(R.id.StartCaptureButton);
        startCaptureButton.setOnClickListener(listener);
        ImageButton stopCaptureButton = findViewById(R.id.StopCaptureButton);
        stopCaptureButton.setOnClickListener(listener);
        ImageButton transitStopButton = findViewById(R.id.transitStopButton);
        transitStopButton.setOnClickListener(listener);
        ImageButton passengerAlightButton = findViewById(R.id.PassengerAlightButton);
        passengerAlightButton.setOnClickListener(listener);
        ImageButton passengerBoardButton = findViewById(R.id.PassengerBoardButton);
        passengerBoardButton.setOnClickListener(listener);
        initButtons();
        updateRouteName();
        updateDistance();
        updateStopCount();
        updatePassengerCountDisplay();
    }


    private void initButtons() {
        if (captureService == null) {
            ImageButton startCaptureButton = findViewById(R.id.StartCaptureButton);
            startCaptureButton.setImageResource(R.drawable.start_button);
            ImageButton stopCaptureButton = findViewById(R.id.StopCaptureButton);
            stopCaptureButton.setImageResource(R.drawable.stop_button_gray);
        } else if (CaptureService.capturing) {
            ImageButton startCaptureButton = findViewById(R.id.StartCaptureButton);
            startCaptureButton.setImageResource(R.drawable.start_button_gray);
            ImageButton stopCaptureButton = findViewById(R.id.StopCaptureButton);
            stopCaptureButton.setImageResource(R.drawable.stop_button);
        } else if (CaptureService.currentCapture == null) {
            ImageButton startCaptureButton = findViewById(R.id.StartCaptureButton);
            startCaptureButton.setImageResource(R.drawable.start_button_gray);
            ImageButton stopCaptureButton = findViewById(R.id.StopCaptureButton);
            stopCaptureButton.setImageResource(R.drawable.stop_button_gray);
        }
        if (!CaptureService.capturing && CaptureService.currentCapture == null && captureService.atStop()) {
            ImageButton transitCaptureButton = findViewById(R.id.transitStopButton);
            transitCaptureButton.setImageResource(R.drawable.transit_stop_button_red);

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
            vibratorService.vibrate(VibrationEffect.createOneShot(25, 10));
            Toast.makeText(CaptureActivity.this, "Iniciar trazado", Toast.LENGTH_SHORT).show();
            ((Chronometer) findViewById(R.id.captureChronometer)).setBase(SystemClock.elapsedRealtime());
            ((Chronometer) findViewById(R.id.captureChronometer)).start();
            initButtons();
        }
    }

    private void stopCapture() {

        if (captureService != null) {
            vibratorService.vibrate(VibrationEffect.createOneShot(150, 10));
            if (CaptureService.currentCapture.points.size() > 0) {
                Toast.makeText(CaptureActivity.this, "Trazado completo", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(CaptureActivity.this, "No se generaron datos", Toast.LENGTH_SHORT).show();
            }
            captureService.stopCapture();
        }
        ((Chronometer) findViewById(R.id.captureChronometer)).stop();
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
            vibratorService.vibrate(VibrationEffect.createOneShot(25, 10));
            CaptureService.currentCapture.totalPassengerCount--;
            CaptureService.currentCapture.alightCount++;
            updatePassengerCountDisplay();
        }
    }

    private void passengerBoard() {
        if (captureService != null && CaptureService.capturing) {
            CaptureService.currentCapture.totalPassengerCount++;
            vibratorService.vibrate(VibrationEffect.createOneShot(25, 10));
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
                    ImageButton transitCaptureButton = findViewById(R.id.transitStopButton);
                    transitCaptureButton.setImageResource(R.drawable.transit_stop_button);
                    vibratorService.vibrate(VibrationEffect.createOneShot(25, 10));

                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    vibratorService.vibrate(VibrationEffect.createOneShot(25, 10));
                    Toast.makeText(CaptureActivity.this, "Salida de la parada", Toast.LENGTH_SHORT).show();
                } else {
                    checkStopValidator.setEnabled(true);
                    captureService.ariveAtStop();
                    ImageButton transitCaptureButton = (ImageButton) findViewById(R.id.transitStopButton);
                    transitCaptureButton.setImageResource(R.drawable.transit_stop_button_red);
                    vibratorService.vibrate(VibrationEffect.createOneShot(25, 10));
                    Toast.makeText(CaptureActivity.this, "Llegada a la parada", Toast.LENGTH_SHORT).show();
                }
                updatePassengerCountDisplay();
                updateStopCount();
                checkStopValidator.setChecked(false);
            } catch (NoGPSFixException e) {
                Toast.makeText(CaptureActivity.this, "GPS bloqueado no puede marcar paradas", Toast.LENGTH_SHORT).show();
                vibratorService.vibrate(VibrationEffect.createOneShot(25, 10));
            }
        }

    }

    private void checkStopSignal() {
        if (captureService != null && CaptureService.capturing) {
            StringBuilder output = new StringBuilder();
            if (checkStopValidator.isChecked()) {
                output.append("Señalizada");
            } else {
                output.append("No señalizada");
            }
            Toast.makeText(CaptureActivity.this, output.toString(), Toast.LENGTH_LONG).show();
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateRouteName() {
        TextView routeNameText = (TextView) findViewById(R.id.routeNameText);
        routeNameText.setText("Trazo: " + CaptureService.currentCapture.name);
    }

    @SuppressLint("SetTextI18n")
    private void updateStopCount() {
        TextView stopsText = (TextView) findViewById(R.id.stopsText);
        stopsText.setText(Integer.valueOf(CaptureService.currentCapture.stops.size()).toString());
    }

    @SuppressLint("SetTextI18n")
    public void updateDistance() {
        if (CaptureService.capturing) {
            TextView distanceText = (TextView) findViewById(R.id.distanceText);
            DecimalFormat distanceFormat = new DecimalFormat("#,##0.00");
            distanceText.setText(distanceFormat.format((double) (CaptureService.currentCapture.distance) / 1000) + "km");
        }
    }

    public void updateDuration() {
        if (CaptureService.capturing) {
            ((Chronometer) findViewById(R.id.captureChronometer)).setBase(CaptureService.currentCapture.startMs);
            ((Chronometer) findViewById(R.id.captureChronometer)).start();
        } else {
            ((Chronometer) findViewById(R.id.captureChronometer)).setBase(SystemClock.elapsedRealtime());
        }
    }

    public void updateGpsStatus() {
        TextView distanceText = (TextView) findViewById(R.id.gpsStatus);
        distanceText.setText(captureService.getGpsStatus());
    }

    @SuppressLint("SetTextI18n")
    private void updatePassengerCountDisplay() {
        if (captureService != null && CaptureService.capturing) {
            TextView totalPassengerCount = (TextView) findViewById(R.id.totalPasssengerCount);
            TextView alightingPassengerCount = (TextView) findViewById(R.id.alightingPassengerCount);
            TextView boardingPassengerCount = (TextView) findViewById(R.id.boardingPassengerCount);
            totalPassengerCount.setText(CaptureService.currentCapture.totalPassengerCount.toString());
            if (CaptureService.currentCapture.alightCount > 0) {
                alightingPassengerCount.setText(CaptureService.currentCapture.alightCount.toString());
            } else {
                alightingPassengerCount.setText("0");
            }
            if (CaptureService.currentCapture.boardCount > 0) {
                boardingPassengerCount.setText(CaptureService.currentCapture.boardCount.toString());
            } else {
                boardingPassengerCount.setText("0");
            }
        }
    }
}
