package org.codeandomexico.mapmap;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.GnssStatus;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.util.Log;
import android.widget.Toast;

import com.javadocmd.simplelatlng.LatLng;
import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import org.codeandomexico.mapmap.protobuffer.TransitWandProtos.Upload;

public class CaptureService extends Service {

    public final static String SERVER = "192.168.0.4:8080"; //"mapaton.org/mapmap";  // 10.0.2.2:9000;
    public final static String URL_BASE = "http://" + SERVER + "/";

    public static Boolean boundToService = false;

    private static final int GPS_UPDATE_INTERVAL = 1;    // seconds
    private static final int MIN_ACCURACY = 15;    // meters
    private static final int MIN_DISTANCE = 5;    // meters

    private static Boolean gpsStarted = false;
    private static Boolean registered = false;

    public static String imei = null;
    public static Long phoneId = null;

    private static final int NOTIFICATION_ID = 234231222;

    private final IBinder binder = new CaptureServiceBinder();

    private CaptureLocationListener locationListener;

    private LocationManager gpsLocationManager;
    private NotificationManager gpsNotificationManager;

    GnssStatus.Callback mGnssStatusCallback;

    private static boolean gpsEnabled;

    public static RouteCapture currentCapture = null;
    public static RouteStop currentStop = null;

    private static Location lastLocation = null;

    public static Boolean capturing = false;

    private static ICaptureActivity captureActivity;

    private SharedPreferences prefsManager = null;

