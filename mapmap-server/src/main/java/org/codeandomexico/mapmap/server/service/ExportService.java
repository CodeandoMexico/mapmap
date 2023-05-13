package org.codeandomexico.mapmap.server.service;

import com.opencsv.CSVWriter;
import org.codeandomexico.mapmap.server.model.Route;
import org.codeandomexico.mapmap.server.model.TripPattern;
import org.codeandomexico.mapmap.server.repository.TripPatternRepository;
import org.codeandomexico.mapmap.server.util.DirectoryZip;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class ExportService {

    private final TripPatternRepository tripPatternRepository;

    Logger logger = LoggerFactory.getLogger(ExportService.class);

    public ExportService(TripPatternRepository tripPatternRepository) {
        this.tripPatternRepository = tripPatternRepository;
    }

    public Optional<File> generateExportShapefile(Set<String> unitIds, String timestamp) {

        List<TripPattern> tripPatterns = tripPatternRepository.findByRoute_Phone_UnitIdInOrderByIdAsc(unitIds);

        try {
            File tempDirectory = Files.createTempDirectory(timestamp).toFile();
            processStopsShapefile(tempDirectory, timestamp, tripPatterns);
            processRoutesShepfile(tempDirectory, timestamp, tripPatterns);
            File tempFile = Files.createTempFile(timestamp, "_SHP.zip").toFile();
            DirectoryZip.zip(tempDirectory, tempFile);
            return Optional.of(tempFile);
        } catch (IOException | SchemaException e) {
            logger.error("No se pudo generar el fichero Shapefile de exportación.");
            logger.error(e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<File> generateExportCsv(Set<String> unitIds, String timestamp) {

        List<TripPattern> tripPatterns = tripPatternRepository.findByRoute_Phone_UnitIdInOrderByIdAsc(unitIds);

        try {
            File tempDirectory = Files.createTempDirectory(timestamp).toFile();

            File routesFile = new File(tempDirectory, timestamp + "_routes.csv");
            FileWriter routesCsv = new FileWriter(routesFile);
            CSVWriter rotuesCsvWriter = new CSVWriter(routesCsv);
            String[] routesHeader = "unit_id, route_id, route_name, route_description, field_notes, vehicle_type, vehicle_capacity, start_capture".split(",");
            rotuesCsvWriter.writeNext(routesHeader);

            File stopsFile = new File(tempDirectory, timestamp + "_stops.csv");
            FileWriter stopsCsv = new FileWriter(stopsFile);
            CSVWriter stopsCsvWriter = new CSVWriter(stopsCsv);
            String[] stopsHeader = "route_id, stop_sequence, lat, lon, travel_time, dwell_time, board, allright".split(",");
            stopsCsvWriter.writeNext(stopsHeader);

            tripPatterns.forEach(tripPattern -> {
                Route route = tripPattern.route;
                String[] routeData = new String[routesHeader.length];
                routeData[0] = route.phone.unitId;
                routeData[1] = route.id.toString();
                routeData[2] = route.routeLongName;
                routeData[3] = route.routeDesc;
                routeData[4] = route.routeNotes;
                routeData[5] = route.vehicleType;
                routeData[6] = route.vehicleCapacity;
                routeData[7] = (route.captureTime != null) ? route.captureTime.toString() : "";
                rotuesCsvWriter.writeNext(routeData);

                tripPattern.patternStops.forEach(tripPatternStop -> {
                    String[] stopData = new String[stopsHeader.length];
                    stopData[0] = route.id.toString();
                    stopData[1] = tripPatternStop.stopSequence.toString();
                    stopData[2] = String.valueOf(tripPatternStop.stop.location.getCoordinate().y);
                    stopData[3] = String.valueOf(tripPatternStop.stop.location.getCoordinate().x);
                    stopData[4] = String.valueOf(tripPatternStop.defaultTravelTime);
                    stopData[5] = String.valueOf(tripPatternStop.defaultDwellTime);
                    stopData[6] = String.valueOf(tripPatternStop.board);
                    stopData[7] = String.valueOf(tripPatternStop.alight);
                    stopsCsvWriter.writeNext(stopData);
                });
            });
            rotuesCsvWriter.flush();
            rotuesCsvWriter.close();

            stopsCsvWriter.flush();
            stopsCsvWriter.close();

            File tempFile = Files.createTempFile(timestamp, "_CSV.zip").toFile();
            DirectoryZip.zip(tempDirectory, tempFile);
            return Optional.of(tempFile);
        } catch (IOException e) {
            logger.error("No se pudo generar el fichero CSV de exportación.");
            logger.error(e.getMessage());
        }
        return Optional.empty();
    }

    private void processStopsShapefile(File outputDirectory, String exportName, List<TripPattern> tripPatterns) throws IOException, SchemaException {

        File outputShapefile = new File(outputDirectory, exportName + "_stops.shp");

        Map<String, Serializable> params = new HashMap<>();
        params.put("url", outputShapefile.toURI().toURL());
        params.put("create spatial index", Boolean.TRUE);

        ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
        ShapefileDataStore shapefileDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
        shapefileDataStore.forceSchemaCRS(DefaultGeographicCRS.WGS84);
        SimpleFeatureType STOP_TYPE = DataUtilities.createType("Stop",
                "the_geom:Point:srid=4326," +
                        "STOP_ID:String," +
                        "ROUTE_ID:String," +
                        "SEQUENCE:Integer," +
                        "TRAVEL_S:Integer," +
                        "DWELL_S:Integer," +
                        "ARRIVAL_S:Integer," +
                        "ARRIVAL_T:String," +
                        "DEPARTUR_S:Integer," +
                        "DEPARTUR_T:String," +
                        "BOARD:Integer," +
                        "ALIGHT:Integer," +
                        "PASSENGERS:Integer");
        shapefileDataStore.createSchema(STOP_TYPE);

        List<SimpleFeature> simpleFeatures = tripPatterns.stream().flatMap(tripPattern -> {
            AtomicLong cumulativeTime = new AtomicLong();
            final AtomicLong[] passengers = {new AtomicLong()};
            Date routeStartTime = tripPattern.route.captureTime;
            return tripPattern.patternStops.stream().map(tripPatternStop -> {
                SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(STOP_TYPE);
                featureBuilder.add(tripPatternStop.stop.location);
                featureBuilder.add(tripPatternStop.stop.id.toString());
                featureBuilder.add(tripPattern.route.id.toString());
                featureBuilder.add(tripPatternStop.stopSequence);
                featureBuilder.add(tripPatternStop.defaultTravelTime);
                featureBuilder.add(tripPatternStop.defaultDwellTime);

                if (tripPatternStop.defaultTravelTime != null) {
                    cumulativeTime.addAndGet(tripPatternStop.defaultTravelTime);
                }
                featureBuilder.add(cumulativeTime);
                Date arrivalTimestamp = new Date(routeStartTime.getTime() + (cumulativeTime.get() * 1000));
                featureBuilder.add(arrivalTimestamp.toString());

                if (tripPatternStop.defaultDwellTime != null) {
                    cumulativeTime.addAndGet(tripPatternStop.defaultDwellTime);
                }
                featureBuilder.add(cumulativeTime);
                Date departureTimestamp = new Date(routeStartTime.getTime() + (cumulativeTime.get() * 1000));
                featureBuilder.add(departureTimestamp.toString());

                featureBuilder.add(tripPatternStop.board);
                featureBuilder.add(tripPatternStop.alight);

                passengers[0].addAndGet(tripPatternStop.board);
                passengers[0].addAndGet(-tripPatternStop.alight);
                passengers[0] = passengers[0].get() < 0 ? new AtomicLong(0) : passengers[0];
                featureBuilder.add(passengers[0].get());

                return featureBuilder.buildFeature(null);
            });
        }).toList();

        DefaultFeatureCollection featureCollection = new DefaultFeatureCollection();
        featureCollection.addAll(simpleFeatures);

        String typeName = shapefileDataStore.getTypeNames()[0];
        SimpleFeatureSource featureSource = shapefileDataStore.getFeatureSource(typeName);
        if (featureSource instanceof SimpleFeatureStore featureStore) {
            Transaction transaction = new DefaultTransaction("create");
            featureStore.setTransaction(transaction);
            featureStore.addFeatures(featureCollection);
            transaction.commit();
            transaction.close();
        }
    }

    private void processRoutesShepfile(File outputDirectory, String exportName, List<TripPattern> tripPatterns) throws IOException, SchemaException {

        File outputShapefile = new File(outputDirectory, exportName + "_routes.shp");

        Map<String, Serializable> params = new HashMap<>();
        params.put("url", outputShapefile.toURI().toURL());
        params.put("create spatial index", Boolean.TRUE);
        ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
        ShapefileDataStore dataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
        dataStore.forceSchemaCRS(DefaultGeographicCRS.WGS84);

        SimpleFeatureType ROUTE_TYPE = DataUtilities.createType("Route",
                "the_geom:LineString:srid=4326," +
                "ROUTE_ID:String," +
                "UNIT_ID:String," +
                "UNIT_OWNER:String," +
                "NAME:String," +
                "DESC:String," +
                "NOTES:String," +
                "START:String");
        dataStore.createSchema(ROUTE_TYPE);

        List<SimpleFeature> simpleFeatures = tripPatterns.stream()
                .filter(tripPattern -> tripPattern.tripShape != null)
                .map(tripPattern -> {
            SimpleFeatureBuilder simpleFeatureBuilder = new SimpleFeatureBuilder(ROUTE_TYPE);
            simpleFeatureBuilder.add(tripPattern.tripShape.shape);
            simpleFeatureBuilder.add(tripPattern.route.id.toString());
            simpleFeatureBuilder.add(tripPattern.route.phone.unitId);
            simpleFeatureBuilder.add(tripPattern.route.phone.userName != null ? tripPattern.route.phone.userName.toUpperCase() : "");
            simpleFeatureBuilder.add(tripPattern.route.routeLongName != null ? tripPattern.route.routeLongName.toUpperCase() : "");
            simpleFeatureBuilder.add(tripPattern.route.routeDesc != null ? tripPattern.route.routeDesc.toUpperCase() : "");
            simpleFeatureBuilder.add(tripPattern.route.routeNotes != null ? tripPattern.route.routeNotes.toUpperCase() : "");
            simpleFeatureBuilder.add(tripPattern.route.captureTime != null ? tripPattern.route.captureTime.toString() : "");
            return simpleFeatureBuilder.buildFeature(null);
        }).toList();

        DefaultFeatureCollection featureCollection = new DefaultFeatureCollection();
        featureCollection.addAll(simpleFeatures);

        String typeName = dataStore.getTypeNames()[0];
        SimpleFeatureSource featureSource = dataStore.getFeatureSource(typeName);

        if (featureSource instanceof SimpleFeatureStore featureStore) {
            Transaction transaction = new DefaultTransaction("create");
            featureStore.setTransaction(transaction);
            featureStore.addFeatures(featureCollection);
            transaction.commit();
            transaction.close();
        }
    }

}
