package org.codeandomexico.mapmap.server.model;


import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@NoArgsConstructor
public class TripPattern {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hibernate")
    @SequenceGenerator(name = "hibernate", sequenceName = "hibernate_sequence", allocationSize = 1)
    public Long id;
    public String name;
    public String headsign;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "shape_id")
    public TripShape tripShape;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "route_id")
    public Route route;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "tripPattern", orphanRemoval = true)
    public List<TripPatternStop> patternStops;

    public Boolean longest;
    public Boolean weekday;
    public Boolean saturday;
    public Boolean sunday;
    public Boolean useFrequency;
    public Integer startTime;
    public Integer endTime;
    public Integer headway;

}