    @Override
    public void onCreate() {
        Log.i("CaptureService", "onCreate");
        gpsNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        prefsManager = PreferenceManager.getDefaultSharedPreferences(this);
        getGpsStatus();

        if (imei == null || CaptureService.imei.length() == 0) {
            CaptureService.imei = Secure.getString(getBaseContext().getContentResolver(), Secure.ANDROID_ID);
        }

        mGnssStatusCallback = new GnssStatus.Callback() {
            // TODO: add your code here!
        };


    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class CaptureServiceBinder extends Binder {
        public CaptureService getService() {
            return CaptureService.this;
        }
    }

    public static void setCaptureActivity(ICaptureActivity ca) {
        captureActivity = ca;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("CaptureService", "onStartCommand");
        handleIntent(intent);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i("CaptureService", "onDestroy");
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        Log.i("CaptureService", "onLowMemory");
        super.onLowMemory();
    }

    private void handleIntent(Intent intent) {
        // handle received intents
    }

    public void newCapture(String name, String description, String notes, String vehicleType, String vehicleCapacity, String path) {

        startGps();
        Toast.makeText(CaptureService.this, "Nueva captura.", Toast.LENGTH_SHORT).show();

        synchronized (this) {
            if (currentCapture != null && capturing) {
                stopCapture();
            }
            lastLocation = null;
            int id = prefsManager.getInt("routeId", 10000);
            id++;
            prefsManager.edit().putInt("routeId", id).apply();
            currentCapture = new RouteCapture();
            currentCapture.id = id;
            currentCapture.setRouteName(name);
            currentCapture.description = description;
            currentCapture.notes = notes;
            currentCapture.vehicleCapacity = vehicleCapacity;
            currentCapture.vehicleType = vehicleType;
            currentCapture.path = path;
            currentCapture.imei = CaptureService.imei;
        }
    }

    public void startCapture() throws NoCurrentCaptureException {

        startGps();
        Toast.makeText(CaptureService.this, "Empezando captura de datos.", Toast.LENGTH_LONG).show();

        if (currentCapture != null) {
            currentCapture.startTime = new Date().getTime();
            currentCapture.startMs = SystemClock.elapsedRealtime();
            capturing = true;
        } else {
            throw new NoCurrentCaptureException();
        }
    }

    public void stopCapture() {
        stopGps();
        hideNotificationTray();
        currentCapture.stopTime = new Date().getTime();
        if (currentCapture.points.size() > 0) {
            Upload.Route routePb = currentCapture.seralize();
            File file = new File(getFilesDir(), "route_" + currentCapture.id + ".pb");
            try {
                FileOutputStream os = new FileOutputStream(file);
                routePb.writeDelimitedTo(os);
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        capturing = false;
        currentCapture = null;
    }


    public void ariveAtStop() throws NoGPSFixException {

        if (currentStop != null) {
            departStopStop(0, 0, true);
        }
        if (lastLocation != null) {
            currentStop = new RouteStop();
            currentStop.arrivalTime = SystemClock.elapsedRealtime();
            currentStop.location = lastLocation;
        } else {
            throw new NoGPSFixException();
        }
    }


    public void departStopStop(int board, int alight, boolean signalStop) throws NoGPSFixException {

        if (lastLocation != null) {
            if (currentStop == null) {
                currentStop = new RouteStop();
                currentStop.arrivalTime = SystemClock.elapsedRealtime();
                currentStop.location = lastLocation;
            }

            currentStop.alight = alight;
            currentStop.board = board;
            currentStop.departureTime = SystemClock.elapsedRealtime();
            currentStop.signalStop = signalStop;

            Log.i("Transit-flag", "Value: " + currentStop.signalStop);

            currentCapture.stops.add(currentStop);
            currentCapture.signals.add(currentStop);
            currentStop = null;
        } else
            throw new NoGPSFixException();

    }

    public boolean atStop() {
        return currentStop != null;
    }

    public long distanceFromLocation(Location locA, Location locB) {
        LatLng latLongA = new LatLng(locA.getLatitude(), locA.getLongitude());
        LatLng latLongB = new LatLng(locB.getLatitude(), locB.getLongitude());
        return Math.round(LatLngTool.distance(latLongA, latLongB, LengthUnit.METER));
    }

    public void onLocationChanged(Location location) {
        Log.i("", "onLocationChanged: " + location);

        if (atStop() && lastLocation != null && distanceFromLocation(currentStop.location, lastLocation) > MIN_DISTANCE * 2) {
            captureActivity.triggerTransitStopDepature();
        }

        if (currentCapture != null && location.getAccuracy() < MIN_ACCURACY * 2) {
            RoutePoint rp = new RoutePoint();
            rp.location = location;
            rp.time = SystemClock.elapsedRealtime();
            currentCapture.points.add(rp);

            if (lastLocation != null) {
                currentCapture.distance += distanceFromLocation(lastLocation, location);
                if (captureActivity != null) {
                    captureActivity.updateDistance();
                }
            }

            lastLocation = location;
            if (captureActivity != null) {
                captureActivity.updateGpsStatus();
            }
        }
    }

    private void startGps() {
        Log.i("LocationService", "startGps");
        if (gpsStarted) {
            return;
        }
        gpsStarted = true;
        if (locationListener == null) {
            locationListener = new CaptureLocationListener();
        }
        // connect location manager
        gpsLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        gpsEnabled = gpsLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (gpsEnabled) {
            Log.i("LocationService", "startGps attaching listeners");
            // request gps location and status updates
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            gpsLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_UPDATE_INTERVAL * 1000, MIN_DISTANCE, locationListener);
            gpsLocationManager.registerGnssStatusCallback(mGnssStatusCallback);
            // update gps status in main activity
        } else {
            Log.e("LocationService", "startGps failed, GPS not enabled");
            // update gps status in main activity
        }
    }

    private void stopGps() {
        Log.i("LocationService", "stopGps");
        if (gpsLocationManager == null) {
            gpsLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }
        if (locationListener != null) {
            gpsLocationManager.removeUpdates(locationListener);
            gpsLocationManager.unregisterGnssStatusCallback(mGnssStatusCallback);
        }
        gpsStarted = false;
    }

    public void restartGps() {
        Log.i("LocationService", "restartGps");
        stopGps();
        startGps();
    }

    public void stopGpsAndRetry() {
        Log.d("LocationService", "stopGpsAndRetry");
        restartGps();
    }

    public String getGpsStatus() {
        if (lastLocation != null) {
            return "GPS +/-" + Math.round(lastLocation.getAccuracy()) + "m";
        }
        return "Adquiriendo señal GPS";
    }

    private void showNotificationTray() {
        Intent contentIntent = new Intent(this, CaptureActivity.class);
        PendingIntent pending = PendingIntent.getActivity(getApplicationContext(), 0, contentIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new Notification.Builder(getApplicationContext())
                .setOngoing(true)
                .setSmallIcon(R.drawable.app_logo)
                .setContentIntent(pending)
                .setChannelId(MainActivity.MAPMAP_CHANNEL_ID)
                .setContentText("MapMap")
                .build();

        gpsNotificationManager.notify(NOTIFICATION_ID, notification);
        startForeground(NOTIFICATION_ID, notification);
    }

    private String createNotificationChannel(String channelId, String channelName) {
        NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_NONE);
        notificationChannel.setLightColor(Color.BLUE);
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager service = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        service.createNotificationChannel(notificationChannel);
        return channelId;
    }

    private void hideNotificationTray() {
        gpsNotificationManager.cancel(NOTIFICATION_ID);
        stopForeground(true);
    }


    private class CaptureLocationListener implements LocationListener, GpsStatus.Listener {

        @Override
        public void onLocationChanged(Location location) {
            try {
                if (location != null) {
                    Log.i("ProbeLocationListener", "onLocationChanged");
                    CaptureService.this.onLocationChanged(location);
                }
            } catch (Exception ex) {
                Log.e("ProbeLocationListener", "onLocationChanged failed", ex);
            }

        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.i("ProbeLocationListener", "onProviderDisabled");
            //this.restartGps();
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.i("ProbeLocationListener", "onProviderEnabled");
            //locationService.restartGps();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        public void onGpsStatusChanged(int event) {
            switch (event) {
                case GpsStatus.GPS_EVENT_FIRST_FIX:

                    break;

                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:

                    break;

                case GpsStatus.GPS_EVENT_STARTED:

                    break;

                case GpsStatus.GPS_EVENT_STOPPED:

                    break;

            }
        }
    }
}
