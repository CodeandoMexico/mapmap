package org.codeandomexico.mapmap;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

import org.codeandomexico.mapmap.TransitWandProtos.Upload;

public class ReviewActivity extends Activity {

    public static String DELETE_ITEM_ACTION = "com.conveyal.transitwand.DELETE_ITEM_ACTION";

    private final BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent == null || intent.getAction() == null) {
                return;
            }
            if (intent.getAction().equals(ReviewActivity.DELETE_ITEM_ACTION)) {
                updateList();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);
        if (RouteCapture.checkNumberListFiles(ReviewActivity.this) == 0) {
            Toast.makeText(ReviewActivity.this, "No hay rutas en el dispositivo", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, new IntentFilter(ReviewActivity.DELETE_ITEM_ACTION));
        updateList();
    }

    private void updateList() {

        ArrayList<RouteCapture> routes = new ArrayList<>();
        FilenameFilter fileNameFilter = RouteCapture.getFileNameFilterRoute();

        for (File f : getFilesDir().listFiles(fileNameFilter)) {

            DataInputStream dataInputStream = null;

            try {
                dataInputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(f)));
                Upload.Route pbRouteData = Upload.Route.parseDelimitedFrom(dataInputStream);
                assert pbRouteData != null;
                RouteCapture rc = RouteCapture.deseralize(pbRouteData);
                routes.add(rc);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (dataInputStream != null) {
                    try {
                        dataInputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        ListView captureListView = (ListView) findViewById(R.id.captureList);
        captureListView.setAdapter(new CaptureListAdapter(this, routes));
    }
}