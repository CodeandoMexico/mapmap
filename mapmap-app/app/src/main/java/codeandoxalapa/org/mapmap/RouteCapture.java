package codeandoxalapa.org.mapmap;

import android.content.Context;
import android.location.Location;

import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import codeandoxalapa.org.mapmap.TransitWandProtos.Upload;
import codeandoxalapa.org.mapmap.TransitWandProtos.Upload.Route.Point;
import codeandoxalapa.org.mapmap.TransitWandProtos.Upload.Route.Stop;

public class RouteCapture {

    public Integer id;

    public String name;
    public String description;
    public String notes;

    public String path;
    public String imei;

    public String vehicleType;
    public String vehicleCapacity;

    public long startMs;
    public long startTime;
    public long stopTime;

    public Integer totalPassengerCount = 0;
    public Integer alightCount = 0;
    public Integer boardCount = 0;

    public Long distance = 0L;

    List<RoutePoint> points = new ArrayList<>();

    List<RouteStop> stops = new ArrayList<>();

    List<RouteStop> signals = new ArrayList<>();

    public void setRouteName(String name) {
        if (name.equals("")) {
            this.name = "Ruta " + id;
        } else {
            this.name = name;
        }
    }

    public static RouteCapture deseralize(Upload.Route r) {

        RouteCapture route = new RouteCapture();

        route.name = r.getRouteName();
        route.description = r.getRouteDescription();
        route.notes = r.getRouteNotes();
        route.vehicleCapacity = r.getVehicleCapacity();
        route.vehicleType = r.getVehicleType();
        route.path = r.getPath();
        route.imei = r.getImei();

        route.startTime = r.getStartTime();

        long lastTimepoint = route.startTime;

        for (Point p : r.getPointList()) {
            RoutePoint rp = new RoutePoint();
            rp.location = new Location("GPS");
            rp.location.setLatitude(p.getLat());
            rp.location.setLongitude(p.getLon());
            rp.time = (p.getTimeoffset() * 1000L) + lastTimepoint;
            lastTimepoint = rp.time;
            route.points.add(rp);
        }

        lastTimepoint = route.startTime;

        for (Stop s : r.getStopList()) {
            RouteStop rs = new RouteStop();
            rs.location = new Location("GPS");
            rs.location.setLatitude(s.getLat());
            rs.location.setLongitude(s.getLon());
            rs.arrivalTime = (s.getArrivalTimeoffset() * 1000L) + lastTimepoint;
            rs.departureTime = (s.getDepartureTimeoffset() * 1000L) + lastTimepoint;
            lastTimepoint = rs.arrivalTime;
            rs.signalStop = s.getSignalStop();
            rs.board = s.getBoard();
            rs.alight = s.getAlight();
            route.stops.add(rs);
        }

        return route;

    }

    public Upload.Route seralize() {

        Upload.Route.Builder route = Upload.Route.newBuilder();
        route.setRouteName(name);
        route.setRouteDescription(description);
        route.setRouteNotes(notes);
        route.setVehicleCapacity(vehicleCapacity);
        route.setVehicleType(vehicleType);
        route.setPath(path);
        route.setStartTime(startTime);
        route.setImei(imei);


        long lastTimepoint = startMs;

        for (RoutePoint rp : points) {
            Upload.Route.Point.Builder point = Upload.Route.Point.newBuilder();
            point.setLat((float) rp.location.getLatitude());
            point.setLon((float) rp.location.getLongitude());
            point.setTimeoffset((int) ((rp.time - lastTimepoint) / 1000));
            lastTimepoint = rp.time;

            route.addPoint(point);
        }

        lastTimepoint = startMs;

        for (RouteStop rs : stops) {

            Upload.Route.Stop.Builder stop = Upload.Route.Stop.newBuilder();

            stop.setLat((float) rs.location.getLatitude());
            stop.setLon((float) rs.location.getLongitude());
            stop.setArrivalTimeoffset((int) ((rs.arrivalTime - lastTimepoint) / 1000));
            stop.setDepartureTimeoffset((int) ((rs.departureTime - lastTimepoint) / 1000));
            stop.setAlight(rs.alight);
            stop.setBoard(rs.board);
            stop.setSignalStop(rs.signalStop);
            lastTimepoint = rs.arrivalTime;

            route.addStop(stop);
        }

        return route.build();
    }

    public static FilenameFilter getFileNameFilterRoute() {
        return (dir, name1) -> {
            if (name1.lastIndexOf('.') > 0) {
                // get last index for '.' char
                int lastIndex = name1.lastIndexOf('.');
                // get extension
                String str = name1.substring(lastIndex);
                // match path name extension
                return str.equals(".pb");
            }
            return false;
        };
    }

    public static int checkNumberListFiles(Context context) {
        FilenameFilter fileNameFilter = getFileNameFilterRoute();
        return context.getFilesDir().listFiles(fileNameFilter).length;
    }
}
