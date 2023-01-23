package org.codeandomexico.mapmap.server.model;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

import java.util.Hashtable;

@Entity
@NoArgsConstructor
public class Stop {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hibernate")
    @SequenceGenerator(name = "hibernate", sequenceName = "hibernate_sequence", allocationSize = 1)
    public Long id;

    @ManyToOne
    @JoinColumn(name = "agency_id")
    public Agency agency;

    @OneToOne(mappedBy = "stop")
    TripPatternStop tripPatternStop;

    public String gtfsStopId;
    public String stopCode;
    public String stopName;
    public String stopDesc;
    public String zoneId;
    public String stopUrl;
    public Integer board;
    public Integer alight;

    @Enumerated(EnumType.STRING)
    public LocationType locationType;
    public String parentStation;
    // Major stop is a custom field; it has no corralary in the GTFS.
    public Boolean majorStop;

    @Column(columnDefinition = "geometry")
    public Point location;

    public void setLocation(Hashtable<String, Double> loc) {
        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        this.location = geometryFactory.createPoint(new Coordinate(loc.get("lng"), loc.get("lat")));
    }

    public Stop(org.onebusaway.gtfs.model.Stop stop, GeometryFactory geometryFactory) {
        this.gtfsStopId = stop.getId().toString();
        this.stopCode = stop.getCode();
        this.stopName = stop.getName();
        this.stopDesc = stop.getDesc();
        this.zoneId = stop.getZoneId();
        this.stopUrl = stop.getUrl();
        this.locationType = stop.getLocationType() == 1 ? LocationType.STATION : LocationType.STOP;
        this.parentStation = stop.getParentStation();
        this.location = geometryFactory.createPoint(new Coordinate(stop.getLat(), stop.getLon()));
    }

    public Stop(Agency agency, String stopName, String stopCode, String stopUrl, String stopDesc, Double lat, Double lon) {
        this.agency = agency;
        this.stopCode = stopCode;
        this.stopName = stopName;
        this.stopDesc = stopDesc;
        this.stopUrl = stopUrl;
        this.locationType = LocationType.STOP;
        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        this.location = geometryFactory.createPoint(new Coordinate(lon, lat));
    }


    /*
    public static BigInteger nativeInsert(EntityManager entityManager, org.onebusaway.gtfs.model.Stop gtfsStop) {
        Query idQuery = entityManager.createNativeQuery("SELECT NEXTVAL('hibernate_sequence');");
        BigInteger nextId = (BigInteger) idQuery.getSingleResult();

        entityManager.createNativeQuery("INSERT INTO stop (id, locationtype, parentstation, stopcode, stopdesc, gtfsstopid, stopname, stopurl, zoneid, location)" +
                        "  VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ST_GeomFromText( ? , 4326));")
                .setParameter(1, nextId)
                .setParameter(2, gtfsStop.getLocationType() == 1 ? LocationType.STATION.name() : LocationType.STOP.name())
                .setParameter(3, gtfsStop.getParentStation())
                .setParameter(4, gtfsStop.getCode())
                .setParameter(5, gtfsStop.getDesc())
                .setParameter(6, gtfsStop.getId().toString())
                .setParameter(7, gtfsStop.getName())
                .setParameter(8, gtfsStop.getUrl())
                .setParameter(9, gtfsStop.getZoneId())
                .setParameter(10, "POINT(" + gtfsStop.getLon() + " " + gtfsStop.getLat() + ")")
                .executeUpdate();

        return nextId;
    }

    public static BigInteger nativeInsert(EntityManager entityManager, Upload.Route.Stop stop) {
        Query idQuery = entityManager.createNativeQuery("SELECT NEXTVAL('hibernate_sequence');");
        BigInteger nextId = (BigInteger) idQuery.getSingleResult();

        entityManager.createNativeQuery("INSERT INTO stop (id, location, board, alight)" +
                        "  VALUES(?, ST_GeomFromText( ? , 4326), ?, ?);")
                .setParameter(1, nextId)
                .setParameter(2, "POINT(" + stop.getLon() + " " + stop.getLat() + ")")
                .setParameter(3, stop.getBoard())
                .setParameter(4, stop.getAlight())
                .executeUpdate();

        return nextId;
    }

    public Set<Route> routesServed() {
        List<TripPatternStop> stops = TripPatternStop.find("stop = ?", this).fetch();
        HashSet<Route> routes = new HashSet<Route>();

        for (TripPatternStop patternStop : stops) {
            if (patternStop.pattern == null)
                continue;

            if (patternStop.pattern.longest != null && patternStop.pattern.longest == true)
                routes.add(patternStop.pattern.route);
        }

        return routes;
    }

    public Set<TripPattern> tripPatternsServed(Boolean weekday, Boolean saturday, Boolean sunday) {
        List<TripPatternStop> stops = TripPatternStop.find("stop = ?", this).fetch();
        HashSet<TripPattern> patterns = new HashSet<TripPattern>();

        for (TripPatternStop patternStop : stops) {
            if (patternStop.pattern == null)
                continue;

            if (patternStop.pattern.longest == true) {
                if ((weekday && patternStop.pattern.weekday) || (saturday && patternStop.pattern.saturday) || (sunday && patternStop.pattern.sunday))
                    patterns.add(patternStop.pattern);
            }
        }

        return patterns;
    }
    *
     */
}
