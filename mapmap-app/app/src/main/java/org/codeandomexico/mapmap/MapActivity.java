package org.codeandomexico.mapmap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.modules.IArchiveFile;
import org.osmdroid.tileprovider.modules.MBTilesFileArchive;
import org.osmdroid.tileprovider.modules.MapTileFileArchiveProvider;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.MyLocationOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.PathOverlay;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

import org.codeandomexico.mapmap.TransitWandProtos.Upload;

public class MapActivity extends Activity {

    private static final Boolean USE_MAPBOX = true;
    private static final String MAPBOX_URL = "http://a.tiles.mapbox.com/v3/conveyal.map-jc4m5i21/";
    private static final String MAPBOX_URL_HIRES = "http://a.tiles.mapbox.com/v3/conveyal.map-o5b3npfa/";
    private MyLocationOverlay locOverlay;
    public static int itemPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        DefaultResourceProxyImpl resourceProxy = new DefaultResourceProxyImpl(getApplicationContext());
        setContentView(R.layout.activity_map);

        Typeface fontBebasNeueBold = Typeface.createFromAsset(getAssets(), "fonts/bebasneue_bold.ttf");
        TextView nombreText = (TextView) findViewById(R.id.nombreText);
        nombreText.setTypeface(fontBebasNeueBold);
        TextView descripcionText = (TextView) findViewById(R.id.descripcionText);
        descripcionText.setTypeface(fontBebasNeueBold);
        TextView notasText = (TextView) findViewById(R.id.notasText);
        notasText.setTypeface(fontBebasNeueBold);

        ITileSource customTileSource;
        MapView mapView;
        if (USE_MAPBOX) {
            // mapbox hosted tile source
            String tileUrl = MAPBOX_URL;
            mapView = new MapView(this, 256);
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            if (metrics.densityDpi > DisplayMetrics.DENSITY_MEDIUM) {
                tileUrl = MAPBOX_URL_HIRES;
            }
            customTileSource = new XYTileSource("Mapbox", null, 0, 17, 256, ".png", tileUrl);
            mapView.setTileSource(customTileSource);
            mapView.getTileProvider().clearTileCache();

        } else {
            // local mbtiles cache
            customTileSource = new XYTileSource(
                    "mbtiles",
                    ResourceProxy.string.offline_mode,
                    10,
                    16,
                    256,
                    ".png",
                    "http://conveyal.com/");
            SimpleRegisterReceiver simpleReceiver = new SimpleRegisterReceiver(this);
            MBTilesFileArchive mbtilesDb = MBTilesFileArchive.getDatabaseFileArchive(null);  // swap null for file
            IArchiveFile[] files = {mbtilesDb};
            MapTileModuleProviderBase moduleProvider = new MapTileFileArchiveProvider(simpleReceiver, customTileSource, files);
            MapTileProviderArray provider = new MapTileProviderArray(customTileSource, null, new MapTileModuleProviderBase[]{moduleProvider});
            mapView = new MapView(this, 256, resourceProxy, provider);
        }

        RelativeLayout mapContainer = (RelativeLayout) findViewById(R.id.mainMapView);
        mapView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
        mapContainer.addView(mapView);

        final FilenameFilter fileNameFilter = RouteCapture.getFileNameFilterRoute();
        File f = getFilesDir().listFiles(fileNameFilter)[itemPosition];
        DataInputStream dataInputStream = null;
        RouteCapture rc = null;
        try {
            dataInputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(f)));
            Upload.Route pbRouteData = Upload.Route.parseDelimitedFrom(dataInputStream);
            assert pbRouteData != null;
            rc = RouteCapture.deseralize(pbRouteData);
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

        GeoPoint gp = null;

        if (rc != null) {
            Paint paint = new Paint();
            paint.setColor(Color.BLUE);
            paint.setAlpha(75);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(10);

            PathOverlay capturePath = new PathOverlay(Color.BLUE, this);
            capturePath.setPaint(paint);

            for (RoutePoint p : rc.points) {
                gp = new GeoPoint(p.location.getLatitude(), p.location.getLongitude());
                capturePath.addPoint(gp);
            }

            mapView.getOverlays().add(capturePath);


            ArrayList<OverlayItem> items = new ArrayList<>();

            for (RouteStop s : rc.stops) {
                OverlayItem itemMarker = new OverlayItem("Subieron: " + s.board + " Bajaron: " + s.alight, "", new GeoPoint(s.location.getLatitude(), s.location.getLongitude()));
                itemMarker.setMarker(MapActivity.this.getResources().getDrawable(R.drawable.stop));
                items.add(itemMarker);
            }


            ItemizedIconOverlay<OverlayItem> stopOverlay = new ItemizedIconOverlay<>(items, new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                @Override
                public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                    Toast.makeText(MapActivity.this, item.mTitle, Toast.LENGTH_SHORT).show();
                    return true;
                }

                @Override
                public boolean onItemLongPress(final int index, final OverlayItem item) {
                    Toast.makeText(MapActivity.this, item.mTitle, Toast.LENGTH_SHORT).show();
                    return false;
                }
            }, resourceProxy);

            mapView.getOverlays().add(stopOverlay);
            mapView.invalidate();

            View.OnClickListener listener = v -> {
                if (v.getId() == R.id.botonBasura) {
                    DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                getFilesDir().listFiles(fileNameFilter)[MapActivity.itemPosition].delete();
                                Intent intent = new Intent(ReviewActivity.DELETE_ITEM_ACTION);
                                LocalBroadcastManager.getInstance(MapActivity.this).sendBroadcast(intent);
                                MapActivity.this.finish();
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
                    builder.setMessage("¿Realmente quieres borrar esta ruta?").setPositiveButton("Sí", dialogClickListener).setNegativeButton("No", dialogClickListener).show();
                }
            };

            ImageButton trashButton = (ImageButton) findViewById(R.id.botonBasura);
            trashButton.setOnClickListener(listener);

            nombreText.setText(rc.name);
            descripcionText.setText(rc.description);
            notasText.setText(rc.notes);
        }

        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(16);

        if (gp != null) {
            mapView.getController().setCenter(gp);
        } else {
            mapView.getController().setCenter(new GeoPoint(10.3021258, 123.89616));
        }
        mapView.invalidate();
    }

}
