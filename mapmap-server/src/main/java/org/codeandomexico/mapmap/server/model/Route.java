package org.codeandomexico.mapmap.server.model;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Entity
@NoArgsConstructor
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hibernate")
    @SequenceGenerator(name = "hibernate", sequenceName = "hibernate_sequence", allocationSize = 1)
    public Long id;

    @OneToOne(mappedBy = "route")
    public TripPattern tripPattern;

    @ManyToOne
    @JoinColumn(name = "agency_id")
    public Agency agency;

    @ManyToOne
    @JoinColumn(name = "phone_id")
    public Phone phone;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "route", orphanRemoval = true)
    public List<RoutePoint> routePoints;

    public String gtfsRouteId;
    public String routeShortName;
    public String routeLongName;
    public String routeDesc;
    public String routeNotes;
    @Enumerated(EnumType.STRING)
    public RouteType routeType;
    public String routeUrl;
    public String routeColor;
    public String routeTextColor;
    public String vehicleCapacity;
    public String vehicleType;
    public Date captureTime;
    // Custom Fields
    public Boolean airCon;
    public String comments;
    public Boolean weekday;
    public Boolean saturday;
    public Boolean sunday;

    public Route(org.onebusaway.gtfs.model.Route route, Agency agency) {
        this.gtfsRouteId = route.getId().toString();
        this.routeShortName = route.getShortName();
        this.routeLongName = route.getLongName();
        this.routeDesc = route.getDesc();
        this.routeType = mapGtfsRouteType(route.getType());
        this.routeUrl = route.getUrl();
        this.routeColor = route.getColor();
        this.agency = agency;
    }

    public Route(String routeShortName, String routeLongName, RouteType routeType, String routeDescription, Agency agency) {
        this.routeShortName = routeShortName;
        this.routeLongName = routeLongName;
        this.routeType = routeType;
        this.routeDesc = routeDescription;
        this.agency = agency;
    }

    public static RouteType mapGtfsRouteType(Integer routeType) {
        return switch (routeType) {
            case 0 -> RouteType.TRAM;
            case 1 -> RouteType.SUBWAY;
            case 2 -> RouteType.RAIL;
            case 3 -> RouteType.BUS;
            case 4 -> RouteType.FERRY;
            case 5 -> RouteType.CABLECAR;
            case 6 -> RouteType.GONDOLA;
            case 7 -> RouteType.FUNICULAR;
            default -> null;
        };
    }

    public static Integer mapGtfsRouteType(RouteType routeType) {
        return switch (routeType) {
            case TRAM -> 0;
            case SUBWAY -> 1;
            case RAIL -> 2;
            case BUS -> 3;
            case FERRY -> 4;
            case CABLECAR -> 5;
            case GONDOLA -> 6;
            case FUNICULAR -> 7;
            default -> null;
        };
    }

}
