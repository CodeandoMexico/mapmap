package org.codeandomexico.mapmap.server.service;

import com.conveyal.transitwand.TransitWandProtos;
import org.apache.commons.lang3.StringUtils;
import org.codeandomexico.mapmap.server.model.*;
import org.codeandomexico.mapmap.server.repository.AgencyRepository;
import org.codeandomexico.mapmap.server.repository.TripPatternRepository;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Component
public class UploadService {

    @Autowired
    private AgencyRepository agencyRepository;
    @Autowired
    private TripPatternRepository tripPatternRepository;

    Logger logger = LoggerFactory.getLogger(UploadService.class);

    public boolean uploadRoutes(MultipartFile data, Phone phone) {
        try {
            File tempFile = Files.createTempFile(phone.unitId, ".pb").toFile();
            data.transferTo(tempFile);

            byte[] dataFrame = new byte[(int) tempFile.length()];
            DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(tempFile)));
            int bytesRead = dataInputStream.read(dataFrame);
            logger.debug("El fichero Protocol Buffer tiene %d bytes".formatted(bytesRead));

            TransitWandProtos.Upload upload = TransitWandProtos.Upload.parseFrom(dataFrame);

            for (TransitWandProtos.Upload.Route r : upload.getRouteList()) {
                if (r.getPointList().size() <= 1) continue;
                Optional<Agency> agency = agencyRepository.findById(1L); // Esto es un parche. Solo hay una agencia en toda la BBDD.
                if (agency.isEmpty()) continue;

                Route route = new Route("", r.getRouteName().toUpperCase(), RouteType.BUS, r.getRouteDescription().toUpperCase(), agency.get());
                route.phone = phone;
                route.routeNotes = r.getRouteNotes().toUpperCase();
                route.vehicleCapacity = r.getVehicleCapacity();
                route.vehicleType = r.getVehicleType();
                route.captureTime = new Date(r.getStartTime());
                route.routePoints = new ArrayList<>();

                List<String> points = new ArrayList<>();
                int pointSequence = 1;
                for (TransitWandProtos.Upload.Route.Point p : r.getPointList()) {
                    points.add(p.getLon() + " " + p.getLat());
                    RoutePoint routePoint = new RoutePoint();
                    routePoint.route = route;
                    routePoint.sequence = pointSequence;
                    routePoint.lat = (double) p.getLat();
                    routePoint.lon = (double) p.getLon();
                    routePoint.timeOffset = p.getTimeoffset();

                    pointSequence++;

                    route.routePoints.add(routePoint);
                }

                // Creamos el Trip Shape
                WKTReader wktReader = new WKTReader();
                TripShape tripShape = new TripShape();
                tripShape.describedDistance = 0.0001;
                tripShape.gtfsShapeId = "";
                tripShape.description = r.getRouteName();
                tripShape.shape = (LineString) wktReader.read("LINESTRING(" + StringUtils.join(points, ", ") + ")");
                tripShape.shape.setSRID(4326);

                // Creamos el Trip Pattern (clase madre)
                TripPattern tripPattern = new TripPattern();
                tripPattern.route = route;
                tripPattern.headsign = r.getRouteName().toUpperCase();
                tripPattern.tripShape = tripShape;
                tripPattern.patternStops = new ArrayList<>();
                tripPattern.name = r.getRouteName().toUpperCase();
                tripPattern.weekday = false;
                tripPattern.sunday = false;
                tripPattern.saturday = false;

                int sequenceId = 0;
                for (TransitWandProtos.Upload.Route.Stop s : r.getStopList()) {
                    Stop stop = new Stop();
                    stop.location = (Point) wktReader.read("POINT(" + s.getLon() + " " + s.getLat() + ")");
                    stop.location.setSRID(4326);
                    stop.board = s.getBoard();
                    stop.alight = s.getAlight();

                    TripPatternStop tripPatternStop = new TripPatternStop();
                    tripPatternStop.stop = stop;
                    tripPatternStop.stopSequence = sequenceId;
                    tripPatternStop.defaultTravelTime = s.getArrivalTimeoffset();
                    tripPatternStop.defaultDwellTime = s.getDepartureTimeoffset() - s.getArrivalTimeoffset();
                    tripPatternStop.tripPattern = tripPattern;
                    tripPatternStop.board = s.getBoard();
                    tripPatternStop.alight = s.getAlight();

                    sequenceId++;

                    tripPattern.patternStops.add(tripPatternStop);
                }
                tripPatternRepository.save(tripPattern);
            }
            dataInputStream.close();
            return true;
        } catch (IOException | ParseException e) {
            logger.error("Se produjo un error al procesar la Ruta.");
            logger.error(e.getMessage());
        }
        return false;
    }

}
